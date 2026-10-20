package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

public final class ServerSide {
    private final Direction direction;
    private final BlockPos pos;
    private final BlockEntity blockEntity;
    private final IItemHandler handler;
    private SideEntry entry;
    private boolean invalid;
    private boolean modified;

    public ServerSide(Direction direction, BlockPos pos, BlockEntity blockEntity, IItemHandler handler, SideEntry entry) {
        this.direction = direction;
        this.pos = pos;
        this.blockEntity = blockEntity;
        this.handler = handler;
        this.entry = entry;
    }

    public Direction direction() {
        return direction;
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockEntity blockEntity() {
        return blockEntity;
    }

    public IItemHandler handler() {
        return handler;
    }

    public SideEntry entry() {
        return entry;
    }

    public void setEntry(SideEntry entry) {
        this.entry = entry;
    }

    public boolean usable() {
        return !invalid && !entry.hidden() && !blockEntity.isRemoved();
    }

    public boolean hidden() {
        return entry.hidden();
    }

    public void invalidate() {
        invalid = true;
    }

    public boolean invalid() {
        return invalid || blockEntity.isRemoved();
    }

    public void markModified() {
        modified = true;
    }

    public boolean takeModified() {
        boolean was = modified;
        modified = false;
        return was;
    }
}
