package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import java.util.List;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class StationGrid implements CraftingContainer {
    private final StationBlockEntity station;

    public StationGrid(StationBlockEntity station) {
        this.station = station;
    }

    public StationBlockEntity station() {
        return station;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.copyOf(station.grid());
    }

    @Override
    public int getContainerSize() {
        return StationBlockEntity.GRID_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return station.isGridEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return station.getGridItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(station.grid(), slot, amount);
        if (!removed.isEmpty()) {
            station.gridChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(station.grid(), slot);
        station.gridChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        station.setGridItem(slot, stack);
    }

    @Override
    public void setChanged() {
        station.gridChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return !station.isRemoved();
    }

    @Override
    public void clearContent() {
        station.grid().replaceAll(ignored -> ItemStack.EMPTY);
        station.gridChanged();
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        for (ItemStack stack : station.grid()) {
            contents.accountSimpleStack(stack);
        }
    }
}
