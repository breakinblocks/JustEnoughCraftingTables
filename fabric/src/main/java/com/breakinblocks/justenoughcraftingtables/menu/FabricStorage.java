package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class FabricStorage implements ItemStorage {
    private final Storage<ItemVariant> storage;
    private final LongSupplier clock;
    private long snapshotTick = Long.MIN_VALUE;
    private List<StorageView<ItemVariant>> snapshot = List.of();

    public FabricStorage(Storage<ItemVariant> storage) { this(storage, () -> 0); }
    public FabricStorage(Storage<ItemVariant> storage, LongSupplier clock) {
        this.storage = storage;
        this.clock = clock;
    }
    private List<StorageView<ItemVariant>> views() {
        long tick = clock.getAsLong();
        if (snapshotTick != tick && !Transaction.isOpen()) {
            List<StorageView<ItemVariant>> views = new ArrayList<>();
            var iterator = storage.iterator();
            while (iterator.hasNext() && views.size() < (1 << 20)) views.add(iterator.next());
            snapshot = views;
            snapshotTick = tick;
        }
        return snapshot;
    }
    private StorageView<ItemVariant> view(int slot) {
        return storage instanceof SlottedStorage<ItemVariant> slotted ? slotted.getSlot(slot) : views().get(slot);
    }
    public Object identity() { return storage; }
    public int getSlots() {
        return storage instanceof SlottedStorage<ItemVariant> slotted ? slotted.getSlotCount() : views().size();
    }
    public ItemStack getStackInSlot(int slot) {
        var view = view(slot);
        return view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }
    @SuppressWarnings("deprecation")
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        var view = view(slot);
        try (Transaction tx = Transaction.openNested(Transaction.getCurrentUnsafe())) {
            long inserted;
            if (view instanceof SingleSlotStorage<ItemVariant> target) {
                inserted = target.insert(ItemVariant.of(stack), stack.getCount(), tx);
            } else {
                // A non-slotted storage cannot target an arbitrary view. Only
                // accept its matching resource through the aggregate API.
                if (!view.isResourceBlank() && !view.getResource().equals(ItemVariant.of(stack))) return stack.copy();
                inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
            }
            if (!simulate) tx.commit();
            return stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }
    @SuppressWarnings("deprecation")
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        var view = view(slot);
        var resource = view.getResource();
        if (resource.isBlank()) return ItemStack.EMPTY;
        try (Transaction tx = Transaction.openNested(Transaction.getCurrentUnsafe())) {
            long extracted = view.extract(resource, amount, tx);
            if (!simulate) tx.commit();
            return resource.toStack((int) extracted);
        }
    }
    public int getSlotLimit(int slot) { return (int) Math.min(Integer.MAX_VALUE, view(slot).getCapacity()); }
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return insertItem(slot, stack.copyWithCount(1), true).isEmpty();
    }
}
