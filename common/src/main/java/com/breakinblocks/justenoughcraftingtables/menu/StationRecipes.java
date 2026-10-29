package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class StationRecipes {
    @FunctionalInterface
    public interface Lookup {
        Optional<RecipeHolder<CraftingRecipe>> find(
                StationMenu menu, ServerPlayer player, CraftingInput input, Level level, @Nullable RecipeHolder<CraftingRecipe> last);
    }

    public static final Lookup VANILLA = (menu, player, input, level, last) ->
            ((ServerLevel) level).getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level, last);

    private static Lookup lookup = VANILLA;

    private StationRecipes() {
    }

    public static void setLookup(Lookup newLookup) {
        lookup = Objects.requireNonNull(newLookup);
    }

    public static Optional<RecipeHolder<CraftingRecipe>> find(
            StationMenu menu, ServerPlayer player, CraftingInput input, Level level, @Nullable RecipeHolder<CraftingRecipe> last) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        RecipeHolder<CraftingRecipe> current = null;
        if (last != null && ((ServerLevel) level).getServer().getRecipeManager().byKey(last.id()).orElse(null) == last) {
            current = last;
        }
        return lookup.find(menu, player, input, level, current)
                .filter(holder -> ((ServerLevel) level).getServer().getRecipeManager().byKey(holder.id()).orElse(null) == holder);
    }
}
