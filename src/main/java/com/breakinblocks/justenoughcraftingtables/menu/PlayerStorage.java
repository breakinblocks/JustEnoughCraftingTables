package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

public final class PlayerStorage {
    private final RangedWrapper main;
    private final RangedWrapper hotbar;
    private final IItemHandler combined;

    public PlayerStorage(Inventory inventory) {
        InvWrapper wrapper = new InvWrapper(inventory);
        main = new RangedWrapper(wrapper, 9, 36);
        hotbar = new RangedWrapper(wrapper, 0, 9);
        combined = new CombinedInvWrapper(main, hotbar);
    }

    public List<Source> sources() {
        return List.of(new Source(Origin.PLAYER, main), new Source(Origin.PLAYER, hotbar));
    }

    public IItemHandler handler() {
        return combined;
    }
}
