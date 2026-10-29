package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.client.renderer.StationRenderer;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, value = Dist.CLIENT)
public final class JustEnoughCraftingTablesClient {
    public static final Identifier DYED_PROPERTY = JustEnoughCraftingTables.id("dyed");

    private JustEnoughCraftingTablesClient() {
    }

    public static void init(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CRAFTING_STATION.get(), StationScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRAFTING_STATION.get(), StationRenderer::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(state -> state.hasProperty(StationColor.PROPERTY)
                        ? state.getValue(StationColor.PROPERTY).tint() : 0xFFFFFFFF),
                ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(JustEnoughCraftingTables.id("station"), StationItemTint.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(DYED_PROPERTY, StationDyedProperty.MAP_CODEC);
    }
}
