package com.breakinblocks.justenoughcraftingtables.registry;

import net.minecraft.resources.ResourceLocation;

public final class ModAliases {
    public static final String LEGACY_NAMESPACE = "craftingstation";

    private ModAliases() {
    }

    public static ResourceLocation legacy(String path) {
        return ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, path);
    }
}
