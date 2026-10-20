package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class StationRecipes {
    @FunctionalInterface
    public interface Lookup {
        Optional<CraftingRecipe> find(
                StationMenu menu, ServerPlayer player, CraftingContainer input, Level level, @Nullable CraftingRecipe last);
    }

    public static final Lookup VANILLA = (menu, player, input, level, last) ->
            level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level, last == null ? null : last.getId()).map(pair -> pair.getSecond());

    private static Lookup lookup = VANILLA;

    private StationRecipes() {
    }

    public static void setLookup(Lookup newLookup) {
        lookup = Objects.requireNonNull(newLookup);
    }

    public static Optional<CraftingRecipe> find(
            StationMenu menu, ServerPlayer player, CraftingContainer input, Level level, @Nullable CraftingRecipe last) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        CraftingRecipe current = null;
        if (last != null && level.getRecipeManager().byKey(last.getId()).orElse(null) == last) {
            current = last;
        }
        return lookup.find(menu, player, input, level, current)
                .filter(holder -> level.getRecipeManager().byKey(holder.getId()).orElse(null) == holder);
    }
}
