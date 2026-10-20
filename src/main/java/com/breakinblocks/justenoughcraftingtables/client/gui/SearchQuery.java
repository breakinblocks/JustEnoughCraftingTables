package com.breakinblocks.justenoughcraftingtables.client.gui;

import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class SearchQuery {
    private SearchQuery() {
    }

    public static Predicate<ItemStack> parse(String raw) {
        String query = raw.trim().toLowerCase(Locale.ROOT);
        if (query.startsWith("@")) {
            String text = query.substring(1);
            return stack -> {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                String namespace = key.getNamespace();
                if (namespace.contains(text)) {
                    return true;
                }
                return ModList.get().getModContainerById(namespace)
                        .map(container -> container.getModInfo().getDisplayName().toLowerCase(Locale.ROOT).contains(text))
                        .orElse(false);
            };
        }
        if (query.startsWith("#")) {
            String text = query.substring(1);
            return stack -> stack.getTags().anyMatch(tag -> tag.location().toString().contains(text));
        }
        return stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query);
    }
}
