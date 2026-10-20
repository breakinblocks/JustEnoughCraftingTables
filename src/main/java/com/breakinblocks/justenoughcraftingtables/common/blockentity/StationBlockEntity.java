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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

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
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        if (menu instanceof StationMenu stationMenu) {
            stationMenu.writeOpenData(buffer);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < GRID_SIZE; i++) {
            grid.set(i, input.read(KEY_SLOT + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
        int side = input.getIntOr(KEY_CURRENT_CONTAINER, -1);
        selectedSide = side >= 0 && side < 6 ? Direction.from3DDataValue(side) : null;
        customName = input.read(KEY_CUSTOM_NAME, LegacyNameCodec.CODEC).orElse(null);
        hidden.clear();
        int mask = input.getByteOr(KEY_HIDDEN, (byte) 0);
        for (Direction direction : Direction.values()) {
            if ((mask & (1 << direction.get3DDataValue())) != 0) hidden.add(direction);
        }
        origins.load(input.read(KEY_ORIGINS, GridOrigins.CODEC).orElse(List.of()), grid);
        if (input.getInt(KEY_FORMAT).isEmpty()) LEGACY_LOADED.incrementAndGet();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        writeGrid(output);
        if (selectedSide != null) output.putInt(KEY_CURRENT_CONTAINER, selectedSide.get3DDataValue());
        output.storeNullable(KEY_CUSTOM_NAME, LegacyNameCodec.CODEC, customName);
        int mask = 0;
        for (Direction direction : hidden) mask |= 1 << direction.get3DDataValue();
        if (mask != 0) output.putByte(KEY_HIDDEN, (byte) mask);
        output.store(KEY_ORIGINS, GridOrigins.CODEC, origins.save());
        output.putInt(KEY_FORMAT, FORMAT);
    }

    private void writeGrid(ValueOutput output) {
        for (int i = 0; i < GRID_SIZE; i++) {
            output.store(KEY_SLOT + i, ItemStack.OPTIONAL_CODEC, grid.get(i));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        writeGrid(output);
        output.putInt(KEY_FORMAT, FORMAT);
        return output.buildResult();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) Containers.dropContents(level, pos, takeGridForDrop());
        super.preRemoveSideEffects(pos, state);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);
        Component name = componentInput.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            customName = name;
        }
        componentInput.get(DataComponents.BLOCK_STATE);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, customName);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(KEY_CUSTOM_NAME);
    }
}
