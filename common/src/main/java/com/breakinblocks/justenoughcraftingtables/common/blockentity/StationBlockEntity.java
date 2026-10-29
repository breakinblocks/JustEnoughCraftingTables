package com.breakinblocks.justenoughcraftingtables.common.blockentity;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.GridOrigins;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StationBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    public static final String KEY_FORMAT = JustEnoughCraftingTables.MOD_ID + ":format";
    public static final String KEY_ORIGINS = JustEnoughCraftingTables.MOD_ID + ":origins";
    public static final String KEY_HIDDEN = JustEnoughCraftingTables.MOD_ID + ":hidden";
    public static final String KEY_SLOT = "Slot";
    public static final String KEY_CURRENT_CONTAINER = "CurrentContainer";
    public static final String KEY_CUSTOM_NAME = "CustomName";
    public static final int FORMAT = 1;
    public static final int GRID_SIZE = 9;

    private static final AtomicInteger LEGACY_LOADED = new AtomicInteger();

    private final NonNullList<ItemStack> grid = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
    private final GridOrigins origins = new GridOrigins();
    private final EnumSet<Direction> hidden = EnumSet.noneOf(Direction.class);
    private final Set<StationMenu> menus = Collections.newSetFromMap(new WeakHashMap<>());

    @Nullable
    private Direction selectedSide;

    @Nullable
    private Component customName;

    private int batchDepth;
    private boolean batchChanged;

    public StationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAFTING_STATION.get(), pos, state);
    }

    public static int takeLegacyLoadedCount() {
        return LEGACY_LOADED.getAndSet(0);
    }

    public NonNullList<ItemStack> grid() {
        return grid;
    }

    public GridOrigins origins() {
        return origins;
    }

    public ItemStack getGridItem(int slot) {
        return grid.get(slot);
    }

    public void setGridItem(int slot, ItemStack stack) {
        grid.set(slot, stack);
        gridChanged();
    }

    public boolean isGridEmpty() {
        for (ItemStack stack : grid) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void beginBatch() {
        batchDepth++;
    }

    public void endBatch() {
        if (batchDepth > 0) {
            batchDepth--;
        }
        if (batchDepth == 0 && batchChanged) {
            batchChanged = false;
            gridChanged();
        }
    }

    public void gridChanged() {
        if (batchDepth > 0) {
            batchChanged = true;
            return;
        }
        origins.reconcileAll(grid);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            for (StationMenu menu : List.copyOf(menus)) {
                menu.onGridChanged();
            }
        }
    }

    public NonNullList<ItemStack> takeGridForDrop() {
        NonNullList<ItemStack> drops = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < GRID_SIZE; i++) {
            drops.set(i, grid.get(i));
            grid.set(i, ItemStack.EMPTY);
        }
        origins.reconcileAll(grid);
        return drops;
    }

    public void addMenu(StationMenu menu) {
        menus.add(menu);
    }

    public void removeMenu(StationMenu menu) {
        menus.remove(menu);
    }

    public List<StationMenu> openMenus() {
        return new ArrayList<>(menus);
    }

    public void onNeighbourChanged() {
        for (StationMenu menu : List.copyOf(menus)) {
            menu.requestRediscovery();
        }
    }

    @Nullable
    public Direction getSelectedSide() {
        return selectedSide;
    }

    public void setSelectedSide(@Nullable Direction side) {
        if (selectedSide != side) {
            selectedSide = side;
            setChanged();
        }
    }

    public boolean isHidden(Direction side) {
        return hidden.contains(side);
    }

    public void toggleHidden(Direction side) {
        if (!hidden.remove(side)) {
            hidden.add(side);
        }
        setChanged();
        for (StationMenu menu : List.copyOf(menus)) {
            menu.requestRediscovery();
        }
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(@Nullable Component name) {
        customName = name;
        setChanged();
    }

    @Override
    public Component getName() {
        return customName != null ? customName : getBlockState().getBlock().getName();
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StationMenu(containerId, playerInventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < GRID_SIZE; i++) {
            String key = KEY_SLOT + i;
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                grid.set(i, ItemStack.of(tag.getCompound(key)));
            } else {
                grid.set(i, ItemStack.EMPTY);
            }
        }
        selectedSide = null;
        if (tag.contains(KEY_CURRENT_CONTAINER, Tag.TAG_ANY_NUMERIC)) {
            int value = tag.getInt(KEY_CURRENT_CONTAINER);
            if (value >= 0 && value < 6) {
                selectedSide = Direction.from3DDataValue(value);
            }
        }
        customName = null;
        if (tag.contains(KEY_CUSTOM_NAME, Tag.TAG_STRING)) {
            try {
                customName = Component.Serializer.fromJson(tag.getString(KEY_CUSTOM_NAME));
            } catch (RuntimeException ignored) {
                customName = null;
            }
        }
        hidden.clear();
        if (tag.contains(KEY_HIDDEN, Tag.TAG_ANY_NUMERIC)) {
            int mask = tag.getInt(KEY_HIDDEN);
            for (Direction direction : Direction.values()) {
                if ((mask & (1 << direction.get3DDataValue())) != 0) {
                    hidden.add(direction);
                }
            }
        }
        if (tag.contains(KEY_ORIGINS, Tag.TAG_LIST)) {
            origins.load(
                    GridOrigins.CODEC
                            .parse(NbtOps.INSTANCE, tag.get(KEY_ORIGINS))
                            .resultOrPartial(error -> JustEnoughCraftingTables.LOGGER.warn("Could not read station grid origins at {}: {}", worldPosition, error))
                            .orElse(List.of()),
                    grid);
        } else {
            origins.load(List.of(), grid);
        }
        if (!tag.contains(KEY_FORMAT, Tag.TAG_ANY_NUMERIC)) {
            LEGACY_LOADED.incrementAndGet();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeGrid(tag);
        if (selectedSide != null) {
            tag.putInt(KEY_CURRENT_CONTAINER, selectedSide.get3DDataValue());
        }
        if (customName != null) {
            tag.putString(KEY_CUSTOM_NAME, Component.Serializer.toJson(customName));
        }
        int mask = 0;
        for (Direction direction : hidden) {
            mask |= 1 << direction.get3DDataValue();
        }
        if (mask != 0) {
            tag.putByte(KEY_HIDDEN, (byte) mask);
        }
        GridOrigins.CODEC
                .encodeStart(NbtOps.INSTANCE, origins.save())
                .resultOrPartial(error -> JustEnoughCraftingTables.LOGGER.warn("Could not save station grid origins at {}: {}", worldPosition, error))
                .ifPresent(encoded -> tag.put(KEY_ORIGINS, encoded));
        tag.putInt(KEY_FORMAT, FORMAT);
    }

    private void writeGrid(CompoundTag tag) {
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = grid.get(i);
            tag.put(KEY_SLOT + i, stack.isEmpty() ? new CompoundTag() : stack.save(new CompoundTag()));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        writeGrid(tag);
        tag.putInt(KEY_FORMAT, FORMAT);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
