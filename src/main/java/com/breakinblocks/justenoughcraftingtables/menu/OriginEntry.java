package com.breakinblocks.justenoughcraftingtables.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record OriginEntry(Origin origin, int count) {
    public static final Codec<OriginEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Origin.CODEC.fieldOf("origin").forGetter(OriginEntry::origin),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(OriginEntry::count))
            .apply(instance, OriginEntry::new));

    public OriginEntry withCount(int newCount) {
        return new OriginEntry(origin, newCount);
    }

    public OriginEntry withOrigin(Origin newOrigin) {
        return new OriginEntry(newOrigin, count);
    }
}
