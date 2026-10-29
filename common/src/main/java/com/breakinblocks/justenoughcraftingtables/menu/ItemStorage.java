package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.world.item.ItemStack;

public interface ItemStorage {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);

    default Object identity() {
        return this;
    }


    static ItemStack insertItemStacked(ItemStorage handler, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack left = stack.copy();
        for (int pass = 0; pass < 2 && !left.isEmpty(); pass++) {
            for (int slot = 0; slot < handler.getSlots() && !left.isEmpty(); slot++) {
                ItemStack current = handler.getStackInSlot(slot);
                if ((pass == 0 && !current.isEmpty() && ItemStack.isSameItemSameComponents(current, left))
                        || (pass == 1 && current.isEmpty())) {
                    left = handler.insertItem(slot, left, simulate);
                }
            }
        }
        return left;
    }

}
