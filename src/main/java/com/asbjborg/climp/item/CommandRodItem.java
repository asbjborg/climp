package com.asbjborg.climp.item;

import java.util.Comparator;
import java.util.List;

import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class CommandRodItem extends Item {
    private static final double SEARCH_RADIUS = 24.0D;

    public CommandRodItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
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
                climp -> climp.isAlive() && !climp.hasCommandTask());
        ClimpEntity nearestClimp = nearbyClimps.stream()
                .min(Comparator.comparingDouble(climp -> climp.distanceToSqr(player)))
                .orElse(null);

        if (nearestClimp == null) {
            player.sendSystemMessage(Component.literal("Climp: No available Climp in range."));
            return InteractionResult.SUCCESS;
        }

        if (!nearestClimp.assignLogTask(player, clickedPos)) {
            player.sendSystemMessage(Component.literal("Climp: I cannot reach that task target."));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }
}
