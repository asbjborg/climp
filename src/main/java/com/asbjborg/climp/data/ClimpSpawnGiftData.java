package com.asbjborg.climp.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.asbjborg.climp.ClimpMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class ClimpSpawnGiftData extends SavedData {
    private static final String DATA_NAME = ClimpMod.MODID + "_spawn_gift_data";
    private static final String GRANTED_PLAYERS_KEY = "granted_players";
    private static final String PLAYER_UUID_KEY = "player_uuid";

    private final Set<UUID> grantedPlayers = new HashSet<>();

    public static ClimpSpawnGiftData get(DimensionDataStorage dataStorage) {
        return dataStorage.computeIfAbsent(new SavedData.Factory<>(ClimpSpawnGiftData::new, ClimpSpawnGiftData::load, null), DATA_NAME);
    }

    private static ClimpSpawnGiftData load(CompoundTag tag, HolderLookup.Provider provider) {
        ClimpSpawnGiftData data = new ClimpSpawnGiftData();
        ListTag grantedPlayersTag = tag.getList(GRANTED_PLAYERS_KEY, Tag.TAG_COMPOUND);

        for (Tag entryTag : grantedPlayersTag) {
            if (!(entryTag instanceof CompoundTag playerTag) || !playerTag.hasUUID(PLAYER_UUID_KEY)) {
                continue;
            }

            data.grantedPlayers.add(playerTag.getUUID(PLAYER_UUID_KEY));
        }

        return data;
    }

    public boolean hasReceivedGift(UUID playerId) {
        return grantedPlayers.contains(playerId);
    }

    public void markGiftGranted(UUID playerId) {
        if (grantedPlayers.add(playerId)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag grantedPlayersTag = new ListTag();

        for (UUID playerId : grantedPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(PLAYER_UUID_KEY, playerId);
            grantedPlayersTag.add(playerTag);
        }

        tag.put(GRANTED_PLAYERS_KEY, grantedPlayersTag);
        return tag;
    }
}
