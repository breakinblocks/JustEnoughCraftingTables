package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Holds extracted inputs until the entire replacement grid and displaced-item returns have been planned. */
final class InventoryTransaction implements AutoCloseable {
    private record Taken(PlannedHandler planner, int slot, List<ItemStack> stacks) {
    }

    private final StationMenu menu;
    private final List<Taken> taken = new ArrayList<>();
    private boolean committed;

    InventoryTransaction(StationMenu menu) {
        this.menu = menu;
    }

    List<ItemStack> extract(PlannedHandler planner, int slot, ItemStack template, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        taken.add(new Taken(planner, slot, stacks));
        if (planner.origin().side() != null) {
            menu.markSideModified(planner.origin().side());
        }
        planner.executeExtract(slot, template, count, stacks);
        return stacks;
    }

    void commit() {
        committed = true;
        taken.clear();
    }

    void rollback() {
        for (int i = taken.size() - 1; i >= 0; i--) {
            Taken items = taken.get(i);
            for (ItemStack stack : items.stacks()) {
                ItemStack remainder = items.planner().handler().insertItem(items.slot(), stack, false);
                if (!remainder.isEmpty()) {
                    remainder = ItemHandlerHelper.insertItemStacked(items.planner().handler(), remainder, false);
                }
                if (!remainder.isEmpty()) {
                    menu.player().getInventory().placeItemBackInInventory(remainder);
                }
            }
        }
        taken.clear();
    }

    @Override
    public void close() {
        if (!committed) {
            rollback();
        }
    }
}
