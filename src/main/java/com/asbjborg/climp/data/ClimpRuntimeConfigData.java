package com.asbjborg.climp.data;

import com.asbjborg.climp.ClimpConfig;
import com.asbjborg.climp.ClimpMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;

public final class ClimpRuntimeConfigData extends SavedData {
    private static final String DATA_NAME = ClimpMod.MODID + "_runtime_config_data";
    private static final String COMMAND_TREE_SCAN_LIMIT_KEY = "command_tree_scan_limit";
    private static final String COMMAND_TREE_BREAK_LIMIT_KEY = "command_tree_break_limit";
    private static final String COMMAND_TREE_SCAN_DEBUG_KEY = "command_tree_scan_debug";

    private int commandTreeScanLimit;
    private int commandTreeBreakLimit;
    private boolean commandTreeScanDebugMessages;

    public static ClimpRuntimeConfigData get(ServerLevel overworld) {
        DimensionDataStorage dataStorage = overworld.getDataStorage();
        return dataStorage.computeIfAbsent(
                new SavedData.Factory<>(ClimpRuntimeConfigData::new, ClimpRuntimeConfigData::load, null),
                DATA_NAME);
    }

    private ClimpRuntimeConfigData() {
        this.commandTreeScanLimit = ClimpConfig.COMMAND_TREE_SCAN_LIMIT.getAsInt();
        this.commandTreeBreakLimit = ClimpConfig.COMMAND_TREE_BREAK_LIMIT.getAsInt();
        this.commandTreeScanDebugMessages = ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.getAsBoolean();
    }

    private static ClimpRuntimeConfigData load(CompoundTag tag, HolderLookup.Provider provider) {
        ClimpRuntimeConfigData data = new ClimpRuntimeConfigData();
        if (tag.contains(COMMAND_TREE_SCAN_LIMIT_KEY, Tag.TAG_INT)) {
            data.commandTreeScanLimit = tag.getInt(COMMAND_TREE_SCAN_LIMIT_KEY);
        }
        if (tag.contains(COMMAND_TREE_BREAK_LIMIT_KEY, Tag.TAG_INT)) {
            data.commandTreeBreakLimit = tag.getInt(COMMAND_TREE_BREAK_LIMIT_KEY);
        }
        if (tag.contains(COMMAND_TREE_SCAN_DEBUG_KEY, Tag.TAG_BYTE)) {
            data.commandTreeScanDebugMessages = tag.getBoolean(COMMAND_TREE_SCAN_DEBUG_KEY);
        }
        return data;
    }

    public void applyToRuntimeConfig() {
        ClimpConfig.COMMAND_TREE_SCAN_LIMIT.set(this.commandTreeScanLimit);
        ClimpConfig.COMMAND_TREE_BREAK_LIMIT.set(this.commandTreeBreakLimit);
        ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.set(this.commandTreeScanDebugMessages);
    }

    public void persistFromRuntimeConfig() {
        this.commandTreeScanLimit = ClimpConfig.COMMAND_TREE_SCAN_LIMIT.getAsInt();
        this.commandTreeBreakLimit = ClimpConfig.COMMAND_TREE_BREAK_LIMIT.getAsInt();
        this.commandTreeScanDebugMessages = ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.getAsBoolean();
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt(COMMAND_TREE_SCAN_LIMIT_KEY, this.commandTreeScanLimit);
        tag.putInt(COMMAND_TREE_BREAK_LIMIT_KEY, this.commandTreeBreakLimit);
        tag.putBoolean(COMMAND_TREE_SCAN_DEBUG_KEY, this.commandTreeScanDebugMessages);
        return tag;
    }
}
