package com.asbjborg.climp;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.asbjborg.climp.entity.ClimpEntityTypes;
import com.asbjborg.climp.item.ClimpItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.asbjborg.climp.event.ClimpPlayerEvents;

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
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::addCreativeTabEntries);
        NeoForge.EVENT_BUS.addListener(ClimpPlayerEvents::onPlayerLoggedIn);

        // Register entity types.
        ClimpEntityTypes.ENTITY_TYPES.register(modEventBus);
        ClimpItems.ITEMS.register(modEventBus);

        // Register this mod's ModConfigSpec so FML can create and load the config file.
        modContainer.registerConfig(ModConfig.Type.COMMON, ClimpConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("CLIMP common setup initialized.");
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ClimpEntityTypes.CLIMP.get(), ClimpEntityTypes.createAttributes().build());
    }

    private void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ClimpItems.CLIMP_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ClimpItems.COMMAND_ROD);
        }
    }
}
