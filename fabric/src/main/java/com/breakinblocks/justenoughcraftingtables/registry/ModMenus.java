package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import com.breakinblocks.justenoughcraftingtables.menu.StationOpeningData;

public final class ModMenus {
    public static final Entry<MenuType<StationMenu>> CRAFTING_STATION = new Entry<>(JustEnoughCraftingTables.id("crafting_station"),
        Registry.register(BuiltInRegistries.MENU, JustEnoughCraftingTables.id("crafting_station"), new ExtendedScreenHandlerType<>(StationMenu::new, StationOpeningData.STREAM_CODEC)));
    public static void init() {}
}
