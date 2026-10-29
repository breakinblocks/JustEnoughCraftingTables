package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.menu.FabricStorage;
import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.world.item.ItemStack;

public class TestItemStorage implements ItemStorage {
    private final List<TestSlot> nativeStorage = new ArrayList<>();
    private final ItemStorage adapter;

    public TestItemStorage(int size) {
        for (int i = 0; i < size; i++) nativeStorage.add(new TestSlot());
        adapter = new FabricStorage(new CombinedSlottedStorage<>(nativeStorage));
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        // Deliberately allow oversized stacks to exercise multi-call extraction.
        nativeStorage.get(slot).set(stack);
    }

    private static final class TestSlot extends SingleVariantStorage<ItemVariant> {
        protected ItemVariant getBlankVariant() { return ItemVariant.blank(); }
        protected long getCapacity(ItemVariant variant) { return variant.isBlank() ? 64 : variant.toStack().getMaxStackSize(); }
        void set(ItemStack stack) { variant = ItemVariant.of(stack); amount = stack.getCount(); }
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
