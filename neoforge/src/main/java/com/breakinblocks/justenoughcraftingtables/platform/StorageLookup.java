package com.breakinblocks.justenoughcraftingtables.platform;
import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import com.breakinblocks.justenoughcraftingtables.menu.NeoStorage;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
public final class StorageLookup {
    private final BlockCapabilityCache<IItemHandler, Direction> cache;
    private IItemHandler last;
    private ItemStorage wrapped;
    public StorageLookup(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier valid, Runnable invalidated) {
        cache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, pos, side, valid, invalidated);
    }
    public ItemStorage get() {
        var raw = cache.getCapability();
        if (raw != last) { last = raw; wrapped = raw == null ? null : new NeoStorage(raw); }
        return wrapped;
    }
    public boolean isValid() { return cache.getCapability() == last; }
}
