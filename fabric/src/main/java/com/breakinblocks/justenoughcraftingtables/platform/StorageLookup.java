package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.menu.FabricStorage;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public final class StorageLookup {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final BooleanSupplier valid;
    private final Runnable invalidated;
    private Storage<ItemVariant> last;
    private FabricStorage wrapped;
    public StorageLookup(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier valid, Runnable invalidated) {
        this.level = level; this.pos = pos; this.side = side; this.valid = valid; this.invalidated = invalidated;
    }
    public FabricStorage get() {
        if (!valid.getAsBoolean()) return null;
        var raw = ItemStorage.SIDED.find(level, pos, side);
        if (!sameStorage(raw, last)) {
            invalidated.run();
            last = raw;
            wrapped = raw == null ? null : new FabricStorage(raw, level::getGameTime);
        }
        return wrapped;
    }

    public boolean isValid() {
        if (!valid.getAsBoolean()) return false;
        if (!sameStorage(ItemStorage.SIDED.find(level, pos, side), last)) {
            invalidated.run();
            return false;
        }
        return true;
    }

    private static boolean sameStorage(Storage<ItemVariant> current, Storage<ItemVariant> previous) {
        if (current == previous) return true;
        // Fabric recreates the double-chest wrapper on every lookup, but caches its two parts.
        // Restrict this to the plain wrapper: subclasses may have additional access semantics.
        if (current == null || previous == null
                || current.getClass() != CombinedSlottedStorage.class
                || previous.getClass() != CombinedSlottedStorage.class) return false;
        var currentParts = ((CombinedSlottedStorage<?, ?>) current).parts;
        var previousParts = ((CombinedSlottedStorage<?, ?>) previous).parts;
        if (currentParts.size() != previousParts.size()) return false;
        for (int i = 0; i < currentParts.size(); i++) {
            if (currentParts.get(i) != previousParts.get(i)) return false;
        }
        return true;
    }
}
