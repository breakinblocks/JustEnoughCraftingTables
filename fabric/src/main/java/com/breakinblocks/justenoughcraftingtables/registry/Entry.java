package com.breakinblocks.justenoughcraftingtables.registry;

import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

public record Entry<T>(Identifier getId, T value) implements Supplier<T> {
    public T get() { return value; }
}
