package com.breakinblocks.justenoughcraftingtables.registry;

import net.minecraft.resources.ResourceLocation;
public final class ModAliases {
    public static final String LEGACY_NAMESPACE="craftingstation";
    public static ResourceLocation legacy(String path) { return new ResourceLocation(LEGACY_NAMESPACE,path); }
}
