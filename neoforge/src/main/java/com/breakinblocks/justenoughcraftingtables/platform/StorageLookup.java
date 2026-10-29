package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class StorageLookup {
    private final BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache;
    private ResourceHandler<ItemResource> last;
    private ItemStorage wrapped;
    public StorageLookup(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier valid, Runnable invalidated) {
        cache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, side, valid, invalidated);
    }
    public ItemStorage get() {
        var raw = cache.getCapability();
        if (raw != last) { last = raw; wrapped = raw == null ? null : ItemStorage.of(raw); }
        return wrapped;
    }
    public boolean isValid() { return cache.getCapability() == last; }
}
