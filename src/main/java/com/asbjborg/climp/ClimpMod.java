package com.asbjborg.climp;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ClimpMod.MODID)
public class ClimpMod {
    // Define mod id in a common place for everything to reference.
    public static final String MODID = "climp";
    // Directly reference a slf4j logger.
    public static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ClimpMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for mod loading.
        modEventBus.addListener(this::commonSetup);

        // Register this mod's ModConfigSpec so FML can create and load the config file.
        modContainer.registerConfig(ModConfig.Type.COMMON, ClimpConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code.
        LOGGER.info("CLIMP common setup initialized.");

        if (ClimpConfig.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", ClimpConfig.MAGIC_NUMBER_INTRODUCTION.get(), ClimpConfig.MAGIC_NUMBER.getAsInt());
        ClimpConfig.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }
}
