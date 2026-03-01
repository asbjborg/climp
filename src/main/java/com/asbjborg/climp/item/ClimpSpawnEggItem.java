package com.asbjborg.climp.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public final class ClimpSpawnEggItem extends DeferredSpawnEggItem {
    private static final double OWNER_ASSIGN_RADIUS = 8.0D;

    public ClimpSpawnEggItem(
            Supplier<? extends EntityType<? extends Mob>> type,
            int backgroundColor,
            int highlightColor,
            Item.Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player) || context.getLevel().isClientSide()) {
            return super.useOn(context);
        }

        BlockPos origin = context.getClickedPos();
        Set<UUID> knownEntityIds = snapshotNearbyClimpIds(context.getLevel(), origin);
        InteractionResult result = super.useOn(context);
        if (result.consumesAction()) {
            assignOwnerToNewlySpawnedClimp(context.getLevel(), player, origin, knownEntityIds);
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) {
            return super.use(level, player, usedHand);
        }

        BlockPos origin = serverPlayer.blockPosition();
        Set<UUID> knownEntityIds = snapshotNearbyClimpIds(level, origin);
        InteractionResultHolder<ItemStack> result = super.use(level, player, usedHand);
        if (result.getResult().consumesAction()) {
            assignOwnerToNewlySpawnedClimp(level, serverPlayer, origin, knownEntityIds);
        }
        return result;
    }

    private static Set<UUID> snapshotNearbyClimpIds(Level level, BlockPos origin) {
        Set<UUID> ids = new HashSet<>();
        for (ClimpEntity climp : getNearbyClimps(level, origin)) {
            ids.add(climp.getUUID());
        }
        return ids;
    }

    private static void assignOwnerToNewlySpawnedClimp(Level level, ServerPlayer player, BlockPos origin, Set<UUID> knownEntityIds) {
        Vec3 center = origin.getCenter();
        ClimpEntity spawnedClimp = getNearbyClimps(level, origin).stream()
                .filter(climp -> !knownEntityIds.contains(climp.getUUID()))
                .filter(climp -> !climp.hasOwner())
                .min((a, b) -> Double.compare(a.distanceToSqr(center), b.distanceToSqr(center)))
                .orElse(null);

        if (spawnedClimp != null) {
            spawnedClimp.setOwner(player);
        }
    }

    private static List<ClimpEntity> getNearbyClimps(Level level, BlockPos origin) {
        return level.getEntitiesOfClass(
                ClimpEntity.class,
                new AABB(origin).inflate(OWNER_ASSIGN_RADIUS),
                ClimpEntity::isAlive);
    }
}
