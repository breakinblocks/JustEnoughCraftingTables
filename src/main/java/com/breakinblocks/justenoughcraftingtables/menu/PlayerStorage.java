package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

public final class PlayerStorage {
    private final ItemStorage main;
    private final ItemStorage hotbar;
    private final ItemStorage combined;

    public PlayerStorage(Inventory inventory) {
        PlayerInventoryWrapper wrapper = PlayerInventoryWrapper.of(inventory);
        var mainSlots = RangedResourceHandler.of(wrapper, 9, 36);
        var hotbarSlots = RangedResourceHandler.of(wrapper, 0, 9);
        main = ItemStorage.of(mainSlots);
        hotbar = ItemStorage.of(hotbarSlots);
        combined = ItemStorage.of(new CombinedResourceHandler<>(mainSlots, hotbarSlots));
    }

    public List<Source> sources() {
        return List.of(new Source(Origin.PLAYER, main), new Source(Origin.PLAYER, hotbar));
    }

    public ItemStorage handler() {
        return combined;
    }
}
