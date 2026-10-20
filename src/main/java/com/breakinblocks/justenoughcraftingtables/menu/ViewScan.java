package com.breakinblocks.justenoughcraftingtables.menu;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** An incremental filter rebuild. The menu disables mapped slots until it has a complete result. */
final class ViewScan {
    private final IItemHandler handler;
    private final IntOpenHashSet wanted;
    private final IntArrayList matching = new IntArrayList();
    private final int length;
    private int cursor;

    ViewScan(IItemHandler handler, int[] ids) {
        this.handler = handler;
        wanted = new IntOpenHashSet(ids);
        length = handler.getSlots();
        cursor = wanted.isEmpty() ? length : 0;
    }

    boolean advance(SummaryTracker summary, int budget) {
        int end = Math.min(Math.min(length, handler.getSlots()), cursor + budget);
        while (cursor < end) {
            ItemStack stack = handler.getStackInSlot(cursor);
            if (!stack.isEmpty() && wanted.contains(summary.idOf(stack))) {
                matching.add(cursor);
            }
            cursor++;
        }
        return cursor >= Math.min(length, handler.getSlots());
    }

    int[] indexes() {
        return matching.toIntArray();
    }
}
