package com.breakinblocks.justenoughcraftingtables.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record ViewFragmentPayload(ViewPayload view, int offset, int total) implements StationPacket {
    public static final int CHUNK_SIZE = 4096;
    public static final PacketCodec<ViewFragmentPayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.offset());
        buffer.writeVarInt(payload.total());
        ViewPayload.STREAM_CODEC.encode(buffer, payload.view());
    }, buffer -> {
        int offset = buffer.readVarInt();
        int total = buffer.readVarInt();
        ViewPayload view = ViewPayload.STREAM_CODEC.decode(buffer);
        ViewFragmentPayload fragment = new ViewFragmentPayload(view, offset, total);
        if (!fragment.valid()) {
            throw new IllegalArgumentException("Invalid view fragment");
        }
        return fragment;
    });

    public boolean valid() {
        int length = view.request().filter().map(ids -> ids.length).orElse(0);
        return total > CHUNK_SIZE && total <= NetCodecs.MAX_IDS && offset >= 0 && offset < total
                && offset % CHUNK_SIZE == 0 && length == Math.min(CHUNK_SIZE, total - offset);
    }

    public static List<ViewFragmentPayload> split(ViewPayload view) {
        int[] ids = view.request().filter().orElseThrow();
        if (ids.length <= CHUNK_SIZE || ids.length > NetCodecs.MAX_IDS) {
            throw new IllegalArgumentException("Invalid filter size");
        }
        List<ViewFragmentPayload> fragments = new ArrayList<>();
        ViewPayload.Request request = view.request();
        for (int offset = 0; offset < ids.length; offset += CHUNK_SIZE) {
            ViewPayload.Request part = new ViewPayload.Request(request.side(), request.page(), request.first(),
                    Optional.of(Arrays.copyOfRange(ids, offset, Math.min(ids.length, offset + CHUNK_SIZE))), request.rebuild());
            fragments.add(new ViewFragmentPayload(new ViewPayload(view.containerId(), view.requestId(), view.sidesVersion(), part), offset, ids.length));
        }
        return fragments;
    }
}
