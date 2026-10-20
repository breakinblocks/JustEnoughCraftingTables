package com.breakinblocks.justenoughcraftingtables.menu;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class PlannedHandler {
    private final IItemHandler handler;
    private final Origin origin;
    private final Int2ObjectLinkedOpenHashMap<ItemStack> inserts = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2IntLinkedOpenHashMap extracts = new Int2IntLinkedOpenHashMap();

    public PlannedHandler(IItemHandler handler, Origin origin) {
        this.handler = handler;
        this.origin = origin;
    }

    public IItemHandler handler() {
        return handler;
    }

    public Origin origin() {
        return origin;
    }

    public int planInsertStacked(ItemStack stack, int count) {
        int left = count;
        int slots = handler.getSlots();
        if (stack.isStackable()) {
            for (int slot = 0; slot < slots && left > 0; slot++) {
                ItemStack planned = inserts.get(slot);
                ItemStack current = handler.getStackInSlot(slot);
                boolean matches = planned != null
                        ? ItemStack.isSameItemSameComponents(planned, stack)
                        : !current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack);
                if (matches) {
                    left -= acceptMore(slot, stack, left);
                }
            }
        }
        for (int slot = 0; slot < slots && left > 0; slot++) {
            if (inserts.containsKey(slot) || !handler.getStackInSlot(slot).isEmpty()) {
                continue;
            }
            left -= acceptMore(slot, stack, left);
        }
        return count - left;
    }

    private int acceptMore(int slot, ItemStack stack, int count) {
        ItemStack planned = inserts.get(slot);
        int already = planned == null ? 0 : planned.getCount();
        ItemStack remainder = handler.insertItem(slot, stack.copyWithCount(already + count), true);
        int total = already + count - remainder.getCount();
        int added = Math.max(0, Math.min(count, total - already));
        if (added > 0) {
            inserts.put(slot, stack.copyWithCount(already + added));
        }
        return added;
    }

    public int planExtract(int slot, ItemStack template, int count) {
        if (count <= 0) {
            return 0;
        }
        ItemStack current = handler.getStackInSlot(slot);
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, template)) {
            return 0;
        }
        if (extracts.containsKey(slot)) {
            return 0;
        }
        int wanted = Math.min(count, current.getCount());
        int probeSize = Math.min(wanted, Math.max(1, template.getMaxStackSize()));
        ItemStack probe = handler.extractItem(slot, probeSize, true);
        if (probe.isEmpty() || !ItemStack.isSameItemSameComponents(probe, template)) {
            return 0;
        }
        if (probe.getCount() < probeSize) {
            wanted = probe.getCount();
        }
        extracts.put(slot, wanted);
        return wanted;
    }

    public void executeExtract(int slot, ItemStack template, int count, List<ItemStack> taken) {
        int left = count;
        int perCall = Math.max(1, template.getMaxStackSize());
        while (left > 0) {
            ItemStack extracted = handler.extractItem(slot, Math.min(left, perCall), false);
            if (extracted.isEmpty()) {
                break;
            }
            if (!ItemStack.isSameItemSameComponents(extracted, template)) {
                taken.add(extracted);
                break;
            }
            taken.add(extracted);
            left -= extracted.getCount();
        }
    }

    public List<ItemStack> executeInserts() {
        List<ItemStack> leftovers = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : inserts.int2ObjectEntrySet()) {
            ItemStack remainder = handler.insertItem(entry.getIntKey(), entry.getValue().copy(), false);
            if (!remainder.isEmpty()) {
                leftovers.add(remainder);
            }
        }
        inserts.clear();
        return leftovers;
    }

    public boolean hasInserts() {
        return !inserts.isEmpty();
    }
}
