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

    public static final ModConfigSpec.IntValue COMMAND_TREE_SCAN_LIMIT = BUILDER
            .comment("Maximum number of connected logs scanned for a command-rod tree task.")
            .defineInRange("commandTreeScanLimit", 100, 1, 2048);

    public static final ModConfigSpec.IntValue COMMAND_TREE_BREAK_LIMIT = BUILDER
            .comment("Maximum number of logs Climp will break from one scanned tree task.")
            .defineInRange("commandTreeBreakLimit", 100, 1, 2048);

    public static final ModConfigSpec.BooleanValue COMMAND_TREE_SCAN_DEBUG_MESSAGES = BUILDER
            .comment("When true, shows command-rod debug messages with scanned/queued log counts.")
            .define("commandTreeScanDebugMessages", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
