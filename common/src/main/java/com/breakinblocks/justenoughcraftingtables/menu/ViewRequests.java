package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import org.jspecify.annotations.Nullable;

final class ViewRequests {
    record Pending(int id, int version, ViewPayload.Request request) {
    }

    @Nullable
    private Pending pending;
    private long lastRequestTick = Long.MIN_VALUE;
    private long lastRebuildTick = Long.MIN_VALUE;

    boolean ready(long tick, boolean rebuild, int rebuildInterval) {
        return tick != lastRequestTick && (!rebuild || tick >= lastRebuildTick + rebuildInterval);
    }

    void offer(int id, int version, ViewPayload.Request request) {
        if (pending != null && pending.request().rebuild() && !request.rebuild()) {
            request = new ViewPayload.Request(request.side(), request.page(), request.first(), request.filter(), true);
        }
        pending = new Pending(id, version, request);
    }

    @Nullable
    Pending pending() {
        return pending;
    }

    void clear() {
        pending = null;
    }

    void applied(long tick, boolean rebuild) {
        lastRequestTick = tick;
        if (rebuild) {
            lastRebuildTick = tick;
        }
    }
}
