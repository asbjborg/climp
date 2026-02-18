package com.asbjborg.climp.item;

import com.asbjborg.climp.ClimpMod;
import com.asbjborg.climp.entity.ClimpEntityTypes;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClimpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ClimpMod.MODID);

    public static final DeferredItem<Item> CLIMP_SPAWN_EGG = ITEMS.register(
            "climp_spawn_egg",
            () -> new DeferredSpawnEggItem(
                    ClimpEntityTypes.CLIMP,
                    0x9FA8B4,
                    0x5F6772,
                    new Item.Properties()));

    public static final DeferredItem<Item> COMMAND_ROD = ITEMS.register(
            "command_rod",
            () -> new CommandRodItem(new Item.Properties()));

    private ClimpItems() {
    }
}
