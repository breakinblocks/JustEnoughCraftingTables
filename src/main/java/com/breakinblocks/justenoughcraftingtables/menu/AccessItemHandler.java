package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.item.ItemStack;

/** Retained source references must recheck access at the point of each operation. */
final class AccessItemHandler implements ItemStorage {
    private static final int MAX_SLOTS = 1 << 20;
    private final ItemStorage delegate;
    private final BooleanSupplier allowed;

    AccessItemHandler(ItemStorage delegate, BooleanSupplier allowed) {
        this.delegate = delegate;
        this.allowed = allowed;
    }

    @Override
    public Object identity() {
        return delegate.identity();
    }

    @Override
    public int getSlots() {
        return Math.max(0, Math.min(MAX_SLOTS, delegate.getSlots()));
    }

    private boolean accessible(int slot) {
        return slot >= 0 && slot < getSlots() && allowed.getAsBoolean();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return accessible(slot) ? delegate.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return accessible(slot) ? delegate.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return accessible(slot) ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return accessible(slot) ? delegate.getSlotLimit(slot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return accessible(slot) && delegate.isItemValid(slot, stack);
    }
}
