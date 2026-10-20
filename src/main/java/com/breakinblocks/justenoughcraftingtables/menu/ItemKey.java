package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.world.item.ItemStack;

public final class ItemKey {
    private final ItemStack stack;
    private final int hash;

    private ItemKey(ItemStack stack) {
        this.stack = stack;
        this.hash = ItemStack.hashItemAndComponents(stack);
    }

    public static ItemKey of(ItemStack stack) {
        return new ItemKey(stack.copyWithCount(1));
    }

    public static ItemKey view(ItemStack stack) {
        return new ItemKey(stack);
    }

    public ItemStack stack() {
        return stack;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemKey key && hash == key.hash && ItemStack.isSameItemSameComponents(stack, key.stack);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
