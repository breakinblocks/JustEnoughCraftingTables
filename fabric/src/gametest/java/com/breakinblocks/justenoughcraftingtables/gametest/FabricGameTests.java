package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.menu.ViewScanGameTests;
import com.breakinblocks.justenoughcraftingtables.platform.StorageLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class FabricGameTests {
    @GameTest(maxTicks = 200)
    public void double_chest_lookup_stays_valid(GameTestHelper helper) {
        var pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT));
        helper.setBlock(pos.east(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT));
        int[] invalidations = {0};
        var lookup = new StorageLookup(helper.getLevel(), helper.absolutePos(pos), Direction.SOUTH,
                () -> true, () -> invalidations[0]++);
        var storage = lookup.get();
        helper.assertTrue(storage != null && storage.getSlots() == 54, "Expected a double chest");
        int initialInvalidations = invalidations[0];
        for (int i = 0; i < 20; i++) {
            helper.assertTrue(lookup.isValid(), "Unchanged double chest was invalidated");
            helper.assertTrue(lookup.get() == storage, "Unchanged double chest wrapper was replaced");
        }
        helper.assertTrue(invalidations[0] == initialInvalidations, "Stable lookup triggered invalidation");
        helper.setBlock(pos.east(), Blocks.AIR);
        helper.assertTrue(!lookup.isValid(), "Removing the other chest half must invalidate the lookup");
        helper.assertTrue(lookup.get().getSlots() == 27, "Lookup did not refresh to a single chest");
        helper.succeed();
    }

    @GameTest(maxTicks = 200)
    public void legacy_block_entity_loads(GameTestHelper helper) { StationGameTests.legacyBlockEntityLoads(helper); }
    @GameTest(maxTicks = 200)
    public void fill_uses_player_first_order(GameTestHelper helper) { StationGameTests.fillUsesPlayerFirstOrder(helper); }
    @GameTest(maxTicks = 200)
    public void fill_uses_connected_first_order_and_reuses_grid(GameTestHelper helper) { StationGameTests.fillUsesConnectedFirstOrderAndReusesGrid(helper); }
    @GameTest(maxTicks = 200)
    public void fill_max_sets(GameTestHelper helper) { StationGameTests.fillMaxSets(helper); }
    @GameTest(maxTicks = 200)
    public void fill_is_all_or_nothing(GameTestHelper helper) { StationGameTests.fillIsAllOrNothing(helper); }
    @GameTest(maxTicks = 200)
    public void store_returns_items_to_origins(GameTestHelper helper) { StationGameTests.storeReturnsItemsToOrigins(helper); }
    @GameTest(maxTicks = 200)
    public void store_leaves_items_whose_inventory_is_gone(GameTestHelper helper) { StationGameTests.storeLeavesItemsWhoseInventoryIsGone(helper); }
    @GameTest(maxTicks = 200)
    public void remainder_keeps_its_origin(GameTestHelper helper) { StationGameTests.remainderKeepsItsOrigin(helper); }
    @GameTest(maxTicks = 200)
    public void other_viewers_see_result_changes(GameTestHelper helper) { StationGameTests.otherViewersSeeResultChanges(helper); }
    @GameTest(maxTicks = 200)
    public void repeat_craft_refills_from_storage(GameTestHelper helper) { StationGameTests.repeatCraftRefillsFromStorage(helper); }
    @GameTest(maxTicks = 200)
    public void window_slots_respect_handler_rules(GameTestHelper helper) { StationGameTests.windowSlotsRespectHandlerRules(helper); }
    @GameTest(maxTicks = 200)
    public void slab_merge_needs_matching_colour(GameTestHelper helper) { StationGameTests.slabMergeNeedsMatchingColour(helper); }
    @GameTest(maxTicks = 200)
    public void hidden_side_is_not_a_source(GameTestHelper helper) { StationGameTests.hiddenSideIsNotASource(helper); }
    @GameTest(maxTicks = 200)
    public void recipes_keep_colour(GameTestHelper helper) { StationGameTests.recipesKeepColour(helper); }
    @GameTest(maxTicks = 200)
    public void cauldron_washes_colour(GameTestHelper helper) { StationGameTests.cauldronWashesColour(helper); }
    @GameTest(maxTicks = 200)
    public void single_slot_drag_goes_through_insert(GameTestHelper helper) { StationGameTests.singleSlotDragGoesThroughInsert(helper); }
    @GameTest(maxTicks = 200)
    public void window_shift_click_and_swap_keep_items(GameTestHelper helper) { StationGameTests.windowShiftClickAndSwapKeepItems(helper); }
    @GameTest(maxTicks = 200)
    public void slab_merge_keeps_name_and_grid(GameTestHelper helper) { StationGameTests.slabMergeKeepsNameAndGrid(helper); }
    @GameTest(maxTicks = 200)
    public void slab_of_other_colour_places_in_next_block(GameTestHelper helper) { StationGameTests.slabOfOtherColourPlacesInNextBlock(helper); }
    @GameTest(maxTicks = 200)
    public void view_rebuilds_are_coalesced(GameTestHelper helper) { StationGameTests.viewRebuildsAreCoalesced(helper); }
    @GameTest(maxTicks = 200)
    public void fuel_values(GameTestHelper helper) { StationGameTests.fuelValues(helper); }
    @GameTest(maxTicks = 200)
    public void locked_chest_is_not_exposed(GameTestHelper helper) { StationRegressionTests.lockedChestIsNotExposed(helper); }
    @GameTest(maxTicks = 200)
    public void spectator_leaves_loot_ungenerated(GameTestHelper helper) { StationRegressionTests.spectatorLeavesLootUngenerated(helper); }
    @GameTest(maxTicks = 200)
    public void removed_recipe_cannot_be_crafted(GameTestHelper helper) { StationRegressionTests.removedRecipeCannotBeCrafted(helper); }
    @GameTest(maxTicks = 200)
    public void fill_uses_space_freed_by_extraction(GameTestHelper helper) { StationRegressionTests.fillUsesSpaceFreedByExtraction(helper); }
    @GameTest(maxTicks = 200)
    public void overlapping_ingredients_are_assigned(GameTestHelper helper) { StationRegressionTests.overlappingIngredientsAreAssigned(helper); }
    @GameTest(maxTicks = 200)
    public void summary_dictionary_stays_bounded(GameTestHelper helper) { StationRegressionTests.summaryDictionaryStaysBounded(helper); }
    @GameTest(maxTicks = 200)
    public void filtered_view_rejects_replaced_stacks(GameTestHelper helper) { StationRegressionTests.filteredViewRejectsReplacedStacks(helper); }
    @GameTest(maxTicks = 200)
    public void summary_separates_visible_and_extractable_items(GameTestHelper helper) { StationRegressionTests.summarySeparatesVisibleAndExtractableItems(helper); }
    @GameTest(maxTicks = 200)
    public void access_revoked_during_open_menu(GameTestHelper helper) { StationRegressionTests.accessRevokedDuringOpenMenu(helper); }
    @GameTest(maxTicks = 200)
    public void protection_event_denies_inventory(GameTestHelper helper) { StationRegressionTests.protectionEventDeniesInventory(helper); }
    @GameTest(maxTicks = 200)
    public void summary_retirement_clears_client(GameTestHelper helper) { StationRegressionTests.summaryRetirementClearsClient(helper); }
    @GameTest(maxTicks = 200)
    public void plain_view_flood_is_coalesced(GameTestHelper helper) { StationRegressionTests.plainViewFloodIsCoalesced(helper); }
    @GameTest(maxTicks = 200)
    public void failed_fill_rolls_back_partial_extraction(GameTestHelper helper) { StationRegressionTests.failedFillRollsBackPartialExtraction(helper); }
    @GameTest(maxTicks = 200)
    public void double_chest_checks_both_locks(GameTestHelper helper) { StationRegressionTests.doubleChestChecksBothLocks(helper); }
    @GameTest(maxTicks = 200)
    public void stale_menu_cannot_transfer_after_break(GameTestHelper helper) { StationRegressionTests.staleMenuCannotTransferAfterBreak(helper); }
    @GameTest(maxTicks = 200)
    public void shared_result_cannot_be_taken_twice(GameTestHelper helper) { StationRegressionTests.sharedResultCannotBeTakenTwice(helper); }
    @GameTest(maxTicks = 200)
    public void fill_replay_cannot_create_client_templates(GameTestHelper helper) { StationRegressionTests.fillReplayCannotCreateClientTemplates(helper); }
    @GameTest(maxTicks = 200)
    public void multi_slot_drag_conserves_items(GameTestHelper helper) { StationRegressionTests.multiSlotDragConservesItems(helper); }
    @GameTest(maxTicks = 200)
    public void extraction_exception_refunds_only_reserved_inputs(GameTestHelper helper) { StationRegressionTests.extractionExceptionRefundsOnlyReservedInputs(helper); }
    @GameTest(maxTicks = 200)
    public void malformed_id_deltas_are_rejected(GameTestHelper helper) { StationRegressionTests.malformedIdDeltasAreRejected(helper); }
    @GameTest(maxTicks = 200)
    public void malformed_drag_cannot_mutate_inventory(GameTestHelper helper) { StationRegressionTests.malformedDragCannotMutateInventory(helper); }
    @GameTest(maxTicks = 200)
    public void drag_refunds_changed_insertion_acceptance(GameTestHelper helper) { StationRegressionTests.dragRefundsChangedInsertionAcceptance(helper); }
    @GameTest(maxTicks = 200)
    public void retained_source_cannot_bypass_hidden_or_removed_station(GameTestHelper helper) { StationRegressionTests.retainedSourceCannotBypassHiddenOrRemovedStation(helper); }
    @GameTest(maxTicks = 200)
    public void hidden_side_discovery_does_not_generate_loot(GameTestHelper helper) { StationRegressionTests.hiddenSideDiscoveryDoesNotGenerateLoot(helper); }
    @GameTest(maxTicks = 200)
    public void large_filter_honors_scan_budget(GameTestHelper helper) { ViewScanGameTests.largeFilterHonorsScanBudget(helper); }
    @GameTest(maxTicks = 200)
    public void summary_codec_preserves_availability_and_retirements(GameTestHelper helper) { ViewScanGameTests.summaryCodecPreservesAvailabilityAndRetirements(helper); }
    @GameTest(maxTicks = 200)
    public void simulation_and_nested_rollback_preserve_components(GameTestHelper helper) { NativeTransferTests.simulationAndNestedRollbackPreserveComponents(helper); }
    @GameTest(maxTicks = 200)
    public void active_menu_fill_defers_commit_synchronization(GameTestHelper helper) { NativeTransferTests.activeMenuFillDefersCommitSynchronization(helper); }
    @GameTest(maxTicks = 200)
    public void rejected_returns_rollback_entire_fill(GameTestHelper helper) { NativeTransferTests.rejectedReturnsRollbackEntireFill(helper); }
    @GameTest(maxTicks = 200)
    public void throwing_returns_rollback_entire_fill(GameTestHelper helper) { NativeTransferTests.throwingReturnsRollbackEntireFill(helper); }
    @GameTest(maxTicks = 200)
    public void replacement_drops_grid_exactly_once(GameTestHelper helper) { NativeTransferTests.replacementDropsGridExactlyOnce(helper); }
    @GameTest(maxTicks = 200)
    public void repeat_connected_first_with_active_creative_menu(GameTestHelper helper) { NativeTransferTests.repeatConnectedFirstWithActiveCreativeMenu(helper); }
}
