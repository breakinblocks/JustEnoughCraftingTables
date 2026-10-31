package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.gametest.TestItemStorage;
import com.breakinblocks.justenoughcraftingtables.integration.RecipeFill;
import com.breakinblocks.justenoughcraftingtables.menu.ClientSummary;
import com.breakinblocks.justenoughcraftingtables.menu.ItemStorage;
import com.breakinblocks.justenoughcraftingtables.menu.Origin;
import com.breakinblocks.justenoughcraftingtables.menu.Source;
import com.breakinblocks.justenoughcraftingtables.menu.SourceOrder;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationOperations;
import com.breakinblocks.justenoughcraftingtables.menu.SummaryTracker;
import com.breakinblocks.justenoughcraftingtables.net.FillRecipePayload;
import com.breakinblocks.justenoughcraftingtables.net.NetCodecs;
import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

// Regression coverage for access control, recipe reloads, transfer planning and inventory summaries.
public final class StationRegressionTests {
    private static final BlockPos STATION = new BlockPos(3, 1, 3);

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 centre = Vec3.atBottomCenterOf(helper.absolutePos(STATION.south()));
        player.teleportTo(centre.x, centre.y, centre.z);
        player.getInventory().clearContent();
        return player;
    }

    private static StationBlockEntity station(GameTestHelper helper) {
        helper.setBlock(STATION, ModBlocks.CRAFTING_STATION.get());
        return helper.getBlockEntity(STATION, StationBlockEntity.class);
    }

    private static void finish(GameTestHelper helper, ServerPlayer player, StationMenu menu) {
        menu.removed(player);
        helper.getLevel().getServer().getPlayerList().remove(player);
        helper.succeed();
    }

    public static void externalWindowWritesKeepItems(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 20));
        chest.setItem(1, new ItemStack(Items.COBBLESTONE, 30));
        chest.setItem(2, new ItemStack(Items.DIRT, 12));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        helper.assertTrue(menu.windowSlot(0).getItem().getCount() == 20, "Window setup failed");

        // Clear, shrink, merge, replace and write into an empty virtual slot, as sorters do.
        menu.windowSlot(0).set(ItemStack.EMPTY);
        menu.windowSlot(1).set(new ItemStack(Items.COBBLESTONE, 10));
        menu.windowSlot(0).set(new ItemStack(Items.COBBLESTONE, 50));
        menu.windowSlot(2).set(new ItemStack(Items.DIAMOND, 12));
        menu.windowSlot(3).set(new ItemStack(Items.DIRT, 12));
        menu.windowSlot(0).container.setItem(0, ItemStack.EMPTY);
        menu.setWindowStack(menu.windowSlot(1), ItemStack.EMPTY);
        helper.assertTrue(chest.getItem(0).is(Items.COBBLESTONE) && chest.getItem(0).getCount() == 20,
                "External write changed the first stack");
        helper.assertTrue(chest.getItem(1).is(Items.COBBLESTONE) && chest.getItem(1).getCount() == 30,
                "External write changed the second stack");
        helper.assertTrue(chest.getItem(2).is(Items.DIRT) && chest.getItem(2).getCount() == 12,
                "External write replaced an item");
        helper.assertTrue(chest.getItem(3).isEmpty(), "External write created items");
        helper.assertTrue(player.getInventory().isEmpty() && menu.getCarried().isEmpty(),
                "External write refunded copied items");
        finish(helper, player, menu);
    }

    public static void lockedChestIsNotExposed(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        CompoundTag tag = new CompoundTag();
        CompoundTag lock = new CompoundTag();
        lock.putString("items", "minecraft:tripwire_hook");
        tag.put("lock", lock);
        chest.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        ServerPlayer player = player(helper);
        helper.assertTrue(!chest.canOpen(player), "Probe player unexpectedly has the chest key");
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty() && chest.getItem(0).is(Items.DIAMOND), "Locked chest was exposed");
        finish(helper, player, menu);
    }

    public static void spectatorLeavesLootUngenerated(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        Player player = helper.makeMockPlayer(GameType.SPECTATOR);
        Vec3 centre = Vec3.atBottomCenterOf(helper.absolutePos(STATION.south()));
        player.setPos(centre.x, centre.y, centre.z);
        helper.assertTrue(!chest.canOpen(player), "Spectator unexpectedly allowed to open ungenerated loot");
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        helper.assertTrue(chest.getLootTable() != null, "Loot unexpectedly generated before scan");
        menu.broadcastChanges();
        helper.assertTrue(chest.getLootTable() != null, "Spectator generated unopened loot");
        menu.removed(player);
        helper.succeed();
    }

    public static void removedRecipeCannotBeCrafted(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_LOG));
        ServerPlayer player = player(helper);
        RecipeManager manager = helper.getLevel().getServer().getRecipeManager();
        List<RecipeHolder<?>> original = List.copyOf(manager.getRecipes());
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        RecipeHolder<CraftingRecipe> recipe = manager.getRecipeFor(RecipeType.CRAFTING, menu.craftingGrid().asCraftInput(), helper.getLevel()).orElseThrow();
        try {
            replaceRecipes(manager, original.stream().filter(holder -> !holder.id().equals(recipe.id())).toList());
            helper.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING, menu.craftingGrid().asCraftInput(), helper.getLevel()).isEmpty(), "Recipe removal setup failed");
            helper.assertTrue(!menu.verifyResult(), "Removed recipe passed verification");
            menu.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);
            helper.assertTrue(menu.getCarried().isEmpty() && station.getGridItem(0).is(Items.OAK_LOG), "Removed recipe was crafted");
        } finally {
            replaceRecipes(manager, original);
        }
        finish(helper, player, menu);
    }

    public static void fillUsesSpaceFreedByExtraction(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.setGridItem(4, new ItemStack(Items.DIRT, 64));
        ServerPlayer player = player(helper);
        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        player.getInventory().setItem(9, new ItemStack(Items.OAK_LOG));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        StationOperations.FillResult result = StationOperations.fill(menu, List.of(new ItemStack(Items.OAK_LOG)), 1);
        helper.assertValueEqual(StationOperations.FillResult.FILLED, result, "Fill should use space freed by extraction");
        finish(helper, player, menu);
    }

    public static void overlappingIngredientsAreAssigned(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_PLANKS));
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.BIRCH_PLANKS));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        RecipeFill.Requirement flexible = new RecipeFill.Requirement(
                Set.of(Items.OAK_PLANKS, Items.BIRCH_PLANKS), stack -> stack.is(Items.OAK_PLANKS) || stack.is(Items.BIRCH_PLANKS));
        RecipeFill.Requirement oakOnly = new RecipeFill.Requirement(Set.of(Items.OAK_PLANKS), stack -> stack.is(Items.OAK_PLANKS));
        RecipeFill.Plan plan = RecipeFill.plan(menu, List.of(flexible, oakOnly));
        helper.assertTrue(plan.complete() && plan.templates().get(0).is(Items.BIRCH_PLANKS) && plan.templates().get(1).is(Items.OAK_PLANKS), "Valid ingredient assignment was rejected");
        finish(helper, player, menu);
    }

    public static void summaryDictionaryStaysBounded(GameTestHelper helper) {
        TestItemStorage handler = new TestItemStorage(1);
        SummaryTracker tracker = new SummaryTracker();
        tracker.bind(Direction.NORTH, handler);
        int entries = 0;
        for (int i = 0; i < 40; i++) {
            ItemStack stack = new ItemStack(Items.PAPER);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("version-" + i));
            handler.setStackInSlot(0, stack);
            tracker.tick(1, 1);
            SummaryPayload payload = tracker.drain(1);
            if (payload != null) {
                entries += payload.entries().size();
            }
            helper.assertTrue(tracker.entryCount() <= 1, "Dictionary exceeded its configured bound");
        }
        helper.assertTrue(entries > 1, "Dictionary stopped accepting new variants after retirement");
        helper.succeed();
    }

    public static void filteredViewRejectsReplacedStacks(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.broadcastChanges();
        ViewPayload.Request request = new ViewPayload.Request(Optional.of(Direction.NORTH), 0, 0, Optional.of(new int[] {0}), true);
        menu.handleViewRequest(1, menu.sidesVersion(), request);
        helper.assertTrue(menu.windowSlot(0).getItem().is(Items.OAK_PLANKS), "Initial filter setup failed");
        chest.setItem(0, new ItemStack(Items.DIRT));
        chest.setItem(1, new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(menu.windowSlot(0).getItem().isEmpty(), "Non-matching replacement leaked into filter");
        helper.runAfterDelay(2, () -> {
            menu.handleViewRequest(2, menu.sidesVersion(), request);
            helper.assertValueEqual(1, menu.windowIndex(0), "Moved matching item was not remapped");
            finish(helper, player, menu);
        });
    }

    public static void summarySeparatesVisibleAndExtractableItems(GameTestHelper helper) {
        TestItemStorage handler = new TestItemStorage(1) {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };
        handler.setStackInSlot(0, new ItemStack(Items.DIAMOND, 64));
        SummaryTracker tracker = new SummaryTracker();
        tracker.bind(Direction.NORTH, handler);
        tracker.tick(1, 8192);
        SummaryPayload payload = tracker.drain(1);
        helper.assertTrue(payload.totals().stream().anyMatch(total -> total.total() == 64 && total.available() == 0), "Unavailable items advertised as craftable");
        helper.succeed();
    }
    public static void accessRevokedDuringOpenMenu(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        CompoundTag tag = new CompoundTag();
        CompoundTag lock = new CompoundTag();
        lock.putString("items", "minecraft:tripwire_hook");
        tag.put("lock", lock);
        chest.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        ServerPlayer player = player(helper);
        ItemStack key = new ItemStack(Items.TRIPWIRE_HOOK);
        key.set(DataComponents.CUSTOM_NAME, Component.literal("secret-key"));
        player.getInventory().setItem(0, key);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        Source source = menu.sources(SourceOrder.CONNECTED_FIRST).getFirst();
        helper.assertTrue(source.handler().getStackInSlot(0).is(Items.DIAMOND), "Key did not grant access");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(source.handler().extractItem(0, 1, false).isEmpty(), "Retained source bypassed revoked access");
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty() && chest.getItem(0).is(Items.DIAMOND), "Revoked access still allowed pickup");
        finish(helper, player, menu);
    }

    public static void protectionEventDeniesInventory(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        ServerPlayer player = player(helper);
        Runnable removeVeto = TestPlatform.deny(chest);
        try {
            StationMenu menu = new StationMenu(1, player.getInventory(), station);
            helper.assertTrue(menu.sides().isEmpty(), "Protection veto did not hide inventory");
            finish(helper, player, menu);
        } finally {
            removeVeto.run();
        }
    }

    public static void summaryRetirementClearsClient(GameTestHelper helper) {
        TestItemStorage handler = new TestItemStorage(1);
        SummaryTracker tracker = new SummaryTracker();
        ClientSummary client = new ClientSummary();
        tracker.bind(Direction.NORTH, handler);
        for (int i = 0; i < 40; i++) {
            ItemStack stack = new ItemStack(Items.PAPER);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("variant-" + i));
            handler.setStackInSlot(0, stack);
            tracker.tick(1, 1);
            SummaryPayload payload = tracker.drain(1);
            if (payload != null) {
                client.apply(payload);
            }
            helper.assertValueEqual(1, client.matching(item -> true).size(), "Client retained retired dictionary entries");
            helper.assertValueEqual(1L, client.total(Direction.NORTH, stack), "Replacement entry total was lost");
        }
        helper.succeed();
    }

    public static void plainViewFloodIsCoalesced(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        helper.setBlock(STATION.east(), Blocks.BARREL);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        ViewPayload.Request north = new ViewPayload.Request(Optional.of(Direction.NORTH), 0, 0, Optional.empty(), false);
        ViewPayload.Request east = new ViewPayload.Request(Optional.of(Direction.EAST), 0, 0, Optional.empty(), false);
        menu.handleViewRequest(1, menu.sidesVersion(), north);
        for (int i = 2; i < 100; i++) {
            menu.handleViewRequest(i, menu.sidesVersion(), east);
        }
        helper.assertValueEqual(Direction.NORTH, menu.selectedSide(), "Flood applied multiple requests within one tick");
        helper.runAfterDelay(1, () -> {
            menu.broadcastChanges();
            helper.assertValueEqual(Direction.EAST, menu.selectedSide(), "Newest request was not applied next tick");
            finish(helper, player, menu);
        });
    }

    public static void failedFillRollsBackPartialExtraction(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        TestItemStorage handler = new TestItemStorage(1) {
            private int extractions;

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!simulate && ++extractions > 1) {
                    return ItemStack.EMPTY;
                }
                return super.extractItem(slot, amount, simulate);
            }
        };
        // Oversized storage requires more than one extraction call for this pattern.
        handler.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 128));
        StationMenu menu = new StationMenu(1, player.getInventory(), station) {
            @Override
            public List<Source> sources(SourceOrder order) {
                return List.of(new Source(Origin.PLAYER, handler));
            }
        };
        StationOperations.FillResult result = StationOperations.fill(menu,
                List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS)), 64);
        helper.assertValueEqual(StationOperations.FillResult.NOT_ENOUGH, result, "Partial extraction was accepted");
        long total = handler.getStackInSlot(0).getCount();
        for (int i = 0; i < 36; i++) {
            total += player.getInventory().getItem(i).getCount();
        }
        helper.assertValueEqual(128L, total, "Failed fill changed the total item count");
        helper.assertTrue(station.isGridEmpty(), "Failed fill wrote a partial grid");
        finish(helper, player, menu);
    }

    public static void doubleChestChecksBothLocks(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT));
        helper.setBlock(STATION.north().east(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT));
        ChestBlockEntity partner = helper.getBlockEntity(STATION.north().east(), ChestBlockEntity.class);
        CompoundTag locked = new CompoundTag();
        CompoundTag lock = new CompoundTag();
        lock.putString("items", "minecraft:tripwire_hook");
        locked.put("lock", lock);
        partner.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), locked));
        partner.setItem(0, new ItemStack(Items.DIAMOND));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        helper.assertTrue(menu.sides().isEmpty(), "Locked remote half was exposed through the adjacent half");
        finish(helper, player, menu);
    }

    public static void staleMenuCannotTransferAfterBreak(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_LOG));
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        helper.setBlock(STATION, Blocks.AIR);
        helper.assertTrue(menu.quickMoveStack(player, StationMenu.RESULT_SLOT).isEmpty(), "Stale menu crafted after removal");
        menu.clicked(StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty() && chest.getItem(0).is(Items.DIAMOND), "Stale menu extracted after removal");
        finish(helper, player, menu);
    }

    public static void sharedResultCannotBeTakenTwice(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.setGridItem(0, new ItemStack(Items.OAK_LOG));
        ServerPlayer first = player(helper);
        ServerPlayer second = player(helper);
        StationMenu a = new StationMenu(1, first.getInventory(), station);
        StationMenu b = new StationMenu(1, second.getInventory(), station);
        a.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, first);
        for (int i = 0; i < 10; i++) {
            b.clicked(StationMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, second);
            b.quickMoveStack(second, StationMenu.RESULT_SLOT);
        }
        helper.assertValueEqual(4, a.getCarried().getCount(), "First craft produced an unexpected amount");
        helper.assertTrue(b.getCarried().isEmpty() && station.isGridEmpty(), "Shared output was taken twice");
        for (int i = 0; i < 36; i++) {
            helper.assertTrue(second.getInventory().getItem(i).isEmpty(), "Second viewer duplicated a stale output");
        }
        b.removed(second);
        helper.getLevel().getServer().getPlayerList().remove(second);
        finish(helper, first, a);
    }

    public static void fillReplayCannotCreateClientTemplates(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 64));
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        for (int i = 0; i < 20; i++) {
            menu.handleFill(new FillRecipePayload(1,
                    List.of(new ItemStack(Items.OAK_PLANKS)), 1));
        }
        helper.assertValueEqual(1, station.getGridItem(0).getCount(), "Replayed fill changed grid count");
        helper.assertValueEqual(63, player.getInventory().getItem(9).getCount(), "Replayed fill changed inventory count");
        ItemStack forged = new ItemStack(Items.OAK_PLANKS);
        forged.set(DataComponents.CUSTOM_NAME, Component.literal("forged-components"));
        helper.assertValueEqual(StationOperations.FillResult.NOT_ENOUGH, StationOperations.fill(menu, List.of(forged), 1), "Client-supplied components were created without a matching source");
        helper.assertTrue(station.getGridItem(0).getComponentsPatch().isEmpty(), "Forged components reached the grid");
        finish(helper, player, menu);
    }

    public static void multiSlotDragConservesItems(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.setCarried(new ItemStack(Items.COBBLESTONE, 63));
        menu.clicked(-999, 0, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.WINDOW_START, 1, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.WINDOW_START + 1, 1, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(-999, 2, ContainerInput.QUICK_CRAFT, player);
        int total = menu.getCarried().getCount();
        for (int i = 0; i < chest.getContainerSize(); i++) {
            total += chest.getItem(i).getCount();
        }
        helper.assertValueEqual(63, total, "Drag distribution lost or duplicated items");
        helper.assertValueEqual(31, chest.getItem(0).getCount(), "First drag slot received wrong amount");
        helper.assertValueEqual(31, chest.getItem(1).getCount(), "Second drag slot received wrong amount");
        finish(helper, player, menu);
    }

    public static void extractionExceptionRefundsOnlyReservedInputs(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        TestItemStorage handler = new TestItemStorage(1) {
            private int calls;

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!simulate && ++calls == 2) {
                    throw new IllegalStateException("test extraction failure");
                }
                return super.extractItem(slot, amount, simulate);
            }
        };
        handler.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 128));
        StationMenu menu = new StationMenu(1, player.getInventory(), station) {
            @Override
            public List<Source> sources(SourceOrder order) {
                return List.of(new Source(Origin.PLAYER, handler));
            }
        };
        boolean threw = false;
        try {
            StationOperations.fill(menu, List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS)), 64);
        } catch (IllegalStateException expected) {
            threw = true;
        }
        helper.assertTrue(threw, "Exception test did not exercise extraction failure");
        int total = handler.getStackInSlot(0).getCount();
        for (int i = 0; i < 36; i++) {
            total += player.getInventory().getItem(i).getCount();
        }
        helper.assertValueEqual(128, total, "Exception refund duplicated or lost inputs");
        helper.assertTrue(station.isGridEmpty(), "Exception left a partial output grid");
        finish(helper, player, menu);
    }

    public static void malformedIdDeltasAreRejected(GameTestHelper helper) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            VarInt.write(buffer, 2);
            VarInt.write(buffer, Integer.MAX_VALUE);
            VarInt.write(buffer, 0);
            boolean rejected = false;
            try {
                NetCodecs.SORTED_IDS.decode(buffer);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
            helper.assertTrue(rejected, "Overflowing id deltas were accepted");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    public static void malformedDragCannotMutateInventory(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.setCarried(new ItemStack(Items.DIAMOND, 32));
        menu.clicked(-999, 0, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(-999, 1, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.GRID_START, 9, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(-999, 10, ContainerInput.QUICK_CRAFT, player);
        helper.assertValueEqual(32, menu.getCarried().getCount(), "Malformed drag changed the cursor");
        helper.assertTrue(station.isGridEmpty(), "Malformed drag created items in the grid");
        finish(helper, player, menu);
    }

    public static void dragRefundsChangedInsertionAcceptance(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ServerPlayer player = player(helper);
        TestItemStorage handler = new TestItemStorage(27) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot == 1 && !simulate && stack.getCount() > 1) {
                    ItemStack rejected = super.insertItem(slot, stack.copyWithCount(1), false);
                    return stack.copyWithCount(stack.getCount() - 1 + rejected.getCount());
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
        StationMenu menu = new StationMenu(1, player.getInventory(), station) {
            @Override
            public ItemStorage windowHandler() {
                return handler;
            }
        };
        menu.setCarried(new ItemStack(Items.COBBLESTONE, 63));
        menu.clicked(-999, 0, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.WINDOW_START, 1, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(StationMenu.WINDOW_START + 1, 1, ContainerInput.QUICK_CRAFT, player);
        menu.clicked(-999, 2, ContainerInput.QUICK_CRAFT, player);
        int total = menu.getCarried().getCount() + handler.getStackInSlot(0).getCount() + handler.getStackInSlot(1).getCount();
        helper.assertValueEqual(63, total, "Changed insertion acceptance lost or duplicated items");
        helper.assertValueEqual(1, handler.getStackInSlot(1).getCount(), "Handler restriction was bypassed");
        helper.assertValueEqual(31, menu.getCarried().getCount(), "Rejected insertion was not refunded exactly once");
        finish(helper, player, menu);
    }

    public static void retainedSourceCannotBypassHiddenOrRemovedStation(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 2));
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        Source source = menu.sources(SourceOrder.CONNECTED_FIRST).getFirst();
        station.toggleHidden(Direction.NORTH);
        helper.assertTrue(source.handler().extractItem(0, 1, false).isEmpty(), "Retained source bypassed hidden side");
        station.toggleHidden(Direction.NORTH);
        helper.setBlock(STATION, Blocks.AIR);
        helper.assertTrue(source.handler().extractItem(0, 1, false).isEmpty(), "Retained source bypassed removed station");
        helper.assertValueEqual(2, chest.getItem(0).getCount(), "Retained source changed chest contents");
        finish(helper, player, menu);
    }

    public static void hiddenSideDiscoveryDoesNotGenerateLoot(GameTestHelper helper) {
        StationBlockEntity station = station(helper);
        station.toggleHidden(Direction.NORTH);
        helper.setBlock(STATION.north(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(STATION.north(), ChestBlockEntity.class);
        chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        ServerPlayer player = player(helper);
        StationMenu menu = new StationMenu(1, player.getInventory(), station);
        menu.broadcastChanges();
        helper.assertTrue(chest.getLootTable() != null, "Hidden inventory generated loot during discovery");
        finish(helper, player, menu);
    }

    private static void replaceRecipes(RecipeManager manager, List<RecipeHolder<?>> recipes) {
        try {
            Field field = RecipeManager.class.getDeclaredField("recipes");
            field.setAccessible(true);
            var registry = new MappedRegistry<Recipe<?>>(Registries.RECIPE, Lifecycle.stable());
            for (var recipe : recipes) registry.register(recipe.id(), recipe.value(), RegistrationInfo.BUILT_IN);
            registry.freeze();
            field.set(manager, RecipeMap.create(registry));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not replace the server recipe map for the reload regression", exception);
        }
    }
}
