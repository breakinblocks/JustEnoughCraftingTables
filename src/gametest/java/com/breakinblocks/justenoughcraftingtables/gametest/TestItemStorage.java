package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class TestItemStorage implements ItemStorage {
    private final ItemStacksResourceHandler nativeStorage;
    private final ItemStorage adapter;

    public TestItemStorage(int size) {
        nativeStorage = new ItemStacksResourceHandler(size);
        adapter = ItemStorage.of(nativeStorage);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        nativeStorage.set(slot, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public int getSlots() {
        return adapter.getSlots();
    }
    @Override
    public ItemStack getStackInSlot(int slot) {
        return adapter.getStackInSlot(slot);
    }
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return adapter.insertItem(slot, stack, simulate);
    }
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return adapter.extractItem(slot, amount, simulate);
    }
    @Override
    public int getSlotLimit(int slot) {
        return adapter.getSlotLimit(slot);
    }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return adapter.isItemValid(slot, stack);
    }
}
