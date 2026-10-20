package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record FillRecipePayload(int containerId, List<ItemStack> templates, int sets) implements CustomPacketPayload {
    public static final Type<FillRecipePayload> TYPE = new Type<>(JustEnoughCraftingTables.id("fill_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FillRecipePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FillRecipePayload::containerId,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(9)), FillRecipePayload::templates,
            ByteBufCodecs.VAR_INT, FillRecipePayload::sets,
            FillRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
