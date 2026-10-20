package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationConfigScreen;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.client.renderer.StationRenderer;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JustEnoughCraftingTablesClient {
    public static final ResourceLocation DYED_PROPERTY = JustEnoughCraftingTables.id("dyed");

    private JustEnoughCraftingTablesClient() {
    }

    public static void init(ModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, parent) -> new StationConfigScreen(parent)));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRAFTING_STATION.get(), StationRenderer::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> tintIndex == 0 && state.hasProperty(StationColor.PROPERTY)
                        ? state.getValue(StationColor.PROPERTY).tint()
                        : 0xFFFFFFFF,
                ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? StationColor.of(stack).tint() : 0xFFFFFFFF,
                ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.CRAFTING_STATION.get(), StationScreen::new);
            ItemProperties.register(ModBlocks.CRAFTING_STATION.get().asItem(), DYED_PROPERTY,
                    (stack, level, entity, seed) -> StationColor.of(stack).isDyed() ? 1.0F : 0.0F);
            ItemProperties.register(ModBlocks.CRAFTING_STATION_SLAB.get().asItem(), DYED_PROPERTY,
                    (stack, level, entity, seed) -> StationColor.of(stack).isDyed() ? 1.0F : 0.0F);
        });
    }
}
