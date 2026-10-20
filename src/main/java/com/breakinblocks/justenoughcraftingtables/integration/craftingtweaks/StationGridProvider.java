package com.breakinblocks.justenoughcraftingtables.integration.craftingtweaks;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.blay09.mods.craftingtweaks.api.CraftingGridBuilder;
import net.blay09.mods.craftingtweaks.api.CraftingGridProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class StationGridProvider implements CraftingGridProvider {
    public static final int GRID_SIZE = 9;

    @Override
    public String getModId() {
        return JustEnoughCraftingTables.MOD_ID;
    }

    @Override
    public boolean handles(AbstractContainerMenu menu) {
        return menu instanceof StationMenu;
    }

    @Override
    public void buildCraftingGrids(CraftingGridBuilder builder, AbstractContainerMenu menu) {
        builder.addGrid(StationMenu.GRID_START, 3, 3);
    }
}
