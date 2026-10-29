package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.client.ClientPackets;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
public final class ClientModNetwork {
    public static void registerClient() {
        receive(SidesPayload.class,ClientPackets::sides);
        receive(ViewStatePayload.class,ClientPackets::view);
        receive(SummaryPayload.class,ClientPackets::summary);
    }
    private static <T extends StationPacket> void receive(Class<T> type,Consumer<T> handler) {
        ModNetwork.Binding<T> b=ModNetwork.binding(type);
        ClientPlayNetworking.registerGlobalReceiver(b.id(),(client,listener,buffer,response)-> {
            T payload=b.codec().decode(buffer); client.execute(()->handler.accept(payload));
        });
    }
    public static void send(StationPacket payload) {
        if(payload instanceof ViewPayload view && view.request().filter().map(ids->ids.length>ViewFragmentPayload.CHUNK_SIZE).orElse(false)) {
            for(ViewFragmentPayload fragment:ViewFragmentPayload.split(view)) sendOne(fragment);
        } else sendOne(payload);
    }
    private static <T extends StationPacket> void sendOne(T payload) {
        ModNetwork.Binding<T> b=ModNetwork.bindingOf(payload);
        ClientPlayNetworking.send(b.id(),ModNetwork.encode(payload,b));
    }
}
