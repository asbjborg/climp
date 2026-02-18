package com.asbjborg.climp.event;

import com.asbjborg.climp.ClimpConfig;
import com.asbjborg.climp.data.ClimpSpawnGiftData;
import com.asbjborg.climp.item.ClimpItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class ClimpPlayerEvents {
    private ClimpPlayerEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !ClimpConfig.GIVE_SPAWN_EGG_ON_FIRST_JOIN.getAsBoolean()) {
            return;
        }

        ServerLevel overworld = player.server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            return;
        }

        ClimpSpawnGiftData spawnGiftData = ClimpSpawnGiftData.get(overworld.getDataStorage());
        if (spawnGiftData.hasReceivedGift(player.getUUID())) {
            return;
        }

        ItemStack spawnEgg = new ItemStack(ClimpItems.CLIMP_SPAWN_EGG.get());
        int preferredSlot = ClimpConfig.PREFERRED_HOTBAR_SLOT.getAsInt();

        if (player.getInventory().getItem(preferredSlot).isEmpty()) {
            player.getInventory().setItem(preferredSlot, spawnEgg);
        } else if (!player.getInventory().add(spawnEgg)) {
            player.drop(spawnEgg, false);
        }

        spawnGiftData.markGiftGranted(player.getUUID());
    }
}
