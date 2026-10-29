package com.breakinblocks.justenoughcraftingtables.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SideEntry(Direction direction, int slots, ItemStack icon, Component name, boolean hidden) {
    public static final Codec<SideEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Direction.CODEC.fieldOf("direction").forGetter(SideEntry::direction),
                    Codec.INT.fieldOf("slots").forGetter(SideEntry::slots),
                    ItemStack.OPTIONAL_CODEC.fieldOf("icon").forGetter(SideEntry::icon),
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(SideEntry::name),
                    Codec.BOOL.fieldOf("hidden").forGetter(SideEntry::hidden))
            .apply(instance, SideEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SideEntry> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC, SideEntry::direction,
            ByteBufCodecs.VAR_INT, SideEntry::slots,
            ItemStack.OPTIONAL_STREAM_CODEC, SideEntry::icon,
            ComponentSerialization.STREAM_CODEC, SideEntry::name,
            ByteBufCodecs.BOOL, SideEntry::hidden,
            SideEntry::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, List<SideEntry>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list(6));

    public SideEntry withSlots(int newSlots) {
        return new SideEntry(direction, newSlots, icon, name, hidden);
    }

    public SideEntry withHidden(boolean newHidden) {
        return new SideEntry(direction, slots, icon, name, newHidden);
    }

    public boolean sameAs(SideEntry other) {
        return direction == other.direction
                && slots == other.slots
                && hidden == other.hidden
                && ItemStack.matches(icon, other.icon)
                && name.equals(other.name);
    }
}
