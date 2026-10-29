package com.breakinblocks.justenoughcraftingtables.menu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
public record NeoStorage(IItemHandler delegate) implements ItemStorage {
    public int getSlots() { return delegate.getSlots(); }
    public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return delegate.insertItem(slot, stack, simulate); }
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return delegate.extractItem(slot, amount, simulate); }
    public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
    public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }
    public Object identity() { return delegate; }
}
