package com.breakinblocks.justenoughcraftingtables.net;

import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;

public final class ViewRequestAssembler {
    private ViewPayload first;
    private int[] ids;
    private int received;
    private int total;

    public void clear() {
        first = null;
        ids = null;
        received = 0;
        total = 0;
    }

    @Nullable
    public ViewPayload accept(ViewFragmentPayload fragment) {
        ViewPayload view = fragment.view();
        if (!fragment.valid()) {
            clear();
            return null;
        }
        int[] part = view.request().filter().orElseThrow();
        if (fragment.offset() == 0) {
            clear();
            first = view;
            total = fragment.total();
            ids = new int[part.length];
        }
        if (first == null || total != fragment.total() || received != fragment.offset()
                || first.containerId() != view.containerId() || first.requestId() != view.requestId()
                || first.sidesVersion() != view.sidesVersion() || !first.request().side().equals(view.request().side())
                || first.request().page() != view.request().page() || first.request().first() != view.request().first()
                || first.request().rebuild() != view.request().rebuild()) {
            clear();
            return null;
        }
        if (received + part.length > ids.length) {
            ids = Arrays.copyOf(ids, Math.min(total, Math.max(ids.length * 2, received + part.length)));
        }
        System.arraycopy(part, 0, ids, received, part.length);
        received += part.length;
        if (received != total) return null;
        ViewPayload.Request request = first.request();
        ViewPayload result = new ViewPayload(first.containerId(), first.requestId(), first.sidesVersion(),
                new ViewPayload.Request(request.side(), request.page(), request.first(), Optional.of(ids), request.rebuild()));
        clear();
        return result;
    }
}
