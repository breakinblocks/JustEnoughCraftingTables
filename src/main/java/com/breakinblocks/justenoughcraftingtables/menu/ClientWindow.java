package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewStatePayload;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ClientWindow {
    public static final int SIZE = 54;
    private static final int CACHE_LIMIT = 8192;

    private final ItemStack[] stacks = new ItemStack[SIZE];
    private final int[] real = new int[SIZE];
    private final Map<Long, ItemStack> cache = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, ItemStack> eldest) {
            return size() > CACHE_LIMIT;
        }
    };
    private final Handler handler = new Handler();

    @Nullable
    private Direction side;

    private int length;
    private int pages = 1;
    private int page;
    private int first;
    private boolean filtered;

    private boolean pending;
    private int pendingRequestId;
    private int nextRequestId = 1;

    private ViewPayload.@Nullable Request pendingRequest;

    @Nullable
    private Direction predictedSide;

    private int predictedPage;
    private int predictedFirst;
    private int predictedLength;
    private boolean predictedFiltered;
    private boolean allowInsert = true;
    private int revision;

    public ClientWindow() {
        Arrays.fill(stacks, ItemStack.EMPTY);
        Arrays.fill(real, -1);
    }

    public void setAllowInsert(boolean allowInsert) {
        this.allowInsert = allowInsert;
    }

    public void initialise(@Nullable Direction initialSide, int slots, int pageSize) {
        side = initialSide;
        length = initialSide == null ? 0 : slots;
        pages = ServerView.pages(length, pageSize);
        page = 0;
        first = 0;
        filtered = false;
        int pageLength = ServerView.pageLength(length, pageSize, 0);
        for (int k = 0; k < SIZE; k++) {
            real[k] = initialSide != null && k < pageLength ? k : -1;
        }
        predictedSide = side;
        predictedPage = 0;
        predictedFirst = 0;
        predictedLength = length;
        predictedFiltered = false;
        revision++;
    }

    private static long key(Direction direction, int index) {
        return ((long) direction.get3DDataValue() << 32) | (index & 0xFFFFFFFFL);
    }

    public ItemStorage handler() {
        return handler;
    }

    public int revision() {
        return revision;
    }

    public boolean pending() {
        return pending;
    }

    @Nullable
    public Direction side() {
        return side;
    }

    public int length() {
        return length;
    }

    public int pages() {
        return pages;
    }

    public int page() {
        return page;
    }

    public int first() {
        return first;
    }

    public boolean filtered() {
        return filtered;
    }

    @Nullable
    public Direction displaySide() {
        return pending ? predictedSide : side;
    }

    public int displayPage() {
        return pending ? predictedPage : page;
    }

    public int displayFirst() {
        return pending ? predictedFirst : first;
    }

    public int displayLength() {
        return pending ? predictedLength : length;
    }

    public boolean displayFiltered() {
        return pending ? predictedFiltered : filtered;
    }

    public ViewPayload.@Nullable Request pendingRequest() {
        return pending ? pendingRequest : null;
    }

    public int realIndex(int position) {
        return real[position];
    }

    public boolean active(int position) {
        return !pending && real[position] >= 0;
    }

    public ItemStack stack(int position) {
        return stacks[position];
    }

    public void setStack(int position, ItemStack stack) {
        stacks[position] = stack;
        if (side != null && real[position] >= 0) {
            cache.put(key(side, real[position]), stack.copy());
        }
        revision++;
    }

    public ItemStack preview(int position, int pageSize) {
        if (!pending) {
            return stacks[position];
        }
        if (predictedSide == null || predictedFiltered) {
            return ItemStack.EMPTY;
        }
        int pageLength = ServerView.pageLength(predictedLength, pageSize, predictedPage);
        if (predictedFirst + position >= pageLength) {
            return ItemStack.EMPTY;
        }
        int index = predictedPage * pageSize + predictedFirst + position;
        ItemStack cached = cache.get(key(predictedSide, index));
        return cached == null ? ItemStack.EMPTY : cached;
    }

    public boolean previewSlot(int position, int pageSize) {
        if (!pending) {
            return false;
        }
        if (predictedSide == null) {
            return false;
        }
        if (predictedFiltered) {
            return false;
        }
        int pageLength = ServerView.pageLength(predictedLength, pageSize, predictedPage);
        return predictedFirst + position < pageLength;
    }

    public int beginRequest(ViewPayload.Request request, int predictedLength) {
        int id = nextRequestId++;
        if (nextRequestId <= 0) {
            nextRequestId = 1;
        }
        pending = true;
        pendingRequestId = id;
        pendingRequest = request;
        predictedSide = request.side().orElse(null);
        predictedPage = request.page();
        predictedFirst = request.first();
        predictedFiltered = request.filter().isPresent();
        this.predictedLength = predictedLength;
        revision++;
        return id;
    }

    public int resendId() {
        int id = nextRequestId++;
        if (nextRequestId <= 0) {
            nextRequestId = 1;
        }
        pendingRequestId = id;
        return id;
    }

    public void apply(ViewStatePayload payload) {
        ViewStatePayload.Info info = payload.info();
        side = info.side().orElse(null);
        length = info.length();
        pages = info.pages();
        page = info.page();
        first = info.first();
        filtered = info.filtered();
        int[] indexes = payload.realIndexes();
        List<ItemStack> received = payload.stacks();
        for (int k = 0; k < SIZE; k++) {
            real[k] = k < indexes.length ? indexes[k] : -1;
            stacks[k] = k < received.size() ? received.get(k) : ItemStack.EMPTY;
            if (side != null && real[k] >= 0) {
                cache.put(key(side, real[k]), stacks[k].copy());
            }
        }
        if (payload.requestId() != 0 && payload.requestId() == pendingRequestId) {
            pending = false;
            pendingRequest = null;
        }
        if (!pending) {
            predictedSide = side;
            predictedPage = page;
            predictedFirst = first;
            predictedLength = length;
            predictedFiltered = filtered;
        }
        revision++;
    }

    private final class Handler implements ItemStorage {
        @Override
        public int getSlots() {
            return SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks[slot];
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !allowInsert) {
                return stack;
            }
            ItemStack current = stacks[slot];
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }
            int limit = stack.getMaxStackSize();
            int space = current.isEmpty() ? limit : Math.max(0, limit - current.getCount());
            int moved = Math.min(space, stack.getCount());
            if (moved <= 0) {
                return stack;
            }
            if (!simulate) {
                setStack(slot, current.isEmpty() ? stack.copyWithCount(moved) : current.copyWithCount(current.getCount() + moved));
            }
            return stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = stacks[slot];
            if (current.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int moved = Math.min(amount, current.getCount());
            if (!simulate) {
                setStack(slot, moved >= current.getCount() ? ItemStack.EMPTY : current.copyWithCount(current.getCount() - moved));
            }
            return current.copyWithCount(moved);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 99;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return allowInsert;
        }
    }
}
