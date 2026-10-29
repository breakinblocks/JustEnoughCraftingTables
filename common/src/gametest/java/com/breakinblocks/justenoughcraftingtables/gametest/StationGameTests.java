package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.events.ModBusEvents;
import com.breakinblocks.justenoughcraftingtables.menu.Origin;
import com.breakinblocks.justenoughcraftingtables.menu.OriginEntry;
import com.breakinblocks.justenoughcraftingtables.menu.SourceOrder;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationOperations;
import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import com.breakinblocks.justenoughcraftingtables.registry.ModAliases;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StationGameTests {
    private static final BlockPos STATION = new BlockPos(3, 1, 3);
    private static final int BUTTON_ORDER_CONNECTED = 11;

    private StationGameTests() {
    }

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 centre = helper.absolutePos(STATION.south()).getBottomCenter();
        player.teleportTo(centre.x, centre.y, centre.z);
        player.getInventory().clearContent();
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    private static StationBlockEntity station(GameTestHelper helper) {
        helper.setBlock(STATION, ModBlocks.CRAFTING_STATION.get().defaultBlockState());
        return helper.getBlockEntity(STATION, StationBlockEntity.class);
    }

    private static ChestBlockEntity chest(GameTestHelper helper, Direction side) {
        BlockPos pos = STATION.relative(side);
        helper.setBlock(pos, Blocks.CHEST);
        return helper.getBlockEntity(pos, ChestBlockEntity.class);
    }

    private static int count(ChestBlockEntity chest, ItemStack item) {
        int total = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int count(ServerPlayer player, ItemStack item) {
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static List<ItemStack> pattern(ItemStack... slots) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(i < slots.length && slots[i] != null ? slots[i] : ItemStack.EMPTY);
        }
        return list;
    }

    public static void legacyBlockEntityLoads(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.BLOCK.getValue(ModAliases.legacy("crafting_station")) == ModBlocks.CRAFTING_STATION.get(),
                "Block alias for the full block does not resolve");
        helper.assertTrue(BuiltInRegistries.BLOCK.getValue(ModAliases.legacy("crafting_station_slab")) == ModBlocks.CRAFTING_STATION_SLAB.get(),
                "Block alias for the slab does not resolve");
        helper.assertTrue(BuiltInRegistries.ITEM.getValue(ModAliases.legacy("crafting_station")) == ModBlocks.CRAFTING_STATION.get().asItem(),
                "Item alias does not resolve");
        helper.assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(ModAliases.legacy("crafting_station")) == ModBlockEntities.CRAFTING_STATION.get(),
                "Block entity type alias does not resolve");

        CompoundTag tag = new CompoundTag();
        tag.putString("id", "craftingstation:crafting_station");
        CompoundTag planks = new CompoundTag();
        planks.putString("id", "minecraft:oak_planks");
        planks.putInt("count", 3);
        tag.put("Slot0", planks);
        CompoundTag legacyItem = new CompoundTag();
        legacyItem.putString("id", "craftingstation:crafting_station_slab");
        legacyItem.putInt("count", 2);
        tag.put("Slot4", legacyItem);
        for (int i = 0; i < 9; i++) {
            if (i != 0 && i != 4) {
                tag.put("Slot" + i, new CompoundTag());
            }
        }
        tag.putInt("CurrentContainer", 2);
        tag.putString("CustomName", "{\"text\":\"Old Station\"}");

        BlockPos pos = helper.absolutePos(STATION);
        BlockState state = ModBlocks.CRAFTING_STATION.get().defaultBlockState();
        BlockEntity loaded = BlockEntity.loadStatic(pos, state, tag, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof StationBlockEntity, "Legacy block entity id did not load as a station");
        StationBlockEntity station = (StationBlockEntity) loaded;
        helper.assertTrue(station.getGridItem(0).is(Items.OAK_PLANKS) && station.getGridItem(0).getCount() == 3, "Slot0 was not read");
        helper.assertTrue(station.getGridItem(4).is(ModBlocks.CRAFTING_STATION_SLAB.get().asItem()) && station.getGridItem(4).getCount() == 2,
                "Item id inside Slot4 did not resolve through the item alias");
        helper.assertTrue(station.getGridItem(1).isEmpty(), "Empty compound did not load as an empty stack");
        helper.assertValueEqual(Direction.NORTH, station.getSelectedSide(), "selected side");
        helper.assertTrue(station.getCustomName() != null && station.getCustomName().getString().equals("Old Station"), "CustomName was not read");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.PLAYER, 3)), station.origins().get(0), "origins of a migrated slot");

        CompoundTag saved = station.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.getIntOr(StationBlockEntity.KEY_FORMAT, -1) == StationBlockEntity.FORMAT, "Format marker was not written");
        helper.assertTrue(saved.getCompoundOrEmpty("Slot0").getStringOr("id", "").equals("minecraft:oak_planks"), "Slot0 was not written back");
        helper.assertTrue(saved.getCompoundOrEmpty("Slot1").isEmpty(), "Empty slot was not written as an empty compound");
        helper.assertTrue(saved.getIntOr("CurrentContainer", -1) == 2, "CurrentContainer was not written back");
        helper.assertTrue(saved.contains("CustomName"), "CustomName was not written back");
        helper.succeed();
    }

    public static void fillUsesPlayerFirstOrder(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 5));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 1));
        player.getInventory().setItem(0, new ItemStack(Items.OAK_PLANKS, 1));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        StationOperations.FillResult result = StationOperations.fill(menu, pattern(planks, null, null, planks), 1);
        helper.assertValueEqual(StationOperations.FillResult.FILLED, result, "fill result");
        helper.assertValueEqual(5, count(chest, planks), "planks left in the chest");
        helper.assertValueEqual(0, count(player, planks), "planks left with the player");
        helper.assertTrue(player.getInventory().getItem(9).isEmpty(), "main inventory was not used first");
        helper.assertValueEqual(1, station.getGridItem(0).getCount(), "grid slot 0");
        helper.assertValueEqual(1, station.getGridItem(3).getCount(), "grid slot 3");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.PLAYER, 1)), station.origins().get(0), "origin of grid slot 0");
        helper.assertTrue(menu.resultStack().is(Items.STICK), "result was not recomputed after the fill");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void fillUsesConnectedFirstOrderAndReusesGrid(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 10));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 10));
        station.setGridItem(0, new ItemStack(Items.OAK_PLANKS, 2));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clickMenuButton(player, BUTTON_ORDER_CONNECTED);
        helper.assertValueEqual(SourceOrder.CONNECTED_FIRST, menu.sourceOrder(), "source order");
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        StationOperations.FillResult result = StationOperations.fill(menu, pattern(planks, null, null, planks), 3);
        helper.assertValueEqual(StationOperations.FillResult.FILLED, result, "fill result");
        helper.assertValueEqual(6, count(chest, planks), "planks left in the chest");
        helper.assertValueEqual(10, count(player, planks), "planks left with the player");
        helper.assertValueEqual(3, station.getGridItem(0).getCount(), "grid slot 0");
        helper.assertValueEqual(3, station.getGridItem(3).getCount(), "grid slot 3");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.PLAYER, 2), new OriginEntry(Origin.NORTH, 1)), station.origins().get(0), "origins of grid slot 0");
        helper.assertValueEqual(station.origins().get(3), List.of(new OriginEntry(Origin.NORTH, 3)), "origins of grid slot 3");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void fillMaxSets(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 11));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        StationOperations.FillResult result = StationOperations.fill(menu, pattern(planks, null, null, planks), 0);
        helper.assertValueEqual(StationOperations.FillResult.FILLED, result, "fill result");
        helper.assertValueEqual(5, station.getGridItem(0).getCount(), "grid slot 0");
        helper.assertValueEqual(5, station.getGridItem(3).getCount(), "grid slot 3");
        helper.assertValueEqual(1, count(chest, planks), "planks left in the chest");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void fillIsAllOrNothing(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 4));
        ServerPlayer player = player(helper);
        station.setGridItem(4, new ItemStack(Items.DIRT, 64));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        ItemStack diamond = new ItemStack(Items.DIAMOND);

        StationOperations.FillResult missing = StationOperations.fill(menu, pattern(planks, diamond), 1);
        helper.assertValueEqual(StationOperations.FillResult.NOT_ENOUGH, missing, "fill with a missing ingredient");
        helper.assertValueEqual(4, count(chest, planks), "chest after a failed fill");
        helper.assertValueEqual(64, station.getGridItem(4).getCount(), "grid after a failed fill");

        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        }
        for (int i = 1; i < chest.getContainerSize(); i++) {
            chest.setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        StationOperations.FillResult noRoom = StationOperations.fill(menu, pattern(planks, null, null, planks), 1);
        helper.assertValueEqual(StationOperations.FillResult.NO_ROOM, noRoom, "fill with nowhere for the displaced dirt");
        helper.assertValueEqual(4, count(chest, planks), "chest after a no-room fill");
        helper.assertValueEqual(64, station.getGridItem(4).getCount(), "dirt after a no-room fill");
        helper.assertTrue(station.getGridItem(0).isEmpty(), "grid slot 0 changed after a no-room fill");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void storeReturnsItemsToOrigins(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 8));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.STICK, 5));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.getCarried().is(Items.OAK_PLANKS) && menu.getCarried().getCount() == 8, "picking up from the chest failed");
        menu.clicked(StationMenu.GRID_START, 0, ContainerInput.PICKUP, player);
        helper.assertValueEqual(8, station.getGridItem(0).getCount(), "planks placed in the grid");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.NORTH, 8)), station.origins().get(0), "origin of planks from the chest");
        menu.clicked(StationMenu.INVENTORY_START, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertTrue(station.getGridItem(1).is(Items.STICK), "shift-click did not move sticks into the grid");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.PLAYER, 5)), station.origins().get(1), "origin of sticks from the player");

        menu.clickMenuButton(player, StationMenu.BUTTON_STORE);
        helper.assertValueEqual(8, count(chest, new ItemStack(Items.OAK_PLANKS)), "planks returned to the chest");
        helper.assertValueEqual(5, count(player, new ItemStack(Items.STICK)), "sticks returned to the player");
        helper.assertTrue(station.isGridEmpty(), "grid not empty after store");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void storeLeavesItemsWhoseInventoryIsGone(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_PLANKS, 4));
        station.origins().set(0, station.getGridItem(0), List.of(new OriginEntry(Origin.EAST, 4)));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        int leftover = StationOperations.store(menu);
        helper.assertValueEqual(4, leftover, "items left in the grid");
        helper.assertValueEqual(4, station.getGridItem(0).getCount(), "planks in the grid");
        helper.assertValueEqual(0, count(player, new ItemStack(Items.OAK_PLANKS)), "planks given to the player");
        menu.clickMenuButton(player, StationMenu.BUTTON_CLEAR);
        helper.assertValueEqual(4, count(player, new ItemStack(Items.OAK_PLANKS)), "planks moved by clear");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void remainderKeepsItsOrigin(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        station.setGridItem(0, new ItemStack(Items.MILK_BUCKET));
        station.origins().set(0, station.getGridItem(0), List.of(new OriginEntry(Origin.EAST, 1)));
        station.setGridItem(1, new ItemStack(Items.MILK_BUCKET));
        station.origins().set(1, station.getGridItem(1), List.of(new OriginEntry(Origin.EAST, 1)));
        station.setGridItem(2, new ItemStack(Items.MILK_BUCKET));
        station.origins().set(2, station.getGridItem(2), List.of(new OriginEntry(Origin.EAST, 1)));
        station.setGridItem(3, new ItemStack(Items.SUGAR));
        station.setGridItem(4, new ItemStack(Items.EGG));
        station.setGridItem(5, new ItemStack(Items.SUGAR));
        station.setGridItem(6, new ItemStack(Items.WHEAT));
        station.setGridItem(7, new ItemStack(Items.WHEAT));
        station.setGridItem(8, new ItemStack(Items.WHEAT));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        helper.assertTrue(menu.resultStack().is(Items.CAKE), "cake recipe not matched");
        menu.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertValueEqual(1, count(player, new ItemStack(Items.CAKE)), "cakes crafted");
        helper.assertTrue(station.getGridItem(0).is(Items.BUCKET), "bucket not put back in the grid");
        helper.assertValueEqual(List.of(new OriginEntry(Origin.EAST, 1)), station.origins().get(0), "origin of the empty bucket");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void otherViewersSeeResultChanges(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer first = player(helper);
        ServerPlayer second = player(helper);
        StationMenu menuA = new StationMenu(1, first.getInventory(), station);
        StationMenu menuB = new StationMenu(1, second.getInventory(), station);
        first.getInventory().setItem(9, new ItemStack(Items.OAK_LOG, 1));
        menuA.clicked(StationMenu.INVENTORY_START, 0, ContainerInput.QUICK_MOVE, first);
        helper.assertTrue(menuB.resultStack().is(Items.OAK_PLANKS), "second viewer's result did not update when the grid filled");
        menuA.clicked(StationMenu.GRID_START, 0, ContainerInput.QUICK_MOVE, first);
        helper.assertTrue(menuB.resultStack().isEmpty(), "second viewer kept a stale result");
        menuB.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, second);
        helper.assertTrue(menuB.getCarried().isEmpty(), "second viewer took a result from an empty grid");
        menuA.removed(first);
        menuB.removed(second);
        removePlayer(helper, first);
        removePlayer(helper, second);
        helper.succeed();
    }

    public static void repeatCraftRefillsFromStorage(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_LOG, 5));
        ServerPlayer player = player(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_LOG, 1));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clickMenuButton(player, StationMenu.BUTTON_REPEAT_ON);
        menu.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertValueEqual(24, count(player, new ItemStack(Items.OAK_PLANKS)), "planks crafted");
        helper.assertValueEqual(0, count(chest, new ItemStack(Items.OAK_LOG)), "logs left in the chest");
        helper.assertTrue(station.isGridEmpty(), "grid not empty after repeat craft");

        station.setGridItem(0, new ItemStack(Items.OAK_LOG, 1));
        chest.setItem(0, new ItemStack(Items.OAK_LOG, 5));
        menu.clickMenuButton(player, StationMenu.BUTTON_REPEAT_OFF);
        menu.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertValueEqual(28, count(player, new ItemStack(Items.OAK_PLANKS)), "planks crafted with repeat off");
        helper.assertValueEqual(5, count(chest, new ItemStack(Items.OAK_LOG)), "logs taken with repeat off");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void windowSlotsRespectHandlerRules(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.FURNACE);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        ItemStack cobble = new ItemStack(Items.COBBLESTONE, 16);
        helper.assertTrue(menu.windowSlot(0).mayPlace(cobble), "furnace input refused cobblestone");
        helper.assertTrue(!menu.windowSlot(1).mayPlace(cobble), "furnace fuel slot accepted cobblestone");
        helper.assertTrue(!menu.windowSlot(2).mayPlace(cobble), "furnace output accepted cobblestone");
        menu.setCarried(cobble.copy());
        menu.clicked(StationMenu.WINDOW_START + 2, 0, ContainerInput.PICKUP, player);
        helper.assertValueEqual(16, menu.getCarried().getCount(), "cursor after clicking the output slot");
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty(), "cobblestone not placed in the furnace input");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void slabMergeNeedsMatchingColour(GameTestHelper helper) {
        BlockPos pos = STATION;
        BlockState red = ModBlocks.CRAFTING_STATION_SLAB.get().defaultBlockState()
                .setValue(StationColor.PROPERTY, StationColor.RED)
                .setValue(StationSlabBlock.FACING, Direction.EAST);
        helper.setBlock(pos, red);
        ServerPlayer player = player(helper);
        BlockPos absolute = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(new Vec3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5), Direction.UP, absolute, false);

        ItemStack blue = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.BLUE);
        player.setItemInHand(InteractionHand.MAIN_HAND, blue);
        BlockPlaceContext blueContext = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, blue, hit);
        helper.assertTrue(!blueContext.replacingClickedOnBlock(), "a blue slab merged into a red slab");

        ItemStack redItem = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.RED);
        player.setItemInHand(InteractionHand.MAIN_HAND, redItem);
        BlockPlaceContext redContext = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, redItem, hit);
        helper.assertTrue(redContext.replacingClickedOnBlock(), "a red slab did not merge into a red slab");
        BlockState merged = ModBlocks.CRAFTING_STATION_SLAB.get().getStateForPlacement(redContext);
        helper.assertTrue(merged != null && merged.getValue(StationSlabBlock.TYPE) == SlabType.DOUBLE, "merge did not make a double slab");
        helper.assertValueEqual(Direction.EAST, merged.getValue(StationSlabBlock.FACING), "facing after merge");
        helper.assertValueEqual(StationColor.RED, merged.getValue(StationColor.PROPERTY), "colour after merge");

        ItemStack undyed = new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, undyed);
        BlockPlaceContext undyedContext = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, undyed, hit);
        helper.assertTrue(!undyedContext.replacingClickedOnBlock(), "an undyed slab merged into a red slab");
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void hiddenSideIsNotASource(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 10));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clickMenuButton(player, StationMenu.BUTTON_HIDE_FIRST + Direction.NORTH.get3DDataValue());
        helper.assertTrue(station.isHidden(Direction.NORTH), "north side not hidden");
        helper.runAfterDelay(2, () -> {
            menu.broadcastChanges();
            ItemStack planks = new ItemStack(Items.OAK_PLANKS);
            StationOperations.FillResult result = StationOperations.fill(menu, pattern(planks, null, null, planks), 1);
            helper.assertValueEqual(StationOperations.FillResult.NOT_ENOUGH, result, "fill from a hidden side");
            helper.assertValueEqual(10, count(chest, planks), "planks taken from a hidden side");
            menu.removed(player);
            removePlayer(helper, player);
            helper.succeed();
        });
    }

    private static Optional<RecipeHolder<CraftingRecipe>> recipe(
            GameTestHelper helper, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        return helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
    }

    public static void recipesKeepColour(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, ModAliases.legacy("crafting_station"))).isPresent(),
                "recipe craftingstation:crafting_station is missing");
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, ModAliases.legacy("crafting_station_slab"))).isPresent(),
                "recipe craftingstation:crafting_station_slab is missing");
        ItemStack redStation = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION.get()), StationColor.RED);
        List<ItemStack> toSlabs = pattern(redStation);
        CraftingInput slabInput = CraftingInput.of(3, 3, toSlabs);
        var slabRecipe = recipe(helper, toSlabs);
        helper.assertTrue(slabRecipe.isPresent(), "station to slab recipe not found");
        ItemStack slabs = slabRecipe.get().value().assemble(slabInput);
        helper.assertTrue(slabs.is(ModBlocks.CRAFTING_STATION_SLAB.get().asItem()) && slabs.getCount() == 2, "station did not make 2 slabs");
        helper.assertValueEqual(StationColor.RED, StationColor.of(slabs), "slab colour");

        ItemStack redSlab = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.RED);
        ItemStack blueSlab = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.BLUE);
        List<ItemStack> back = pattern(redSlab, redSlab.copy());
        var backRecipe = recipe(helper, back);
        helper.assertTrue(backRecipe.isPresent(), "slab to station recipe not found");
        ItemStack station = backRecipe.get().value().assemble(CraftingInput.of(3, 3, back));
        helper.assertTrue(station.is(ModBlocks.CRAFTING_STATION.get().asItem()), "two slabs did not make a station");
        helper.assertValueEqual(StationColor.RED, StationColor.of(station), "station colour");
        helper.assertTrue(recipe(helper, pattern(redSlab, blueSlab)).isEmpty(), "slabs of two colours made a station");

        var plain = recipe(helper, pattern(new ItemStack(Items.CRAFTING_TABLE)));
        helper.assertTrue(plain.isPresent(), "crafting table to station recipe not found");
        ItemStack fresh = plain.get().value().assemble(CraftingInput.of(3, 3, pattern(new ItemStack(Items.CRAFTING_TABLE))));
        helper.assertTrue(fresh.getComponentsPatch().isEmpty(), "crafted station carries components");
        helper.succeed();
    }

    public static void cauldronWashesColour(GameTestHelper helper) {
        BlockPos pos = STATION;
        helper.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        ServerPlayer player = player(helper);
        ItemStack dyed = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION.get(), 1), StationColor.GREEN);
        player.setItemInHand(InteractionHand.MAIN_HAND, dyed);
        BlockPos absolute = helper.absolutePos(pos);
        ModBusEvents.WASH_STATION.interact(helper.getLevel().getBlockState(absolute), helper.getLevel(), absolute, player, InteractionHand.MAIN_HAND, dyed);
        helper.assertValueEqual(1, count(player, new ItemStack(ModBlocks.CRAFTING_STATION.get())), "undyed stations after washing");
        ItemStack washed = ItemStack.EMPTY;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModBlocks.CRAFTING_STATION.get().asItem()) && !stack.has(DataComponents.BLOCK_STATE)) {
                washed = stack;
            }
        }
        helper.assertTrue(!washed.isEmpty(), "no washed station found");
        helper.assertValueEqual(2, helper.getLevel().getBlockState(absolute).getValue(LayeredCauldronBlock.LEVEL), "cauldron level");
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void singleSlotDragGoesThroughInsert(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.setCarried(new ItemStack(Items.COBBLESTONE, 10));
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.WINDOW_START, AbstractContainerMenu.getQuickcraftMask(1, 0), ContainerInput.QUICK_CRAFT, player);
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ContainerInput.QUICK_CRAFT, player);
        helper.assertTrue(menu.getCarried().isEmpty(), "cursor not empty after a one-slot drag onto the chest");
        helper.assertValueEqual(10, count(chest, new ItemStack(Items.COBBLESTONE)), "cobblestone in the chest after a one-slot drag");

        menu.setCarried(new ItemStack(Items.DIRT, 3));
        TestPlatform.rightClickWindow(helper, menu, player);
        helper.assertValueEqual(2, menu.getCarried().getCount(), "cursor after a right-click routed through insert");
        helper.assertValueEqual(1, count(chest, new ItemStack(Items.DIRT)), "dirt inserted by a right-click routed through insert");
        menu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void windowShiftClickAndSwapKeepItems(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        ServerPlayer player = player(helper);
        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        }
        player.getInventory().setItem(20, new ItemStack(Items.COBBLESTONE, 60));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.QUICK_MOVE, player);
        helper.assertValueEqual(64, count(player, new ItemStack(Items.COBBLESTONE)), "cobblestone with the player after shift-click");
        helper.assertValueEqual(60, count(chest, new ItemStack(Items.COBBLESTONE)), "cobblestone left in the chest after shift-click");
        menu.removed(player);

        chest.clearContent();
        helper.setBlock(STATION.north(), Blocks.FURNACE);
        player.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE, 16));
        StationMenu furnaceMenu = new StationMenu(2, player.getInventory(), station);
        furnaceMenu.clicked(StationMenu.WINDOW_START + 1, 0, ContainerInput.SWAP, player);
        helper.assertValueEqual(16, player.getInventory().getItem(0).getCount(), "hotbar after a refused swap into the fuel slot");
        helper.assertTrue(furnaceMenu.windowSlot(1).getItem().isEmpty(), "fuel slot accepted cobblestone through a swap");
        furnaceMenu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.SWAP, player);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "hotbar after a swap into the furnace input");
        helper.assertValueEqual(16, furnaceMenu.windowSlot(0).getItem().getCount(), "furnace input after a swap");
        furnaceMenu.removed(player);
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void slabMergeKeepsNameAndGrid(GameTestHelper helper) {
        helper.setBlock(STATION, ModBlocks.CRAFTING_STATION_SLAB.get().defaultBlockState());
        StationBlockEntity station = helper.getBlockEntity(STATION, StationBlockEntity.class);
        station.setCustomName(Component.literal("Workbench"));
        station.setGridItem(0, new ItemStack(Items.DIAMOND, 3));
        ServerPlayer player = player(helper);
        BlockPos absolute = helper.absolutePos(STATION);
        BlockHitResult hit = new BlockHitResult(new Vec3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5), Direction.UP, absolute, false);

        TagValueOutput data = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        data.store("Slot0", ItemStack.CODEC, new ItemStack(Items.GOLD_INGOT, 5));
        ItemStack copied = new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get());
        BlockItem.setBlockEntityData(copied, ModBlockEntities.CRAFTING_STATION.get(), data);
        player.setItemInHand(InteractionHand.MAIN_HAND, copied);
        InteractionResult result = ((BlockItem) copied.getItem()).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, copied, hit));
        helper.assertTrue(result.consumesAction(), "slab item did not merge into the slab");
        helper.assertValueEqual(SlabType.DOUBLE, helper.getBlockState(STATION).getValue(StationSlabBlock.TYPE), "slab type after merge");
        StationBlockEntity merged = helper.getBlockEntity(STATION, StationBlockEntity.class);
        helper.assertTrue(merged == station, "merge replaced the block entity");
        helper.assertTrue(merged.getCustomName() != null && merged.getCustomName().getString().equals("Workbench"), "name lost on merge");
        helper.assertTrue(merged.getGridItem(0).is(Items.DIAMOND) && merged.getGridItem(0).getCount() == 3,
                "item block entity data replaced the grid on merge");

        BlockPos fresh = new BlockPos(6, 1, 6);
        BlockPos freshAbsolute = helper.absolutePos(fresh);
        ItemStack another = copied.copyWithCount(1);
        BlockHitResult freshHit = new BlockHitResult(
                new Vec3(freshAbsolute.getX() + 0.5, freshAbsolute.getY() + 0.1, freshAbsolute.getZ() + 0.5), Direction.UP, freshAbsolute, false);
        ((BlockItem) another.getItem()).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, another, freshHit));
        BlockEntity placed = helper.getLevel().getBlockEntity(freshAbsolute);
        helper.assertTrue(placed instanceof StationBlockEntity fresher && fresher.getGridItem(0).is(Items.GOLD_INGOT),
                "item block entity data was not applied to a newly placed slab");
        removePlayer(helper, player);
        helper.succeed();
    }

    public static void slabOfOtherColourPlacesInNextBlock(GameTestHelper helper) {
        BlockState red = ModBlocks.CRAFTING_STATION_SLAB.get().defaultBlockState()
                .setValue(StationColor.PROPERTY, StationColor.RED)
                .setValue(StationSlabBlock.FACING, Direction.EAST);
        helper.setBlock(STATION, red);
        StationBlockEntity station = helper.getBlockEntity(STATION, StationBlockEntity.class);
        station.setGridItem(0, new ItemStack(Items.DIAMOND, 3));
        ServerPlayer player = player(helper);
        BlockPos absolute = helper.absolutePos(STATION);
        BlockHitResult hit = new BlockHitResult(
                new Vec3(absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5), Direction.UP, absolute, false);

        ItemStack blue = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.BLUE);
        player.setItemInHand(InteractionHand.MAIN_HAND, blue);
        InteractionResult result = player.gameMode.useItemOn(player, helper.getLevel(), blue, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction(), "blue slab was not placed");
        helper.assertTrue(player.containerMenu == player.inventoryMenu, "the station screen opened instead of placing");
        BlockState original = helper.getBlockState(STATION);
        helper.assertTrue(original.is(ModBlocks.CRAFTING_STATION_SLAB.get()), "original slab replaced");
        helper.assertValueEqual(SlabType.BOTTOM, original.getValue(StationSlabBlock.TYPE), "original slab type");
        helper.assertValueEqual(StationColor.RED, original.getValue(StationColor.PROPERTY), "original slab colour");
        helper.assertValueEqual(Direction.EAST, original.getValue(StationSlabBlock.FACING), "original slab facing");
        StationBlockEntity kept = helper.getBlockEntity(STATION, StationBlockEntity.class);
        helper.assertTrue(kept == station && kept.getGridItem(0).is(Items.DIAMOND) && kept.getGridItem(0).getCount() == 3,
                "original slab grid changed");
        BlockState placed = helper.getBlockState(STATION.above());
        helper.assertTrue(placed.is(ModBlocks.CRAFTING_STATION_SLAB.get()), "no slab in the next block");
        helper.assertValueEqual(StationColor.BLUE, placed.getValue(StationColor.PROPERTY), "placed slab colour");

        BlockPos blocked = new BlockPos(1, 1, 1);
        helper.setBlock(blocked, red);
        helper.setBlock(blocked.above(), Blocks.STONE);
        BlockPos blockedAbsolute = helper.absolutePos(blocked);
        BlockHitResult blockedHit = new BlockHitResult(
                new Vec3(blockedAbsolute.getX() + 0.5, blockedAbsolute.getY() + 0.5, blockedAbsolute.getZ() + 0.5),
                Direction.UP, blockedAbsolute, false);
        ItemStack green = StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), StationColor.GREEN);
        player.setItemInHand(InteractionHand.MAIN_HAND, green);
        InteractionResult blockedResult = helper.getBlockState(blocked)
                .useItemOn(green, helper.getLevel(), player, InteractionHand.MAIN_HAND, blockedHit);
        helper.assertValueEqual(InteractionResult.TRY_WITH_EMPTY_HAND, blockedResult, "the station screen was skipped when nothing could be placed");
        removePlayer(helper, player);
        helper.succeed();
    }

    private static ViewPayload.Request viewRequest(Direction side, boolean emptyFilter, boolean rebuild) {
        return new ViewPayload.Request(Optional.of(side), 0, 0, emptyFilter ? Optional.of(new int[0]) : Optional.empty(), rebuild);
    }

    public static void viewRebuildsAreCoalesced(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ChestBlockEntity chest = chest(helper, Direction.NORTH);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 10));
        helper.setBlock(STATION.east(), Blocks.BARREL);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        int version = menu.sidesVersion();

        menu.handleViewRequest(1, version, viewRequest(Direction.NORTH, true, true));
        helper.assertValueEqual(Direction.NORTH, menu.selectedSide(), "side after the first rebuild");
        helper.assertValueEqual(-1, menu.windowIndex(0), "window after the first rebuild");
        menu.handleViewRequest(2, version, viewRequest(Direction.EAST, false, true));
        menu.handleViewRequest(3, version, viewRequest(Direction.NORTH, true, true));
        menu.handleViewRequest(4, version, viewRequest(Direction.EAST, false, false));
        helper.assertValueEqual(Direction.NORTH, menu.selectedSide(), "a second rebuild applied in the same tick");
        helper.assertValueEqual(-1, menu.windowIndex(0), "window changed by a second rebuild in the same tick");

        helper.runAfterDelay(1, () -> {
            menu.broadcastChanges();
            helper.assertValueEqual(Direction.EAST, menu.selectedSide(), "side after the coalesced rebuild");
            helper.assertValueEqual(0, menu.windowIndex(0), "window after the coalesced rebuild");
            menu.handleViewRequest(5, version, viewRequest(Direction.NORTH, false, false));
            helper.assertValueEqual(Direction.EAST, menu.selectedSide(), "a second plain view request applied in the same tick");
            menu.handleViewRequest(6, version, viewRequest(Direction.NORTH, true, true));
            helper.assertValueEqual(0, menu.windowIndex(0), "a rebuild applied twice in one tick");
            helper.runAfterDelay(1, () -> {
                menu.broadcastChanges();
                helper.assertValueEqual(-1, menu.windowIndex(0), "deferred rebuild never applied");
                menu.removed(player);
                removePlayer(helper, player);
                helper.succeed();
            });
        });
    }

    public static void fuelValues(GameTestHelper helper) {
        helper.assertValueEqual(300, helper.getLevel().fuelValues().burnDuration(new ItemStack(ModBlocks.CRAFTING_STATION.get())), "station burn time");
        helper.assertValueEqual(150, helper.getLevel().fuelValues().burnDuration(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get())), "slab burn time");
        helper.succeed();
    }
}
