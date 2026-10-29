package com.breakinblocks.justenoughcraftingtables.registry;

import net.minecraft.resources.Identifier;

public final class ModAliases {
    public static final String LEGACY_NAMESPACE = "craftingstation";

    private ModAliases() {
    }

    public static Identifier legacy(String path) {
        return Identifier.fromNamespaceAndPath(LEGACY_NAMESPACE, path);
    }
}
