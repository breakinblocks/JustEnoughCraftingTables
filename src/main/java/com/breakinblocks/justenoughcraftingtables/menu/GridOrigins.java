package com.breakinblocks.justenoughcraftingtables.menu;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public final class GridOrigins {
    public static final int SIZE = 9;
    public static final Codec<List<List<OriginEntry>>> CODEC = OriginEntry.CODEC.listOf().listOf();

    private final List<List<OriginEntry>> slots = new ArrayList<>(SIZE);
    private final ItemStack[] tracked = new ItemStack[SIZE];
    private final int[] trackedCount = new int[SIZE];

    public GridOrigins() {
        for (int i = 0; i < SIZE; i++) {
            slots.add(new ArrayList<>());
            tracked[i] = ItemStack.EMPTY;
        }
    }

    public void reconcile(int slot, ItemStack actual) {
        List<OriginEntry> list = slots.get(slot);
        if (actual.isEmpty()) {
            list.clear();
            tracked[slot] = ItemStack.EMPTY;
            trackedCount[slot] = 0;
            return;
        }
        ItemStack previous = tracked[slot];
        if (previous.isEmpty() || !ItemStack.isSameItemSameTags(previous, actual)) {
            list.clear();
            list.add(new OriginEntry(Origin.PLAYER, actual.getCount()));
            tracked[slot] = actual.copyWithCount(1);
            trackedCount[slot] = actual.getCount();
            return;
        }
        int diff = actual.getCount() - trackedCount[slot];
        if (diff > 0) {
            appendTo(list, Origin.PLAYER, diff);
        } else if (diff < 0) {
            removeNewest(list, -diff);
        }
        trackedCount[slot] = actual.getCount();
    }

    public void reconcileAll(List<ItemStack> grid) {
        for (int i = 0; i < SIZE; i++) {
            reconcile(i, grid.get(i));
        }
    }

    public List<OriginEntry> get(int slot) {
        return List.copyOf(slots.get(slot));
    }

    public void set(int slot, ItemStack stack, List<OriginEntry> entries) {
        List<OriginEntry> list = slots.get(slot);
        list.clear();
        if (stack.isEmpty()) {
            tracked[slot] = ItemStack.EMPTY;
            trackedCount[slot] = 0;
            return;
        }
        for (OriginEntry entry : entries) {
            appendTo(list, entry.origin(), entry.count());
        }
        int total = total(list);
        if (total < stack.getCount()) {
            appendTo(list, Origin.PLAYER, stack.getCount() - total);
        } else if (total > stack.getCount()) {
            removeNewest(list, total - stack.getCount());
        }
        tracked[slot] = stack.copyWithCount(1);
        trackedCount[slot] = stack.getCount();
    }

    public void append(int slot, ItemStack stackAfter, Origin origin, int count) {
        if (count <= 0) {
            return;
        }
        List<OriginEntry> list = slots.get(slot);
        if (tracked[slot].isEmpty() || !ItemStack.isSameItemSameTags(tracked[slot], stackAfter)) {
            list.clear();
            tracked[slot] = stackAfter.copyWithCount(1);
            trackedCount[slot] = 0;
        }
        appendTo(list, origin, count);
        trackedCount[slot] += count;
    }

    public List<OriginEntry> shrink(int slot, int count) {
        List<OriginEntry> removed = removeNewest(slots.get(slot), count);
        trackedCount[slot] -= total(removed);
        if (trackedCount[slot] <= 0) {
            slots.get(slot).clear();
            tracked[slot] = ItemStack.EMPTY;
            trackedCount[slot] = 0;
        }
        return removed;
    }

    @Nullable
    public Origin newest(int slot) {
        List<OriginEntry> list = slots.get(slot);
        return list.isEmpty() ? null : list.get(list.size() - 1).origin();
    }

    public void load(List<List<OriginEntry>> data, List<ItemStack> grid) {
        for (int i = 0; i < SIZE; i++) {
            set(i, grid.get(i), i < data.size() ? data.get(i) : List.of());
        }
    }

    public List<List<OriginEntry>> save() {
        List<List<OriginEntry>> out = new ArrayList<>(SIZE);
        for (List<OriginEntry> list : slots) {
            out.add(List.copyOf(list));
        }
        return out;
    }

    public static void appendTo(List<OriginEntry> list, Origin origin, int count) {
        if (count <= 0) {
            return;
        }
        if (!list.isEmpty() && list.get(list.size() - 1).origin() == origin) {
            OriginEntry last = list.remove(list.size() - 1);
            list.add(last.withCount(last.count() + count));
        } else {
            list.add(new OriginEntry(origin, count));
        }
    }

    public static void appendAll(List<OriginEntry> list, List<OriginEntry> entries) {
        for (OriginEntry entry : entries) {
            appendTo(list, entry.origin(), entry.count());
        }
    }

    public static List<OriginEntry> removeNewest(List<OriginEntry> list, int count) {
        List<OriginEntry> removed = new ArrayList<>();
        int left = count;
        while (left > 0 && !list.isEmpty()) {
            OriginEntry last = list.remove(list.size() - 1);
            if (last.count() > left) {
                list.add(last.withCount(last.count() - left));
                removed.add(0, last.withCount(left));
                left = 0;
            } else {
                removed.add(0, last);
                left -= last.count();
            }
        }
        return removed;
    }

    public static List<OriginEntry> removeOldest(List<OriginEntry> list, int count) {
        List<OriginEntry> removed = new ArrayList<>();
        int left = count;
        while (left > 0 && !list.isEmpty()) {
            OriginEntry first = list.remove(0);
            if (first.count() > left) {
                list.add(0, first.withCount(first.count() - left));
                removed.add(first.withCount(left));
                left = 0;
            } else {
                removed.add(first);
                left -= first.count();
            }
        }
        return removed;
    }

    public static int total(List<OriginEntry> list) {
        int total = 0;
        for (OriginEntry entry : list) {
            total += entry.count();
        }
        return total;
    }
}
