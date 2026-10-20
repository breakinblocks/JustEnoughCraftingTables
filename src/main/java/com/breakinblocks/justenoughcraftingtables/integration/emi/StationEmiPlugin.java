package com.breakinblocks.justenoughcraftingtables.integration.emi;

import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;

@EmiEntrypoint
public class StationEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeHandler(ModMenus.CRAFTING_STATION.get(), new StationEmiHandler());
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModBlocks.CRAFTING_STATION.get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModBlocks.CRAFTING_STATION_SLAB.get()));
        registry.addExclusionArea(StationScreen.class, (screen, consumer) -> {
            for (Rect2i area : screen.getExclusionAreas()) {
                consumer.accept(new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
            }
        });
    }
}
