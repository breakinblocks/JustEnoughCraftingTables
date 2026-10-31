package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.menu.ViewScanGameTests;
import com.breakinblocks.justenoughcraftingtables.platform.StorageLookup;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
public final class FabricGameTests implements FabricGameTest {
    @GameTest(template = "justenoughcraftingtables:empty")
    public void largeSearchFitsLegacyPacketLimit(GameTestHelper helper) { BackportTests.largeSearchFitsLegacyPacketLimit(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void interleavedSearchFragmentsCannotMixRequests(GameTestHelper helper) { BackportTests.interleavedSearchFragmentsCannotMixRequests(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void oversizedItemCodecPreservesCountAndTag(GameTestHelper helper) { BackportTests.oversizedItemCodecPreservesCountAndTag(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void vanillaSyncCannotOverwriteStorageCounts(GameTestHelper helper) { BackportTests.vanillaSyncCannotOverwriteStorageCounts(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void malformedSavedNameDoesNotBreakStation(GameTestHelper helper) { BackportTests.malformedSavedNameDoesNotBreakStation(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void placedItemsKeepCustomNames(GameTestHelper helper) { BackportTests.placedItemsKeepCustomNames(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void legacyBlockEntityLoads(GameTestHelper helper) { StationGameTests.legacyBlockEntityLoads(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillUsesPlayerFirstOrder(GameTestHelper helper) { StationGameTests.fillUsesPlayerFirstOrder(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillUsesConnectedFirstOrderAndReusesGrid(GameTestHelper helper) { StationGameTests.fillUsesConnectedFirstOrderAndReusesGrid(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillMaxSets(GameTestHelper helper) { StationGameTests.fillMaxSets(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillIsAllOrNothing(GameTestHelper helper) { StationGameTests.fillIsAllOrNothing(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void storeReturnsItemsToOrigins(GameTestHelper helper) { StationGameTests.storeReturnsItemsToOrigins(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void storeLeavesItemsWhoseInventoryIsGone(GameTestHelper helper) { StationGameTests.storeLeavesItemsWhoseInventoryIsGone(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void remainderKeepsItsOrigin(GameTestHelper helper) { StationGameTests.remainderKeepsItsOrigin(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void otherViewersSeeResultChanges(GameTestHelper helper) { StationGameTests.otherViewersSeeResultChanges(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void repeatCraftRefillsFromStorage(GameTestHelper helper) { StationGameTests.repeatCraftRefillsFromStorage(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void windowSlotsRespectHandlerRules(GameTestHelper helper) { StationGameTests.windowSlotsRespectHandlerRules(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void slabMergeNeedsMatchingColour(GameTestHelper helper) { StationGameTests.slabMergeNeedsMatchingColour(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void hiddenSideIsNotASource(GameTestHelper helper) { StationGameTests.hiddenSideIsNotASource(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void recipesKeepColour(GameTestHelper helper) { StationGameTests.recipesKeepColour(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void cauldronWashesColour(GameTestHelper helper) { StationGameTests.cauldronWashesColour(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void singleSlotDragGoesThroughInsert(GameTestHelper helper) { StationGameTests.singleSlotDragGoesThroughInsert(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void windowShiftClickAndSwapKeepItems(GameTestHelper helper) { StationGameTests.windowShiftClickAndSwapKeepItems(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void slabMergeKeepsNameAndGrid(GameTestHelper helper) { StationGameTests.slabMergeKeepsNameAndGrid(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void slabOfOtherColourPlacesInNextBlock(GameTestHelper helper) { StationGameTests.slabOfOtherColourPlacesInNextBlock(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void viewRebuildsAreCoalesced(GameTestHelper helper) { StationGameTests.viewRebuildsAreCoalesced(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fuelValues(GameTestHelper helper) { StationGameTests.fuelValues(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void externalWindowWritesKeepItems(GameTestHelper helper) { StationRegressionTests.externalWindowWritesKeepItems(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void lockedChestIsNotExposed(GameTestHelper helper) { StationRegressionTests.lockedChestIsNotExposed(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void spectatorLeavesLootUngenerated(GameTestHelper helper) { StationRegressionTests.spectatorLeavesLootUngenerated(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void removedRecipeCannotBeCrafted(GameTestHelper helper) { StationRegressionTests.removedRecipeCannotBeCrafted(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillUsesSpaceFreedByExtraction(GameTestHelper helper) { StationRegressionTests.fillUsesSpaceFreedByExtraction(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void overlappingIngredientsAreAssigned(GameTestHelper helper) { StationRegressionTests.overlappingIngredientsAreAssigned(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void summaryDictionaryStaysBounded(GameTestHelper helper) { StationRegressionTests.summaryDictionaryStaysBounded(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void filteredViewRejectsReplacedStacks(GameTestHelper helper) { StationRegressionTests.filteredViewRejectsReplacedStacks(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void summarySeparatesVisibleAndExtractableItems(GameTestHelper helper) { StationRegressionTests.summarySeparatesVisibleAndExtractableItems(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void accessRevokedDuringOpenMenu(GameTestHelper helper) { StationRegressionTests.accessRevokedDuringOpenMenu(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void protectionEventDeniesInventory(GameTestHelper helper) { StationRegressionTests.protectionEventDeniesInventory(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void summaryRetirementClearsClient(GameTestHelper helper) { StationRegressionTests.summaryRetirementClearsClient(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void plainViewFloodIsCoalesced(GameTestHelper helper) { StationRegressionTests.plainViewFloodIsCoalesced(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void failedFillRollsBackPartialExtraction(GameTestHelper helper) { StationRegressionTests.failedFillRollsBackPartialExtraction(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void doubleChestChecksBothLocks(GameTestHelper helper) { StationRegressionTests.doubleChestChecksBothLocks(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void staleMenuCannotTransferAfterBreak(GameTestHelper helper) { StationRegressionTests.staleMenuCannotTransferAfterBreak(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void sharedResultCannotBeTakenTwice(GameTestHelper helper) { StationRegressionTests.sharedResultCannotBeTakenTwice(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void fillReplayCannotCreateClientTemplates(GameTestHelper helper) { StationRegressionTests.fillReplayCannotCreateClientTemplates(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void multiSlotDragConservesItems(GameTestHelper helper) { StationRegressionTests.multiSlotDragConservesItems(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void extractionExceptionRefundsOnlyReservedInputs(GameTestHelper helper) { StationRegressionTests.extractionExceptionRefundsOnlyReservedInputs(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void malformedIdDeltasAreRejected(GameTestHelper helper) { StationRegressionTests.malformedIdDeltasAreRejected(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void malformedDragCannotMutateInventory(GameTestHelper helper) { StationRegressionTests.malformedDragCannotMutateInventory(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void dragRefundsChangedInsertionAcceptance(GameTestHelper helper) { StationRegressionTests.dragRefundsChangedInsertionAcceptance(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void retainedSourceCannotBypassHiddenOrRemovedStation(GameTestHelper helper) { StationRegressionTests.retainedSourceCannotBypassHiddenOrRemovedStation(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void hiddenSideDiscoveryDoesNotGenerateLoot(GameTestHelper helper) { StationRegressionTests.hiddenSideDiscoveryDoesNotGenerateLoot(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void largeFilterHonorsScanBudget(GameTestHelper helper) { ViewScanGameTests.largeFilterHonorsScanBudget(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
    public void summaryCodecPreservesAvailabilityAndRetirements(GameTestHelper helper) { ViewScanGameTests.summaryCodecPreservesAvailabilityAndRetirements(helper); }
    @GameTest(template = "justenoughcraftingtables:empty")
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

}
