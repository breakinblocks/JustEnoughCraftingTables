package com.breakinblocks.justenoughcraftingtables.integration.polymorph;

import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationRecipes;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

public final class PolymorphIntegration {
    private PolymorphIntegration() {
    }

    public static void register() {
        StationRecipes.setLookup(PolymorphIntegration::find);
        MinecraftForge.EVENT_BUS.addListener(PolymorphIntegration::onContainerOpen);
    }

    private static Optional<CraftingRecipe> find(
            StationMenu menu, ServerPlayer player, CraftingContainer input, Level level, @Nullable CraftingRecipe last) {
        IPlayerRecipeData data = PolymorphApi.common().getRecipeData(player).orElse(null);
        if (data == null) {
            return StationRecipes.VANILLA.find(menu, player, input, level, last);
        }
        data.setContainerMenu(menu);
        return data.getRecipe(RecipeType.CRAFTING, input, level, new ArrayList<>());
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof StationMenu menu && event.getEntity() instanceof ServerPlayer) {
            menu.updateResult(true);
        }
    }
}
