package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class StationResultSlot extends ResultSlot {
    private final StationMenu menu;
    private final CraftingContainer grid;
    private final Player player;

    public StationResultSlot(StationMenu menu, Player player, CraftingContainer grid, Container result, int x, int y) {
        super(player, grid, result, 0, x, y);
        this.menu = menu;
        this.grid = grid;
        this.player = player;
    }

    @Override
    public boolean mayPickup(Player player) {
        return menu.isClientSide() || menu.verifyResult();
    }

    @Override
    public void onTake(Player taker, ItemStack stack) {
        checkTakeAchievements(stack);
        if (menu.isClientSide()) {
            return;
        }
        CraftingContainer input = grid;
        int left = 0;
        int top = 0;
        ForgeHooks.setCraftingPlayer(taker);
        NonNullList<ItemStack> remaining;
        try {
            remaining = menu.remainingItems(input);
        } finally {
            ForgeHooks.setCraftingPlayer(null);
        }
        menu.beginGridBatch();
        try {
            for (int row = 0; row < input.getHeight(); row++) {
                for (int col = 0; col < input.getWidth(); col++) {
                    int slot = col + left + (row + top) * grid.getWidth();
                    ItemStack current = grid.getItem(slot);
                    ItemStack remainder = remaining.get(col + row * input.getWidth());
                    Origin consumedOrigin = Origin.PLAYER;
                    if (!current.isEmpty()) {
                        Origin newest = menu.gridOrigins().newest(slot);
                        if (newest != null) {
                            consumedOrigin = newest;
                        }
                        grid.removeItem(slot, 1);
                        menu.gridOrigins().shrink(slot, 1);
                        current = grid.getItem(slot);
                    }
                    if (remainder.isEmpty()) {
                        continue;
                    }
                    if (current.isEmpty()) {
                        grid.setItem(slot, remainder);
                        menu.gridOrigins().append(slot, remainder, consumedOrigin, remainder.getCount());
                    } else if (ItemStack.isSameItemSameTags(current, remainder)) {
                        int added = remainder.getCount();
                        remainder.grow(current.getCount());
                        grid.setItem(slot, remainder);
                        menu.gridOrigins().append(slot, remainder, consumedOrigin, added);
                    } else if (!player.getInventory().add(remainder)) {
                        player.drop(remainder, false);
                    }
                }
            }
        } finally {
            menu.endGridBatch();
        }
    }
}
