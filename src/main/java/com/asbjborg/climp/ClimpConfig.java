package com.asbjborg.climp;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClimpConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue GIVE_SPAWN_EGG_ON_FIRST_JOIN = BUILDER
            .comment("Give the Climp spawn egg the first time a player joins a world.")
            .define("giveSpawnEggOnFirstJoin", true);

    public static final ModConfigSpec.IntValue PREFERRED_HOTBAR_SLOT = BUILDER
            .comment("Preferred hotbar slot for the gifted Climp spawn egg (0-8).")
            .defineInRange("preferredHotbarSlot", 0, 0, 8);

    static final ModConfigSpec SPEC = BUILDER.build();
}
