package com.asbjborg.climp.item;

import java.util.Comparator;
import java.util.List;

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

        if (!nearestReadyClimp.assignLogTask(player, clickedPos)) {
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
}
