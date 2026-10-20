package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import com.breakinblocks.justenoughcraftingtables.menu.Origin;
import com.breakinblocks.justenoughcraftingtables.menu.OriginEntry;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationOperations;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class NativeTransferTests {
    private static final BlockPos STATION = new BlockPos(3, 1, 3);

    private static StationBlockEntity station(GameTestHelper helper) {
        helper.setBlock(STATION, ModBlocks.CRAFTING_STATION.get());
        return helper.getBlockEntity(STATION, StationBlockEntity.class);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.teleportTo(helper.absolutePos(STATION).getX() + 0.5, helper.absolutePos(STATION).getY(), helper.absolutePos(STATION).getZ() + 1.5);
        player.getInventory().clearContent();
        return player;
    }

    private static void finish(GameTestHelper helper, ServerPlayer player, StationMenu menu) {
        menu.removed(player);
        player.containerMenu = player.inventoryMenu;
        helper.getLevel().getServer().getPlayerList().remove(player);
        helper.succeed();
    }

    public static void simulationAndNestedRollbackPreserveComponents(GameTestHelper helper) {
        ItemStacksResourceHandler nativeStorage = new ItemStacksResourceHandler(1);
        ItemStack named = new ItemStack(Items.DIAMOND, 10);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Transactional diamond"));
        nativeStorage.set(0, ItemResource.of(named), 10);
        ItemStorage storage = ItemStorage.of(nativeStorage);
        helper.assertValueEqual(4, storage.extractItem(0, 4, true).getCount(), "Simulation result");
        helper.assertTrue(ItemStack.matches(named, storage.getStackInSlot(0)), "Simulation mutated native storage");
        try (Transaction outer = Transaction.openRoot()) {
            storage.extractItem(0, 3, false);
            helper.assertValueEqual(7, storage.getStackInSlot(0).getCount(), "Nested extraction was not visible");
            storage.insertItem(0, named.copyWithCount(2), false);
        }
        helper.assertTrue(ItemStack.matches(named, storage.getStackInSlot(0)), "Rollback did not restore count and components");
        storage.getStackInSlot(0).shrink(5);
        helper.assertValueEqual(10, storage.getStackInSlot(0).getCount(), "A returned stack aliased native storage");
        helper.succeed();
    }

    public static void activeMenuFillDefersCommitSynchronization(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 8));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 2));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        player.containerMenu = menu;
        helper.assertValueEqual(StationOperations.FillResult.FILLED,
                StationOperations.fill(menu, List.of(new ItemStack(Items.OAK_PLANKS)), 4), "Active menu fill failed");
        menu.broadcastChanges();
        helper.assertValueEqual(4, station.getGridItem(0).getCount(), "Filled grid count");
        helper.assertValueEqual(6, chest.getItem(0).getCount(), "Connected storage count");
        helper.assertTrue(player.getInventory().getItem(9).isEmpty(), "Player inputs were not consumed");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.PLAYER, 2), new OriginEntry(Origin.NORTH, 2)),
                station.origins().get(0), "Commit synchronization corrupted origins");
        finish(helper, player, menu);
    }

    public static void repeatConnectedFirstWithActiveCreativeMenu(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.OAK_LOG));
        station.setGridItem(0, new ItemStack(Items.OAK_LOG));
        ServerPlayer player = player(helper);
        player.setGameMode(GameType.CREATIVE);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        player.containerMenu = menu;
        menu.clickMenuButton(player, StationMenu.BUTTON_REPEAT_ON);
        menu.clickMenuButton(player, StationMenu.BUTTON_ORDER_CONNECTED);
        menu.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, player);
        int planks = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.OAK_PLANKS)) planks += stack.getCount();
        }
        helper.assertValueEqual(8, planks, "Repeat did not craft the grid log and connected log");
        helper.assertTrue(chest.isEmpty() && station.isGridEmpty(), "Repeat left an available log behind");
        finish(helper, player, menu);
    }

    public static void rejectedReturnsRollbackEntireFill(GameTestHelper helper) {
        checkReturnFailure(helper, false);
    }

    public static void throwingReturnsRollbackEntireFill(GameTestHelper helper) {
        checkReturnFailure(helper, true);
    }

    private static void checkReturnFailure(GameTestHelper helper, boolean throwsException) {
        StationBlockEntity station = station(helper);
        station.setGridItem(0, new ItemStack(Items.DIRT, 4));
        station.origins().set(0, station.getGridItem(0), List.of(new OriginEntry(Origin.NORTH, 4)));
        station.setGridItem(1, new ItemStack(Items.GOLD_INGOT, 4));
        station.origins().set(1, station.getGridItem(1), List.of(new OriginEntry(Origin.NORTH, 4)));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 2));
        TestItemStorage returns = new TestItemStorage(2) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (!simulate && slot == 1) {
                    if (throwsException) throw new IllegalStateException("test return failure");
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
        StationMenu menu = new StationMenu(1, player.getInventory(), station) {
            @Override
            public ItemStorage storeTarget(@Nullable Direction direction) {
                return returns;
            }
        };
        boolean thrown = false;
        try {
            helper.assertValueEqual(StationOperations.FillResult.NO_ROOM,
                    StationOperations.fill(menu, List.of(new ItemStack(Items.OAK_PLANKS)), 1), "Rejected returns were committed");
        } catch (IllegalStateException exception) {
            if (!throwsException) throw exception;
            thrown = true;
        }
        helper.assertTrue(thrown == throwsException, "Wrong exception path");
        helper.assertValueEqual(2, player.getInventory().getItem(9).getCount(), "Input extraction escaped rollback");
        helper.assertTrue(returns.getStackInSlot(0).isEmpty() && returns.getStackInSlot(1).isEmpty(), "A partial return escaped rollback");
        helper.assertTrue(station.getGridItem(0).is(Items.DIRT) && station.getGridItem(0).getCount() == 4
                && station.getGridItem(1).is(Items.GOLD_INGOT) && station.getGridItem(1).getCount() == 4, "Old grid was changed");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.NORTH, 4)), station.origins().get(0), "Old origins were changed");
        finish(helper, player, menu);
    }

    public static void replacementDropsGridExactlyOnce(GameTestHelper helper) {
        checkDrop(helper, ModBlocks.CRAFTING_STATION.get());
        checkDrop(helper, ModBlocks.CRAFTING_STATION_SLAB.get());
        helper.succeed();
    }

    private static void checkDrop(GameTestHelper helper, Block block) {
        helper.setBlock(STATION, block);
        StationBlockEntity station = helper.getBlockEntity(STATION, StationBlockEntity.class);
        station.setGridItem(0, new ItemStack(Items.DIAMOND, 7));
        BlockPos pos = helper.absolutePos(STATION);
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
        helper.assertValueEqual(7, drops.stream().filter(drop -> drop.getItem().is(Items.DIAMOND)).mapToInt(drop -> drop.getItem().getCount()).sum(),
                "Station removal dropped its grid more or less than once");
        drops.forEach(ItemEntity::discard);
        helper.assertTrue(station.isGridEmpty(), "Removed block entity retained droppable items");
    }
}
