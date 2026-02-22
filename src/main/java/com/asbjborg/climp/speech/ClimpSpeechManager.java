package com.asbjborg.climp.speech;

import java.util.EnumMap;
import java.util.Map;

import com.asbjborg.climp.entity.ClimpEntity;
import com.asbjborg.climp.sound.ClimpSoundEvents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Handles Climp speech gating (rarity + cooldowns) and delivery.
 */
public final class ClimpSpeechManager {
    public enum TaskFailureReason {
        UNREACHABLE,
        TARGET_REMOVED
    }

    private int idleCooldownTicks = 20 * 20;
    private int hitCooldownTicks = 0;
    private final Map<ClimpSpeechType, String> lastSoundIdByType = new EnumMap<>(ClimpSpeechType.class);

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
            send(climp, player, ClimpSpeechType.HIT, pickLine(ClimpSpeechType.HIT, climp));
            hitCooldownTicks = 20 * 8;
            // Delay idle chatter for a bit after a hit reaction.
            idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 10);
        }
    }

    public void onTaskStart(ClimpEntity climp, ServerPlayer player) {
        if (climp.level().isClientSide) {
            return;
        }
        send(climp, player, ClimpSpeechType.TASK_START, pickLine(ClimpSpeechType.TASK_START, climp));
        // Pause idle chatter briefly so command speech is not immediately followed by idle text.
        idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 8);
    }

    public void onTaskComplete(ClimpEntity climp, ServerPlayer player) {
        if (climp.level().isClientSide) {
            return;
        }
        send(climp, player, ClimpSpeechType.TASK_COMPLETE, pickLine(ClimpSpeechType.TASK_COMPLETE, climp));
        idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 10);
    }

    public void onTaskFailed(ClimpEntity climp, ServerPlayer player, TaskFailureReason failureReason) {
        if (climp.level().isClientSide) {
            return;
        }
        ClimpSpeechType failureType = switch (failureReason) {
            case TARGET_REMOVED -> ClimpSpeechType.TASK_FAILED_TARGET_REMOVED;
            case UNREACHABLE -> ClimpSpeechType.TASK_FAILED_UNREACHABLE;
        };
        send(climp, player, failureType, pickLine(failureType, climp));
        idleCooldownTicks = Math.max(idleCooldownTicks, 20 * 10);
    }

    private void tryIdleSpeech(ClimpEntity climp) {
        if (climp.level().isClientSide || idleCooldownTicks-- > 0) {
            return;
        }

        if (climp.hasCommandTask()) {
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

        send(climp, player, ClimpSpeechType.IDLE, pickLine(ClimpSpeechType.IDLE, climp));
        idleCooldownTicks = 20 * 35;
    }

    private ClimpSpeechLibrary.Line pickLine(ClimpSpeechType type, ClimpEntity climp) {
        return ClimpSpeechLibrary.randomLine(type, climp.getRandom(), lastSoundIdByType.get(type));
    }

    private void send(ClimpEntity climp, ServerPlayer player, ClimpSpeechType type, ClimpSpeechLibrary.Line line) {
        lastSoundIdByType.put(type, line.soundId());
        player.sendSystemMessage(Component.literal("Climp: " + line.text()));
        climp.level().playSound(
                null,
                climp.blockPosition(),
                ClimpSoundEvents.get(line.soundId()).value(),
                SoundSource.NEUTRAL,
                1.0f,
                1.0f);
    }
}
