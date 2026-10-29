package com.breakinblocks.justenoughcraftingtables.platform;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import com.breakinblocks.justenoughcraftingtables.client.gui.CompactCount;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
public abstract class StationScreenBase extends AbstractContainerScreen<StationMenu> {
    public StationScreenBase(StationMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, String count) {
        if (count == null && slot instanceof WindowSlot && stack.getCount() > 999
                && JustEnoughCraftingTablesConfig.CLIENT.compactCounts.get()) count = CompactCount.format(stack.getCount());
        super.renderSlotContents(graphics, stack, slot, count);
    }
}

