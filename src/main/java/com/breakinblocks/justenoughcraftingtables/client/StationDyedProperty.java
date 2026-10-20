package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record StationDyedProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<StationDyedProperty> MAP_CODEC = MapCodec.unit(StationDyedProperty::new);

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
        return StationColor.of(stack).isDyed();
    }

    @Override
    public MapCodec<StationDyedProperty> type() {
        return MAP_CODEC;
    }
}
