package com.asbjborg.climp.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asbjborg.climp.ClimpConfig;
import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public final class CommandRodItem extends Item {
    private static final double SEARCH_RADIUS = 24.0D;

    public CommandRodItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }

        handleEmergencyRecall(level, serverPlayer);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getPlayer() instanceof ServerPlayer player && player.isShiftKeyDown()) {
            handleEmergencyRecall(level, player);
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        if (!level.getBlockState(clickedPos).is(BlockTags.LOGS)) {
            return InteractionResult.PASS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        int scanLimit = getConfiguredScanLimit();
        int breakLimit = getConfiguredBreakLimit();
        ClusterAnchorResolution anchorResolution = resolveClusterAnchor(level, clickedPos, scanLimit, breakLimit);
        maybeSendScanDebug(player, anchorResolution, scanLimit, breakLimit);
        if (anchorResolution.state == ClusterAnchorResolution.State.TOO_LARGE) {
            player.sendSystemMessage(Component.literal("Climp: That tree is too complex for safe delegation right now."));
            return InteractionResult.SUCCESS;
        }
        if (anchorResolution.state == ClusterAnchorResolution.State.NONE || anchorResolution.orderedTargets.isEmpty()) {
            player.sendSystemMessage(Component.literal("Climp: I could not resolve a valid task target from that tree."));
            return InteractionResult.SUCCESS;
        }

        List<ClimpEntity> nearbyClimps = level.getEntitiesOfClass(
                ClimpEntity.class,
                player.getBoundingBox().inflate(SEARCH_RADIUS),
                ClimpEntity::isAlive);

        if (nearbyClimps.isEmpty()) {
            player.sendSystemMessage(Component.literal("Climp: No Climp in range."));
            return InteractionResult.SUCCESS;
        }

        ClimpEntity nearestReadyClimp = nearbyClimps.stream()
                .filter(ClimpEntity::canAcceptCommandTask)
                .min(Comparator.comparingDouble(climp -> climp.distanceToSqr(player)))
                .orElse(null);

        if (nearestReadyClimp == null) {
            boolean hasBusyClimp = nearbyClimps.stream().anyMatch(ClimpEntity::hasCommandTask);
            if (hasBusyClimp) {
                player.sendSystemMessage(Component.literal("Climp: I am busy with a task right now."));
                return InteractionResult.SUCCESS;
            }

            boolean hasCoolingClimp = nearbyClimps.stream().anyMatch(ClimpEntity::isOnCommandCooldown);
            if (hasCoolingClimp) {
                player.sendSystemMessage(Component.literal("Climp: Brief cooldown. Dramatic pause in progress."));
                return InteractionResult.SUCCESS;
            }

            player.sendSystemMessage(Component.literal("Climp: No available Climp in range."));
            return InteractionResult.SUCCESS;
        }

        if (!nearestReadyClimp.assignLogTask(player, anchorResolution.orderedTargets)) {
            player.sendSystemMessage(Component.literal("Climp: I cannot reach that task target."));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    private static void handleEmergencyRecall(Level level, ServerPlayer player) {
        List<ClimpEntity> nearbyClimps = level.getEntitiesOfClass(
                ClimpEntity.class,
                player.getBoundingBox().inflate(SEARCH_RADIUS),
                ClimpEntity::isAlive);

        if (nearbyClimps.isEmpty()) {
            player.sendSystemMessage(Component.literal("Climp: No Climp in range."));
            return;
        }

        ClimpEntity nearestBusyClimp = nearbyClimps.stream()
                .filter(ClimpEntity::hasCommandTask)
                .min(Comparator.comparingDouble(climp -> climp.distanceToSqr(player)))
                .orElse(null);

        if (nearestBusyClimp == null) {
            player.sendSystemMessage(Component.literal("Climp: Nothing to recall right now."));
            return;
        }

        if (nearestBusyClimp.requestImmediateRecall(player)) {
            player.sendSystemMessage(Component.literal("Climp: Recall acknowledged. Returning immediately."));
            return;
        }

        player.sendSystemMessage(Component.literal("Climp: Recall failed. Try again."));
    }

    private static int getConfiguredScanLimit() {
        return Math.max(1, ClimpConfig.COMMAND_TREE_SCAN_LIMIT.getAsInt());
    }

    private static int getConfiguredBreakLimit() {
        return Math.max(1, ClimpConfig.COMMAND_TREE_BREAK_LIMIT.getAsInt());
    }

    private static void maybeSendScanDebug(
            ServerPlayer player,
            ClusterAnchorResolution resolution,
            int scanLimit,
            int breakLimit) {
        if (!ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.getAsBoolean()) {
            return;
        }

        String message = "Climp Debug: scanned " + resolution.scannedCount + " logs, queued "
                + resolution.orderedTargets.size() + " (scanLimit=" + scanLimit + ", breakLimit=" + breakLimit + ").";
        player.sendSystemMessage(Component.literal(message));
    }

    private static ClusterAnchorResolution resolveClusterAnchor(Level level, BlockPos startPos, int scanLimit, int breakLimit) {
        BlockPos start = startPos.immutable();
        if (!level.getBlockState(start).is(BlockTags.LOGS)) {
            return ClusterAnchorResolution.none();
        }

        Set<BlockPos> visitedLogs = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visitedLogs.add(start);

        BlockPos anchor = start;
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (visitedLogs.size() > scanLimit) {
                return ClusterAnchorResolution.tooLarge(visitedLogs.size());
            }

            if (isBetterAnchor(current, anchor, start)) {
                anchor = current;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockPos next = current.offset(dx, dy, dz).immutable();
                        if (visitedLogs.contains(next) || !level.getBlockState(next).is(BlockTags.LOGS)) {
                            continue;
                        }

                        visitedLogs.add(next);
                        queue.addLast(next);
                    }
                }
            }
        }

        BlockPos anchorPos = anchor.immutable();
        List<BlockPos> orderedTargets = new ArrayList<>(visitedLogs.size());
        orderedTargets.add(anchorPos);
        visitedLogs.stream()
                .filter(pos -> !pos.equals(anchorPos))
                .sorted(Comparator
                        .<BlockPos>comparingInt(pos -> pos.getY())
                        .thenComparingDouble(pos -> pos.distSqr(anchorPos)))
                .map(BlockPos::immutable)
                .forEachOrdered(orderedTargets::add);

        if (orderedTargets.size() > breakLimit) {
            orderedTargets = new ArrayList<>(orderedTargets.subList(0, breakLimit));
        }

        return ClusterAnchorResolution.resolved(orderedTargets, visitedLogs.size());
    }

    private static boolean isBetterAnchor(BlockPos candidate, BlockPos currentAnchor, BlockPos clickedStart) {
        if (candidate.getY() != currentAnchor.getY()) {
            return candidate.getY() < currentAnchor.getY();
        }
        return candidate.distSqr(clickedStart) < currentAnchor.distSqr(clickedStart);
    }

    private static final class ClusterAnchorResolution {
        private enum State {
            RESOLVED,
            TOO_LARGE,
            NONE
        }

        private final State state;
        private final List<BlockPos> orderedTargets;
        private final int scannedCount;

        private ClusterAnchorResolution(State state, List<BlockPos> orderedTargets, int scannedCount) {
            this.state = state;
            this.orderedTargets = orderedTargets == null ? List.of() : List.copyOf(orderedTargets);
            this.scannedCount = Math.max(0, scannedCount);
        }

        private static ClusterAnchorResolution resolved(List<BlockPos> orderedTargets, int scannedCount) {
            return new ClusterAnchorResolution(State.RESOLVED, orderedTargets, scannedCount);
        }

        private static ClusterAnchorResolution tooLarge(int scannedCount) {
            return new ClusterAnchorResolution(State.TOO_LARGE, List.of(), scannedCount);
        }

        private static ClusterAnchorResolution none() {
            return new ClusterAnchorResolution(State.NONE, List.of(), 0);
        }
    }
}
