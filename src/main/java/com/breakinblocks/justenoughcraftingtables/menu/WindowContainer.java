package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WindowContainer implements Container {
    private final StationMenu menu;

    public WindowContainer(StationMenu menu) {
        this.menu = menu;
    }

    @Override
    public int getContainerSize() {
        return ClientWindow.SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            if (!menu.windowSlot(k).getItem().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return menu.windowSlot(slot).getItem();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return menu.windowSlot(slot).remove(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        WindowSlot windowSlot = menu.windowSlot(slot);
        return windowSlot.remove(windowSlot.getItem().getCount());
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        menu.windowSlot(slot).set(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 99;
    }

    @Override
    public void setChanged() {
        menu.onWindowModified();
    }

    @Override
    public boolean stillValid(Player player) {
        return menu.stillValid(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return menu.windowSlot(slot).mayPlace(stack);
    }

    @Override
    public void clearContent() {
    }
}
