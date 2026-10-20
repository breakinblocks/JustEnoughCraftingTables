package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.config.AccessContext;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public final class SideDiscovery {
    @FunctionalInterface
    public interface HandlerQuery {
        @Nullable
        IItemHandler query(Direction direction, BlockPos pos, @Nullable Direction context);
    }

    private SideDiscovery() {
    }

    @Nullable
    public static Direction accessContext(BlockEntity blockEntity, Direction direction) {
        if (ModTags.is(blockEntity.getType(), ModTags.TOUCHING_FACE_ACCESS)) {
            return direction.getOpposite();
        }
        return JustEnoughCraftingTablesConfig.SERVER.accessContext.get() == AccessContext.TOUCHING_FACE ? direction.getOpposite() : null;
    }

    public static List<ServerSide> discover(ServerLevel level, BlockPos stationPos, Player player, StationBlockEntity station, HandlerQuery query) {
        List<ServerSide> accepted = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos pos = stationPos.relative(direction);
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || blockEntity.isRemoved()) {
                continue;
            }
            if (blockEntity instanceof StationBlockEntity) {
                continue;
            }
            if (ModTags.isExcluded(blockEntity.getType())) {
                continue;
            }
            if (!StationAccess.allowed(player, stationPos, blockEntity)) {
                continue;
            }
            IItemHandler handler = query.query(direction, pos, accessContext(blockEntity, direction));
            if (handler == null || handler.getSlots() <= 0) {
                continue;
            }
            boolean duplicate = false;
            for (ServerSide side : accepted) {
                if (identity(side.handler()) == identity(handler)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                continue;
            }
            SideEntry entry = new SideEntry(direction, handler.getSlots(), icon(level, pos), name(level, pos, blockEntity), station.isHidden(direction));
            accepted.add(new ServerSide(direction, pos, blockEntity, handler, entry));
        }
        return accepted;
    }

    private static IItemHandler identity(IItemHandler handler) {
        return handler instanceof AccessItemHandler guarded ? guarded.identity() : handler;
    }

    private static ItemStack icon(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        ItemStack icon = ItemStack.EMPTY;
        try {
            icon = state.getBlock().getCloneItemStack(level, pos, state);
        } catch (RuntimeException exception) {
            JustEnoughCraftingTables.LOGGER.debug("Pick-block item lookup failed at {}", pos, exception);
        }
        if (icon.isEmpty()) {
            icon = new ItemStack(state.getBlock().asItem());
        }
        return icon.isEmpty() ? ItemStack.EMPTY : icon.copyWithCount(1);
    }

    private static Component name(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
        if (blockEntity instanceof Nameable nameable) {
            return nameable.getDisplayName();
        }
        return level.getBlockState(pos).getBlock().getName();
    }
}
