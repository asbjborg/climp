package com.asbjborg.climp.sound;

import java.util.Map;

import com.asbjborg.climp.ClimpMod;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events for Climp voice lines. One event per line (e.g., climp_idle_1, climp_idle_2).
 */
public final class ClimpSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT, ClimpMod.MODID);

    // IDLE
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_1 = reg("climp_idle_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_2 = reg("climp_idle_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_3 = reg("climp_idle_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_4 = reg("climp_idle_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_5 = reg("climp_idle_5");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_6 = reg("climp_idle_6");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_7 = reg("climp_idle_7");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_8 = reg("climp_idle_8");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_IDLE_9 = reg("climp_idle_9");

    // HIT
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_HIT_1 = reg("climp_hit_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_HIT_2 = reg("climp_hit_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_HIT_3 = reg("climp_hit_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_HIT_4 = reg("climp_hit_4");

    // TASK_START
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_START_1 = reg("climp_task_start_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_START_2 = reg("climp_task_start_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_START_3 = reg("climp_task_start_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_START_4 = reg("climp_task_start_4");

    // TASK_COMPLETE
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_COMPLETE_1 = reg("climp_task_complete_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_COMPLETE_2 = reg("climp_task_complete_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_COMPLETE_3 = reg("climp_task_complete_3");

    // TASK_FAILED_UNREACHABLE
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_FAILED_UNREACHABLE_1 = reg("climp_task_failed_unreachable_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_FAILED_UNREACHABLE_2 = reg("climp_task_failed_unreachable_2");

    // TASK_FAILED_TARGET_REMOVED
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_FAILED_TARGET_REMOVED_1 = reg("climp_task_failed_target_removed_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIMP_TASK_FAILED_TARGET_REMOVED_2 = reg("climp_task_failed_target_removed_2");

    private static final Map<String, Holder<SoundEvent>> BY_SOUND_ID = Map.ofEntries(
            Map.entry("climp_idle_1", CLIMP_IDLE_1),
            Map.entry("climp_idle_2", CLIMP_IDLE_2),
            Map.entry("climp_idle_3", CLIMP_IDLE_3),
            Map.entry("climp_idle_4", CLIMP_IDLE_4),
            Map.entry("climp_idle_5", CLIMP_IDLE_5),
            Map.entry("climp_idle_6", CLIMP_IDLE_6),
            Map.entry("climp_idle_7", CLIMP_IDLE_7),
            Map.entry("climp_idle_8", CLIMP_IDLE_8),
            Map.entry("climp_idle_9", CLIMP_IDLE_9),
            Map.entry("climp_hit_1", CLIMP_HIT_1),
            Map.entry("climp_hit_2", CLIMP_HIT_2),
            Map.entry("climp_hit_3", CLIMP_HIT_3),
            Map.entry("climp_hit_4", CLIMP_HIT_4),
            Map.entry("climp_task_start_1", CLIMP_TASK_START_1),
            Map.entry("climp_task_start_2", CLIMP_TASK_START_2),
            Map.entry("climp_task_start_3", CLIMP_TASK_START_3),
            Map.entry("climp_task_start_4", CLIMP_TASK_START_4),
            Map.entry("climp_task_complete_1", CLIMP_TASK_COMPLETE_1),
            Map.entry("climp_task_complete_2", CLIMP_TASK_COMPLETE_2),
            Map.entry("climp_task_complete_3", CLIMP_TASK_COMPLETE_3),
            Map.entry("climp_task_failed_unreachable_1", CLIMP_TASK_FAILED_UNREACHABLE_1),
            Map.entry("climp_task_failed_unreachable_2", CLIMP_TASK_FAILED_UNREACHABLE_2),
            Map.entry("climp_task_failed_target_removed_1", CLIMP_TASK_FAILED_TARGET_REMOVED_1),
            Map.entry("climp_task_failed_target_removed_2", CLIMP_TASK_FAILED_TARGET_REMOVED_2));

    private ClimpSoundEvents() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ClimpMod.MODID, name)));
    }

    /** Returns the sound event for the given soundId (e.g. "climp_idle_1"). Uses climp_idle_1 as fallback if unknown. */
    public static Holder<SoundEvent> get(String soundId) {
        Holder<SoundEvent> holder = BY_SOUND_ID.get(soundId);
        return holder != null ? holder : CLIMP_IDLE_1;
    }
}
