package com.breakinblocks.justenoughcraftingtables.gametest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import com.breakinblocks.justenoughcraftingtables.menu.ViewScanGameTests;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
@GameTestHolder("justenoughcraftingtables")
@PrefixGameTestTemplate(false)
public class NeoforgeGameTests {
    @GameTest(template = "empty")
    public static void legacyBlockEntityLoads(GameTestHelper helper) { StationGameTests.legacyBlockEntityLoads(helper); }
    @GameTest(template = "empty")
    public static void fillUsesPlayerFirstOrder(GameTestHelper helper) { StationGameTests.fillUsesPlayerFirstOrder(helper); }
    @GameTest(template = "empty")
    public static void fillUsesConnectedFirstOrderAndReusesGrid(GameTestHelper helper) { StationGameTests.fillUsesConnectedFirstOrderAndReusesGrid(helper); }
    @GameTest(template = "empty")
    public static void fillMaxSets(GameTestHelper helper) { StationGameTests.fillMaxSets(helper); }
    @GameTest(template = "empty")
    public static void fillIsAllOrNothing(GameTestHelper helper) { StationGameTests.fillIsAllOrNothing(helper); }
    @GameTest(template = "empty")
    public static void storeReturnsItemsToOrigins(GameTestHelper helper) { StationGameTests.storeReturnsItemsToOrigins(helper); }
    @GameTest(template = "empty")
    public static void storeLeavesItemsWhoseInventoryIsGone(GameTestHelper helper) { StationGameTests.storeLeavesItemsWhoseInventoryIsGone(helper); }
    @GameTest(template = "empty")
    public static void remainderKeepsItsOrigin(GameTestHelper helper) { StationGameTests.remainderKeepsItsOrigin(helper); }
    @GameTest(template = "empty")
    public static void otherViewersSeeResultChanges(GameTestHelper helper) { StationGameTests.otherViewersSeeResultChanges(helper); }
    @GameTest(template = "empty")
    public static void repeatCraftRefillsFromStorage(GameTestHelper helper) { StationGameTests.repeatCraftRefillsFromStorage(helper); }
    @GameTest(template = "empty")
    public static void windowSlotsRespectHandlerRules(GameTestHelper helper) { StationGameTests.windowSlotsRespectHandlerRules(helper); }
    @GameTest(template = "empty")
    public static void slabMergeNeedsMatchingColour(GameTestHelper helper) { StationGameTests.slabMergeNeedsMatchingColour(helper); }
    @GameTest(template = "empty")
    public static void hiddenSideIsNotASource(GameTestHelper helper) { StationGameTests.hiddenSideIsNotASource(helper); }
    @GameTest(template = "empty")
    public static void recipesKeepColour(GameTestHelper helper) { StationGameTests.recipesKeepColour(helper); }
    @GameTest(template = "empty")
    public static void cauldronWashesColour(GameTestHelper helper) { StationGameTests.cauldronWashesColour(helper); }
    @GameTest(template = "empty")
    public static void singleSlotDragGoesThroughInsert(GameTestHelper helper) { StationGameTests.singleSlotDragGoesThroughInsert(helper); }
    @GameTest(template = "empty")
    public static void windowShiftClickAndSwapKeepItems(GameTestHelper helper) { StationGameTests.windowShiftClickAndSwapKeepItems(helper); }
    @GameTest(template = "empty")
    public static void slabMergeKeepsNameAndGrid(GameTestHelper helper) { StationGameTests.slabMergeKeepsNameAndGrid(helper); }
    @GameTest(template = "empty")
    public static void slabOfOtherColourPlacesInNextBlock(GameTestHelper helper) { StationGameTests.slabOfOtherColourPlacesInNextBlock(helper); }
    @GameTest(template = "empty")
    public static void viewRebuildsAreCoalesced(GameTestHelper helper) { StationGameTests.viewRebuildsAreCoalesced(helper); }
    @GameTest(template = "empty")
    public static void fuelValues(GameTestHelper helper) { StationGameTests.fuelValues(helper); }
    @GameTest(template = "empty")
    public static void externalWindowWritesKeepItems(GameTestHelper helper) { StationRegressionTests.externalWindowWritesKeepItems(helper); }
    @GameTest(template = "empty")
    public static void lockedChestIsNotExposed(GameTestHelper helper) { StationRegressionTests.lockedChestIsNotExposed(helper); }
    @GameTest(template = "empty")
    public static void spectatorLeavesLootUngenerated(GameTestHelper helper) { StationRegressionTests.spectatorLeavesLootUngenerated(helper); }
    @GameTest(template = "empty")
    public static void removedRecipeCannotBeCrafted(GameTestHelper helper) { StationRegressionTests.removedRecipeCannotBeCrafted(helper); }
    @GameTest(template = "empty")
    public static void fillUsesSpaceFreedByExtraction(GameTestHelper helper) { StationRegressionTests.fillUsesSpaceFreedByExtraction(helper); }
    @GameTest(template = "empty")
    public static void overlappingIngredientsAreAssigned(GameTestHelper helper) { StationRegressionTests.overlappingIngredientsAreAssigned(helper); }
    @GameTest(template = "empty")
    public static void summaryDictionaryStaysBounded(GameTestHelper helper) { StationRegressionTests.summaryDictionaryStaysBounded(helper); }
    @GameTest(template = "empty")
    public static void filteredViewRejectsReplacedStacks(GameTestHelper helper) { StationRegressionTests.filteredViewRejectsReplacedStacks(helper); }
    @GameTest(template = "empty")
    public static void summarySeparatesVisibleAndExtractableItems(GameTestHelper helper) { StationRegressionTests.summarySeparatesVisibleAndExtractableItems(helper); }
    @GameTest(template = "empty")
    public static void accessRevokedDuringOpenMenu(GameTestHelper helper) { StationRegressionTests.accessRevokedDuringOpenMenu(helper); }
    @GameTest(template = "empty")
    public static void protectionEventDeniesInventory(GameTestHelper helper) { StationRegressionTests.protectionEventDeniesInventory(helper); }
    @GameTest(template = "empty")
    public static void summaryRetirementClearsClient(GameTestHelper helper) { StationRegressionTests.summaryRetirementClearsClient(helper); }
    @GameTest(template = "empty")
    public static void plainViewFloodIsCoalesced(GameTestHelper helper) { StationRegressionTests.plainViewFloodIsCoalesced(helper); }
    @GameTest(template = "empty")
    public static void failedFillRollsBackPartialExtraction(GameTestHelper helper) { StationRegressionTests.failedFillRollsBackPartialExtraction(helper); }
    @GameTest(template = "empty")
    public static void doubleChestChecksBothLocks(GameTestHelper helper) { StationRegressionTests.doubleChestChecksBothLocks(helper); }
    @GameTest(template = "empty")
    public static void staleMenuCannotTransferAfterBreak(GameTestHelper helper) { StationRegressionTests.staleMenuCannotTransferAfterBreak(helper); }
    @GameTest(template = "empty")
    public static void sharedResultCannotBeTakenTwice(GameTestHelper helper) { StationRegressionTests.sharedResultCannotBeTakenTwice(helper); }
    @GameTest(template = "empty")
    public static void fillReplayCannotCreateClientTemplates(GameTestHelper helper) { StationRegressionTests.fillReplayCannotCreateClientTemplates(helper); }
    @GameTest(template = "empty")
    public static void multiSlotDragConservesItems(GameTestHelper helper) { StationRegressionTests.multiSlotDragConservesItems(helper); }
    @GameTest(template = "empty")
    public static void extractionExceptionRefundsOnlyReservedInputs(GameTestHelper helper) { StationRegressionTests.extractionExceptionRefundsOnlyReservedInputs(helper); }
    @GameTest(template = "empty")
    public static void malformedIdDeltasAreRejected(GameTestHelper helper) { StationRegressionTests.malformedIdDeltasAreRejected(helper); }
    @GameTest(template = "empty")
    public static void malformedDragCannotMutateInventory(GameTestHelper helper) { StationRegressionTests.malformedDragCannotMutateInventory(helper); }
    @GameTest(template = "empty")
    public static void dragRefundsChangedInsertionAcceptance(GameTestHelper helper) { StationRegressionTests.dragRefundsChangedInsertionAcceptance(helper); }
    @GameTest(template = "empty")
    public static void retainedSourceCannotBypassHiddenOrRemovedStation(GameTestHelper helper) { StationRegressionTests.retainedSourceCannotBypassHiddenOrRemovedStation(helper); }
    @GameTest(template = "empty")
    public static void hiddenSideDiscoveryDoesNotGenerateLoot(GameTestHelper helper) { StationRegressionTests.hiddenSideDiscoveryDoesNotGenerateLoot(helper); }
    @GameTest(template = "empty")
    public static void largeFilterHonorsScanBudget(GameTestHelper helper) { ViewScanGameTests.largeFilterHonorsScanBudget(helper); }
    @GameTest(template = "empty")
    public static void summaryCodecPreservesAvailabilityAndRetirements(GameTestHelper helper) { ViewScanGameTests.summaryCodecPreservesAvailabilityAndRetirements(helper); }
}
