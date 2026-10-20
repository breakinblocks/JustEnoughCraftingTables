package com.breakinblocks.justenoughcraftingtables.integration.polymorph;

import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationRecipes;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

public final class PolymorphIntegration {
    private PolymorphIntegration() {
    }

    public static void register() {
        StationRecipes.setLookup(PolymorphIntegration::find);
        NeoForge.EVENT_BUS.addListener(PolymorphIntegration::onContainerOpen);
    }

    private static Optional<RecipeHolder<CraftingRecipe>> find(
            StationMenu menu, ServerPlayer player, CraftingInput input, Level level, @Nullable RecipeHolder<CraftingRecipe> last) {
        IPlayerRecipeData data = PolymorphApi.getInstance().getPlayerRecipeData(player);
        if (data == null) {
            return StationRecipes.VANILLA.find(menu, player, input, level, last);
        }
        data.setContainerMenu(menu);
        return Optional.ofNullable(data.getRecipe(RecipeType.CRAFTING, input, level, new ArrayList<>()));
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof StationMenu menu && event.getEntity() instanceof ServerPlayer) {
            menu.updateResult(true);
        }
    }
}
