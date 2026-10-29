package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

public final class PlayerStorage {
    private final ItemStorage main;
    private final ItemStorage hotbar;
    private final ItemStorage combined;

    public PlayerStorage(Inventory inventory) {
        InvWrapper wrapper = new InvWrapper(inventory);
        var mainHandler = new RangedWrapper(wrapper, 9, 36);
        main = new NeoStorage(mainHandler);
        var hotbarHandler = new RangedWrapper(wrapper, 0, 9);
        hotbar = new NeoStorage(hotbarHandler);
        combined = new NeoStorage(new CombinedInvWrapper(mainHandler, hotbarHandler));
    }

    public List<Source> sources() {
        return List.of(new Source(Origin.PLAYER, main), new Source(Origin.PLAYER, hotbar));
    }

    public ItemStorage handler() {
        return combined;
    }
}
