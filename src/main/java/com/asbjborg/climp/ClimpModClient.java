package com.asbjborg.climp;

import com.asbjborg.climp.client.ClimpEntityRenderer;
import com.asbjborg.climp.entity.ClimpEntityTypes;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ClimpMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ClimpMod.MODID, value = Dist.CLIENT)
public class ClimpModClient {
    public ClimpModClient(IEventBus modEventBus, ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ClimpMod.LOGGER.info("CLIMP client setup initialized.");
        ClimpMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ClimpEntityTypes.CLIMP.get(), ClimpEntityRenderer::new);
    }
}
