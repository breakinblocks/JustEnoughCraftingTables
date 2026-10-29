package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class StationScreenBase extends AbstractContainerScreen<StationMenu> {
    protected StationScreenBase(StationMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
}
