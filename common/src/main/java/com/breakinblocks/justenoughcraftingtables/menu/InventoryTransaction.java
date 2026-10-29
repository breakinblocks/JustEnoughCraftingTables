package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.justenoughcraftingtables.platform.StorageTransaction;

final class InventoryTransaction implements AutoCloseable {
    private final StorageTransaction transaction = StorageTransaction.openRoot();
    private final StationMenu menu;

    InventoryTransaction(StationMenu menu) { this.menu = menu; }

    List<ItemStack> extract(PlannedHandler planner, int slot, ItemStack template, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        if (planner.origin().side() != null) menu.markSideModified(planner.origin().side());
        planner.executeExtract(slot, template, count, stacks);
        return stacks;
    }

    void commit() { transaction.commit(); }
    void rollback() { transaction.close(); }

    @Override
    public void close() {
        transaction.close();
    }
}
