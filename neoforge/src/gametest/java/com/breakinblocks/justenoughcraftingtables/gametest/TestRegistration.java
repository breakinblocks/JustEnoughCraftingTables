package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.ViewScanGameTests;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class TestRegistration {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> {
            registry.register(JustEnoughCraftingTables.id("legacy_block_entity_loads"), StationGameTests::legacyBlockEntityLoads);
            registry.register(JustEnoughCraftingTables.id("fill_uses_player_first_order"), StationGameTests::fillUsesPlayerFirstOrder);
            registry.register(JustEnoughCraftingTables.id("fill_uses_connected_first_order_and_reuses_grid"), StationGameTests::fillUsesConnectedFirstOrderAndReusesGrid);
            registry.register(JustEnoughCraftingTables.id("fill_max_sets"), StationGameTests::fillMaxSets);
            registry.register(JustEnoughCraftingTables.id("fill_is_all_or_nothing"), StationGameTests::fillIsAllOrNothing);
            registry.register(JustEnoughCraftingTables.id("store_returns_items_to_origins"), StationGameTests::storeReturnsItemsToOrigins);
            registry.register(JustEnoughCraftingTables.id("store_leaves_items_whose_inventory_is_gone"), StationGameTests::storeLeavesItemsWhoseInventoryIsGone);
            registry.register(JustEnoughCraftingTables.id("remainder_keeps_its_origin"), StationGameTests::remainderKeepsItsOrigin);
            registry.register(JustEnoughCraftingTables.id("other_viewers_see_result_changes"), StationGameTests::otherViewersSeeResultChanges);
            registry.register(JustEnoughCraftingTables.id("repeat_craft_refills_from_storage"), StationGameTests::repeatCraftRefillsFromStorage);
            registry.register(JustEnoughCraftingTables.id("window_slots_respect_handler_rules"), StationGameTests::windowSlotsRespectHandlerRules);
            registry.register(JustEnoughCraftingTables.id("slab_merge_needs_matching_colour"), StationGameTests::slabMergeNeedsMatchingColour);
            registry.register(JustEnoughCraftingTables.id("hidden_side_is_not_a_source"), StationGameTests::hiddenSideIsNotASource);
            registry.register(JustEnoughCraftingTables.id("recipes_keep_colour"), StationGameTests::recipesKeepColour);
            registry.register(JustEnoughCraftingTables.id("cauldron_washes_colour"), StationGameTests::cauldronWashesColour);
            registry.register(JustEnoughCraftingTables.id("single_slot_drag_goes_through_insert"), StationGameTests::singleSlotDragGoesThroughInsert);
            registry.register(JustEnoughCraftingTables.id("window_shift_click_and_swap_keep_items"), StationGameTests::windowShiftClickAndSwapKeepItems);
            registry.register(JustEnoughCraftingTables.id("slab_merge_keeps_name_and_grid"), StationGameTests::slabMergeKeepsNameAndGrid);
            registry.register(JustEnoughCraftingTables.id("slab_of_other_colour_places_in_next_block"), StationGameTests::slabOfOtherColourPlacesInNextBlock);
            registry.register(JustEnoughCraftingTables.id("view_rebuilds_are_coalesced"), StationGameTests::viewRebuildsAreCoalesced);
            registry.register(JustEnoughCraftingTables.id("fuel_values"), StationGameTests::fuelValues);
            registry.register(JustEnoughCraftingTables.id("locked_chest_is_not_exposed"), StationRegressionTests::lockedChestIsNotExposed);
            registry.register(JustEnoughCraftingTables.id("spectator_leaves_loot_ungenerated"), StationRegressionTests::spectatorLeavesLootUngenerated);
            registry.register(JustEnoughCraftingTables.id("removed_recipe_cannot_be_crafted"), StationRegressionTests::removedRecipeCannotBeCrafted);
            registry.register(JustEnoughCraftingTables.id("fill_uses_space_freed_by_extraction"), StationRegressionTests::fillUsesSpaceFreedByExtraction);
            registry.register(JustEnoughCraftingTables.id("overlapping_ingredients_are_assigned"), StationRegressionTests::overlappingIngredientsAreAssigned);
            registry.register(JustEnoughCraftingTables.id("summary_dictionary_stays_bounded"), StationRegressionTests::summaryDictionaryStaysBounded);
            registry.register(JustEnoughCraftingTables.id("filtered_view_rejects_replaced_stacks"), StationRegressionTests::filteredViewRejectsReplacedStacks);
            registry.register(JustEnoughCraftingTables.id("summary_separates_visible_and_extractable_items"), StationRegressionTests::summarySeparatesVisibleAndExtractableItems);
            registry.register(JustEnoughCraftingTables.id("access_revoked_during_open_menu"), StationRegressionTests::accessRevokedDuringOpenMenu);
            registry.register(JustEnoughCraftingTables.id("protection_event_denies_inventory"), StationRegressionTests::protectionEventDeniesInventory);
            registry.register(JustEnoughCraftingTables.id("summary_retirement_clears_client"), StationRegressionTests::summaryRetirementClearsClient);
            registry.register(JustEnoughCraftingTables.id("plain_view_flood_is_coalesced"), StationRegressionTests::plainViewFloodIsCoalesced);
            registry.register(JustEnoughCraftingTables.id("failed_fill_rolls_back_partial_extraction"), StationRegressionTests::failedFillRollsBackPartialExtraction);
            registry.register(JustEnoughCraftingTables.id("double_chest_checks_both_locks"), StationRegressionTests::doubleChestChecksBothLocks);
            registry.register(JustEnoughCraftingTables.id("stale_menu_cannot_transfer_after_break"), StationRegressionTests::staleMenuCannotTransferAfterBreak);
            registry.register(JustEnoughCraftingTables.id("shared_result_cannot_be_taken_twice"), StationRegressionTests::sharedResultCannotBeTakenTwice);
            registry.register(JustEnoughCraftingTables.id("fill_replay_cannot_create_client_templates"), StationRegressionTests::fillReplayCannotCreateClientTemplates);
            registry.register(JustEnoughCraftingTables.id("multi_slot_drag_conserves_items"), StationRegressionTests::multiSlotDragConservesItems);
            registry.register(JustEnoughCraftingTables.id("extraction_exception_refunds_only_reserved_inputs"), StationRegressionTests::extractionExceptionRefundsOnlyReservedInputs);
            registry.register(JustEnoughCraftingTables.id("malformed_id_deltas_are_rejected"), StationRegressionTests::malformedIdDeltasAreRejected);
            registry.register(JustEnoughCraftingTables.id("malformed_drag_cannot_mutate_inventory"), StationRegressionTests::malformedDragCannotMutateInventory);
            registry.register(JustEnoughCraftingTables.id("drag_refunds_changed_insertion_acceptance"), StationRegressionTests::dragRefundsChangedInsertionAcceptance);
            registry.register(JustEnoughCraftingTables.id("retained_source_cannot_bypass_hidden_or_removed_station"), StationRegressionTests::retainedSourceCannotBypassHiddenOrRemovedStation);
            registry.register(JustEnoughCraftingTables.id("hidden_side_discovery_does_not_generate_loot"), StationRegressionTests::hiddenSideDiscoveryDoesNotGenerateLoot);
            registry.register(JustEnoughCraftingTables.id("large_filter_honors_scan_budget"), ViewScanGameTests::largeFilterHonorsScanBudget);
            registry.register(JustEnoughCraftingTables.id("summary_codec_preserves_availability_and_retirements"), ViewScanGameTests::summaryCodecPreservesAvailabilityAndRetirements);
            registry.register(JustEnoughCraftingTables.id("simulation_and_nested_rollback_preserve_components"), NativeTransferTests::simulationAndNestedRollbackPreserveComponents);
            registry.register(JustEnoughCraftingTables.id("active_menu_fill_defers_commit_synchronization"), NativeTransferTests::activeMenuFillDefersCommitSynchronization);
            registry.register(JustEnoughCraftingTables.id("rejected_returns_rollback_entire_fill"), NativeTransferTests::rejectedReturnsRollbackEntireFill);
            registry.register(JustEnoughCraftingTables.id("throwing_returns_rollback_entire_fill"), NativeTransferTests::throwingReturnsRollbackEntireFill);
            registry.register(JustEnoughCraftingTables.id("replacement_drops_grid_exactly_once"), NativeTransferTests::replacementDropsGridExactlyOnce);
            registry.register(JustEnoughCraftingTables.id("repeat_connected_first_with_active_creative_menu"), NativeTransferTests::repeatConnectedFirstWithActiveCreativeMenu);
        });
    }
}
