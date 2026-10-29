package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.menu.ForgeStorage;
import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
public final class StorageLookup {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final BooleanSupplier valid;
    private final Runnable invalidated;
    private LazyOptional<IItemHandler> capability = LazyOptional.empty();
    private BlockEntity inventory;
    private IItemHandler last;
    private ItemStorage wrapped;
    public StorageLookup(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier valid, Runnable invalidated) {
        this.level=level; this.pos=pos; this.side=side; this.valid=valid; this.invalidated=invalidated;
    }
    public ItemStorage get() {
        if (!valid.getAsBoolean()) return null;
        BlockEntity current=level.getBlockEntity(pos);
        if (current!=inventory || !capability.isPresent()) {
            inventory=current;
            capability=current==null ? LazyOptional.empty() : current.getCapability(ForgeCapabilities.ITEM_HANDLER,side);
            LazyOptional<IItemHandler> bound=capability;
            capability.addListener(ignored -> { if(valid.getAsBoolean() && capability==bound) invalidated.run(); });
        }
        IItemHandler raw=capability.orElse(null);
        if(raw!=last) { last=raw; wrapped=raw==null ? null : new ForgeStorage(raw); }
        return wrapped;
    }
    public boolean isValid() { return valid.getAsBoolean() && level.getBlockEntity(pos)==inventory && capability.orElse(null)==last; }
}
