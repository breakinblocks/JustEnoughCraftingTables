package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
public final class ModNetwork {
    record Binding<T extends StationPacket>(ResourceLocation id, PacketCodec<T> codec) {}
    private static final Map<Class<?>, Binding<?>> BINDINGS = new LinkedHashMap<>();
    static {
        bind(ViewPayload.class,"view",ViewPayload.STREAM_CODEC);
        bind(FillRecipePayload.class,"fill_recipe",FillRecipePayload.STREAM_CODEC);
        bind(SidesPayload.class,"sides",SidesPayload.STREAM_CODEC);
        bind(ViewStatePayload.class,"view_state",ViewStatePayload.STREAM_CODEC);
        bind(SummaryPayload.class,"summary",SummaryPayload.STREAM_CODEC);
        bind(ViewFragmentPayload.class,"view_fragment",ViewFragmentPayload.STREAM_CODEC);
    }
    private static <T extends StationPacket> void bind(Class<T> type,String path,PacketCodec<T> codec) { BINDINGS.put(type,new Binding<>(JustEnoughCraftingTables.id(path),codec)); }
    @SuppressWarnings("unchecked")
    static <T extends StationPacket> Binding<T> binding(Class<T> type) { return (Binding<T>) BINDINGS.get(type); }
    public static void register() {
        receive(ViewPayload.class,(p,v)-> { StationMenu m=menu(p,v.containerId()); if(m!=null) m.handleViewRequest(v.requestId(),v.sidesVersion(),v.request()); });
        receive(FillRecipePayload.class,(p,v)-> { StationMenu m=menu(p,v.containerId()); if(m!=null) m.handleFill(v); });
        receive(ViewFragmentPayload.class,(p,v)-> { StationMenu m=menu(p,v.view().containerId()); if(m!=null) m.handleViewFragment(v); });
    }
    private static StationMenu menu(ServerPlayer player,int id) { return player.containerMenu instanceof StationMenu m && m.containerId==id && m.stillValid(player) && !player.isSpectator() ? m : null; }
    private static <T extends StationPacket> void receive(Class<T> type,BiConsumer<ServerPlayer,T> handler) {
        Binding<T> b=binding(type);
        ServerPlayNetworking.registerGlobalReceiver(b.id(),(server,player,listener,buffer,response)-> {
            T payload=b.codec().decode(buffer);
            server.execute(()->handler.accept(player,payload));
        });
    }
    public static boolean hasChannel(ServerPlayer player) { return ServerPlayNetworking.canSend(player,binding(ViewStatePayload.class).id()); }
    public static void sendToServer(StationPacket payload) { ClientModNetwork.send(payload); }
    public static <T extends StationPacket> void sendToPlayer(ServerPlayer player,T payload) {
        Binding<T> b=bindingOf(payload);
        if(ServerPlayNetworking.canSend(player,b.id())) ServerPlayNetworking.send(player,b.id(),encode(payload,b));
    }
    @SuppressWarnings("unchecked")
    static <T extends StationPacket> Binding<T> bindingOf(T payload) { return (Binding<T>) BINDINGS.get(payload.getClass()); }
    static <T extends StationPacket> FriendlyByteBuf encode(T payload,Binding<T> b) { FriendlyByteBuf buffer=PacketByteBufs.create(); b.codec().encode(buffer,payload); return buffer; }
}
