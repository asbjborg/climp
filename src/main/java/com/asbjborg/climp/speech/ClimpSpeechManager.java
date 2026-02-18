package com.asbjborg.climp.speech;

import com.asbjborg.climp.entity.ClimpEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Handles Climp speech gating (rarity + cooldowns) and delivery.
 */
public final class ClimpSpeechManager {
    private int idleCooldownTicks = 20 * 20;
    private int hitCooldownTicks = 0;

    public void tick(ClimpEntity climp) {
        if (hitCooldownTicks > 0) {
            hitCooldownTicks--;
        }
        tryIdleSpeech(climp);
    }

    public void onHit(ClimpEntity climp, DamageSource source, boolean didHurt) {
        if (!didHurt || climp.level().isClientSide || hitCooldownTicks > 0) {
            return;
        }

        if (source.getEntity() instanceof ServerPlayer player) {
            send(player, ClimpSpeechLibrary.randomLine(ClimpSpeechType.HIT, climp.getRandom()));
            hitCooldownTicks = 20 * 8;
            // Delay idle chatter for a bit after a hit reaction.
            idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 10);
        }
    }

    public void onTaskStart(ClimpEntity climp, ServerPlayer player) {
        if (climp.level().isClientSide) {
            return;
        }
        send(player, ClimpSpeechLibrary.randomLine(ClimpSpeechType.TASK_START, climp.getRandom()));
        // Pause idle chatter briefly so command speech is not immediately followed by idle text.
        idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 8);
    }

    public void onTaskComplete(ClimpEntity climp, ServerPlayer player) {
        if (climp.level().isClientSide) {
            return;
        }
        send(player, ClimpSpeechLibrary.randomLine(ClimpSpeechType.TASK_COMPLETE, climp.getRandom()));
        idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 10);
    }

    private void tryIdleSpeech(ClimpEntity climp) {
        if (climp.level().isClientSide || idleCooldownTicks-- > 0) {
            return;
        }

        Player nearest = climp.level().getNearestPlayer(climp, 7.0D);
        if (!(nearest instanceof ServerPlayer player) || nearest.isSpectator()) {
            return;
        }

        boolean climpIsSettled = climp.getNavigation().isDone() && climp.distanceToSqr(nearest) < 25.0D;
        if (!climpIsSettled || climp.getRandom().nextInt(16) != 0) {
            idleCooldownTicks = 20 * 8;
            return;
        }

        send(player, ClimpSpeechLibrary.randomLine(ClimpSpeechType.IDLE, climp.getRandom()));
        idleCooldownTicks = 20 * 35;
    }

    private static void send(ServerPlayer player, String line) {
        player.sendSystemMessage(Component.literal("Climp: " + line));
    }
}
