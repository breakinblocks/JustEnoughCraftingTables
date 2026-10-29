package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.client.renderer.StationRenderer;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import com.breakinblocks.justenoughcraftingtables.client.gui.ViewMemory;
import com.breakinblocks.justenoughcraftingtables.net.ClientModNetwork;

public final class JustEnoughCraftingTablesClient implements ClientModInitializer {
    public static final ResourceLocation DYED_PROPERTY = JustEnoughCraftingTables.id("dyed");

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.CRAFTING_STATION.get(), StationScreen::new);
        BlockEntityRenderers.register(ModBlockEntities.CRAFTING_STATION.get(), StationRenderer::new);
        ColorProviderRegistry.BLOCK.register((state, level, pos, tint) -> tint == 0 ? state.getValue(StationColor.PROPERTY).tint() : -1,
                ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
        ColorProviderRegistry.ITEM.register((stack, tint) -> tint == 0 ? StationColor.of(stack).tint() : -1,
                ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
        for (var block : new Block[] {ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get()}) {
            ItemProperties.register(block.asItem(), DYED_PROPERTY, (stack, level, entity, seed) -> StationColor.of(stack).isDyed() ? 1.0F : 0.0F);
        }
        ClientModNetwork.registerClient();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ViewMemory.clear());
    }
}
