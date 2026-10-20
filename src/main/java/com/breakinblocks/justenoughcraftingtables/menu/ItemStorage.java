package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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

    static ItemStorage of(ResourceHandler<ItemResource> handler) {
        return new NativeStorage(handler);
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

    record NativeStorage(ResourceHandler<ItemResource> handler) implements ItemStorage {
        @Override
        public Object identity() {
            return handler;
        }

        @Override
        public int getSlots() {
            return handler.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return handler.getResource(slot).toStack((int) Math.min(Integer.MAX_VALUE, handler.getAmountAsLong(slot)));
        }

        @Override
        @SuppressWarnings("deprecation")
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            try (Transaction transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
                if (!simulate) transaction.commit();
                return stack.copyWithCount(stack.getCount() - inserted);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) return ItemStack.EMPTY;
            try (Transaction transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                int extracted = handler.extract(slot, resource, amount, transaction);
                if (!simulate) transaction.commit();
                return resource.toStack(extracted);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return (int) Math.min(Integer.MAX_VALUE, handler.getCapacityAsLong(slot, handler.getResource(slot)));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
        }
    }
}
