package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.entity.player.Inventory;

public final class PlayerStorage {
    private final ItemStorage main;
    private final ItemStorage hotbar;
    private final ItemStorage combined;
    public PlayerStorage(Inventory inventory) {
        var slots = PlayerInventoryStorage.of(inventory).getSlots();
        var mainSlots = slots.subList(9, 36);
        var hotbarSlots = slots.subList(0, 9);
        main = new FabricStorage(new CombinedSlottedStorage<>(mainSlots));
        hotbar = new FabricStorage(new CombinedSlottedStorage<>(hotbarSlots));
        List<SingleSlotStorage<ItemVariant>> ordered = new ArrayList<>(mainSlots);
        ordered.addAll(hotbarSlots);
        combined = new FabricStorage(new CombinedSlottedStorage<>(ordered));
    }
    public List<Source> sources() { return List.of(new Source(Origin.PLAYER, main), new Source(Origin.PLAYER, hotbar)); }
    public ItemStorage handler() { return combined; }
}
