package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.ClientPackets;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(JustEnoughCraftingTables.id("main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        register(ViewFragmentPayload.class, 5, NetworkDirection.PLAY_TO_SERVER, ViewFragmentPayload.STREAM_CODEC, (payload, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            ServerPlayer player = context.getSender();
            StationMenu menu = menu(player, payload.view().containerId());
            if (menu != null && menu.stillValid(player) && !player.isSpectator()) menu.handleViewFragment(payload);
            context.setPacketHandled(true);
        });
        register(ViewPayload.class, 0, NetworkDirection.PLAY_TO_SERVER, ViewPayload.STREAM_CODEC, (payload, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            ServerPlayer player = context.getSender();
            StationMenu menu = menu(player, payload.containerId());
            if (menu != null && menu.stillValid(player) && !player.isSpectator()) {
                menu.handleViewRequest(payload.requestId(), payload.sidesVersion(), payload.request());
            }
            context.setPacketHandled(true);
        });
        register(FillRecipePayload.class, 1, NetworkDirection.PLAY_TO_SERVER, FillRecipePayload.STREAM_CODEC, (payload, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            ServerPlayer player = context.getSender();
            StationMenu menu = menu(player, payload.containerId());
            if (menu != null && menu.stillValid(player) && !player.isSpectator()) menu.handleFill(payload);
            context.setPacketHandled(true);
        });
        register(SidesPayload.class, 2, NetworkDirection.PLAY_TO_CLIENT, SidesPayload.STREAM_CODEC, (payload, context) -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPackets.sides(payload));
            context.get().setPacketHandled(true);
        });
        register(ViewStatePayload.class, 3, NetworkDirection.PLAY_TO_CLIENT, ViewStatePayload.STREAM_CODEC, (payload, context) -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPackets.view(payload));
            context.get().setPacketHandled(true);
        });
        register(SummaryPayload.class, 4, NetworkDirection.PLAY_TO_CLIENT, SummaryPayload.STREAM_CODEC, (payload, context) -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPackets.summary(payload));
            context.get().setPacketHandled(true);
        });
    }

    private static <T extends StationPacket> void register(Class<T> type, int id, NetworkDirection direction,
            PacketCodec<T> codec, BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.messageBuilder(type, id, direction).encoder((payload, buffer) -> codec.encode(buffer, payload))
                .decoder(codec::decode).consumerMainThread(handler).add();
    }

    @Nullable
    private static StationMenu menu(@Nullable ServerPlayer player, int containerId) {
        return player != null && player.containerMenu instanceof StationMenu menu && menu.containerId == containerId ? menu : null;
    }

    public static boolean hasChannel(ServerPlayer player) {
        return player.connection != null && player.connection.connection.channel() != null && CHANNEL.isRemotePresent(player.connection.connection);
    }

    public static void sendToServer(StationPacket payload) {
        if (payload instanceof ViewPayload view && view.request().filter().map(ids -> ids.length > ViewFragmentPayload.CHUNK_SIZE).orElse(false)) {
            for (ViewFragmentPayload fragment : ViewFragmentPayload.split(view)) CHANNEL.sendToServer(fragment);
        } else {
            CHANNEL.sendToServer(payload);
        }
    }

    public static void sendToPlayer(ServerPlayer player, StationPacket payload) {
        if (hasChannel(player)) CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
