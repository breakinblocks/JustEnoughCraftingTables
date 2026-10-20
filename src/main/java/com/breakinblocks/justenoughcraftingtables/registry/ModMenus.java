package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JustEnoughCraftingTables.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<StationMenu>> CRAFTING_STATION =
            MENUS.register("crafting_station", () -> IMenuTypeExtension.create(StationMenu::new));

    private ModMenus() {
    }
}
