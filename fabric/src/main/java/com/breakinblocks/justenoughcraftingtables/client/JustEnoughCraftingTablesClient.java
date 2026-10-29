package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.client.gui.ViewMemory;
import com.breakinblocks.justenoughcraftingtables.client.renderer.StationRenderer;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.net.ClientModNetwork;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;

public final class JustEnoughCraftingTablesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.CRAFTING_STATION.get(), StationScreen::new);
        BlockEntityRenderers.register(ModBlockEntities.CRAFTING_STATION.get(), StationRenderer::new);
        BlockColorRegistry.register(List.of(state -> state.hasProperty(StationColor.PROPERTY)
            ? state.getValue(StationColor.PROPERTY).tint() : 0xFFFFFFFF), ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get());
        ItemTintSources.ID_MAPPER.put(JustEnoughCraftingTables.id("station"), StationItemTint.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(JustEnoughCraftingTables.id("dyed"), StationDyedProperty.MAP_CODEC);
        ClientModNetwork.registerClient();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ViewMemory.clear());
    }
}
