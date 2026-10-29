package com.breakinblocks.justenoughcraftingtables.registry;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public record Entry<T>(ResourceLocation getId, T value) implements Supplier<T> {
    public T get() { return value; }
}
