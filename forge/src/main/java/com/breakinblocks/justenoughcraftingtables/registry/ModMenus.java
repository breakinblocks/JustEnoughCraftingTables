package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JustEnoughCraftingTables.MOD_ID);

    public static final RegistryObject<MenuType<StationMenu>> CRAFTING_STATION =
            MENUS.register("crafting_station", () -> IForgeMenuType.create(StationMenu::new));

    private ModMenus() {
    }
}
