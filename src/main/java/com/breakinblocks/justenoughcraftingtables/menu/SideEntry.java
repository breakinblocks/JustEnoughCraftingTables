package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.net.NetCodecs;
import com.breakinblocks.justenoughcraftingtables.net.PacketCodec;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record SideEntry(Direction direction, int slots, ItemStack icon, Component name, boolean hidden) {
    public static final PacketCodec<SideEntry> STREAM_CODEC = PacketCodec.of((buffer, entry) -> {
        NetCodecs.DIRECTION.encode(buffer, entry.direction());
        buffer.writeVarInt(entry.slots());
        NetCodecs.ITEM.encode(buffer, entry.icon());
        buffer.writeComponent(entry.name());
        buffer.writeBoolean(entry.hidden());
    }, buffer -> new SideEntry(NetCodecs.DIRECTION.decode(buffer), buffer.readVarInt(), NetCodecs.ITEM.decode(buffer), buffer.readComponent(), buffer.readBoolean()));
    public static final PacketCodec<List<SideEntry>> LIST_STREAM_CODEC = NetCodecs.list(STREAM_CODEC, 6);

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
