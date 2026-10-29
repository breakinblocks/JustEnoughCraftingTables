package com.breakinblocks.justenoughcraftingtables.menu;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class ServerView {
    private final int pageSize;

    @Nullable
    private Direction side;

    private boolean valid;
    private boolean filtered;
    private int[] list = new int[0];
    private int page;
    private int first;

    public ServerView(int pageSize) {
        this.pageSize = pageSize;
    }

    @Nullable
    public Direction side() {
        return side;
    }

    public boolean valid() {
        return valid;
    }

    public boolean filtered() {
        return filtered;
    }

    public int page() {
        return page;
    }

    public int first() {
        return first;
    }

    public int pageSize() {
        return pageSize;
    }

    public void invalidate() {
        valid = false;
    }

    public void bind(@Nullable Direction newSide) {
        side = newSide;
        valid = newSide != null;
    }

    public void setUnfiltered() {
        filtered = false;
        list = new int[0];
    }

    public void setFiltered(int[] matching) {
        filtered = true;
        list = matching;
    }

    public int length(int slotCount) {
        return filtered ? list.length : slotCount;
    }

    public static int pages(int length, int pageSize) {
        return Math.max(1, (int) (((long) length + pageSize - 1) / pageSize));
    }

    public static int pageLength(int length, int pageSize, int page) {
        return Math.max(0, Math.min(pageSize, length - page * pageSize));
    }

    public static int maxFirst(int pageLength) {
        return Math.max(0, (pageLength + 5) / 6 - 9) * 6;
    }

    public static int clampFirst(int value, int pageLength) {
        int rounded = Math.max(0, value) / 6 * 6;
        return Math.min(rounded, maxFirst(pageLength));
    }

    public void setPosition(int slotCount, int requestedPage, int requestedFirst) {
        int length = length(slotCount);
        int pages = pages(length, pageSize);
        page = Math.max(0, Math.min(pages - 1, requestedPage));
        first = clampFirst(requestedFirst, pageLength(length, pageSize, page));
    }

    public int realIndex(int position, int slotCount) {
        if (!valid || side == null) {
            return -1;
        }
        int length = length(slotCount);
        int start = page * pageSize;
        int pageLength = pageLength(length, pageSize, page);
        if (first + position >= pageLength) {
            return -1;
        }
        int index = start + first + position;
        int real = filtered ? list[index] : index;
        return real >= 0 && real < slotCount ? real : -1;
    }
}
