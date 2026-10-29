package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record StationItemTint() implements ItemTintSource {
    public static final MapCodec<StationItemTint> MAP_CODEC = MapCodec.unit(StationItemTint::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return StationColor.of(stack).tint();
    }

    @Override
    public MapCodec<StationItemTint> type() {
        return MAP_CODEC;
    }
}
