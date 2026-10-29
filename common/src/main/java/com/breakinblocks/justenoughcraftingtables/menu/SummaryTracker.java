package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class SummaryTracker {
    private final Map<ItemKey, Integer> ids = new HashMap<>();
    private final Int2ObjectLinkedOpenHashMap<ItemStack> templates = new Int2ObjectLinkedOpenHashMap<>();
    private final IntLinkedOpenHashSet pendingNew = new IntLinkedOpenHashSet();
    private final IntLinkedOpenHashSet unused = new IntLinkedOpenHashSet();
    private final IntLinkedOpenHashSet removed = new IntLinkedOpenHashSet();
    private final IntOpenHashSet sent = new IntOpenHashSet();
    private int nextId;
    private int revision;
    private final SideScan[] scans = new SideScan[6];
    private int resetMask;
    private int sentPartialMask;
    private int nextSide;
    private int nextDrainSide;

    public int idOf(ItemStack stack) {
        Integer id = ids.get(ItemKey.view(stack));
        return id == null ? -1 : id;
    }

    private int idFor(ItemStack stack, int maxEntries) {
        Integer known = ids.get(ItemKey.view(stack));
        if (known != null) {
            return known;
        }
        while (templates.size() >= maxEntries && !unused.isEmpty()) {
            int id = unused.removeFirstInt();
            boolean inUse = false;
            for (SideScan scan : scans) {
                inUse |= scan != null && scan.totals.containsKey(id);
            }
            if (!inUse) {
                ItemStack old = templates.remove(id);
                if (old != null) {
                    ids.remove(ItemKey.view(old));
                    pendingNew.remove(id);
                    if (sent.remove(id)) {
                        removed.add(id);
                    }
                }
            }
        }
        if (templates.size() >= maxEntries || nextId == Integer.MAX_VALUE) {
            return -1;
        }
        int id = nextId++;
        ItemKey stored = ItemKey.of(stack);
        ids.put(stored, id);
        templates.put(id, stored.stack());
        pendingNew.add(id);
        return id;
    }

    public ItemStack template(int id) {
        return templates.getOrDefault(id, ItemStack.EMPTY);
    }

    public int entryCount() {
        return templates.size();
    }

    public int revision() {
        return revision;
    }

    public void bind(Direction direction, @Nullable ItemStorage handler) {
        int index = direction.get3DDataValue();
        SideScan current = scans[index];
        if (handler == null) {
            if (current != null) {
                unused.addAll(current.totals.keySet());
                scans[index] = null;
                resetMask |= 1 << index;
            }
            return;
        }
        if (current == null || current.handler != handler) {
            if (current != null) {
                unused.addAll(current.totals.keySet());
            }
            scans[index] = new SideScan(handler);
            resetMask |= 1 << index;
        }
    }

    public void tick(int budget, int maxEntries) {
        int active = 0;
        for (SideScan scan : scans) {
            if (scan != null) {
                active++;
            }
        }
        if (active == 0) {
            return;
        }
        int left = budget;
        for (int pass = 0; pass < 2 && left > 0; pass++) {
            int share = Math.max(1, left / active);
            for (int i = 0; i < 6 && left > 0; i++) {
                int index = (nextSide + i) % 6;
                SideScan scan = scans[index];
                if (scan == null) {
                    continue;
                }
                left -= scan.scan(this, Math.min(share, left), maxEntries);
            }
        }
        nextSide = (nextSide + 1) % 6;
    }

    @Nullable
    public SummaryPayload drain(int containerId) {
        List<SummaryPayload.NewEntry> entries = new ArrayList<>();
        IntArrayList retired = new IntArrayList();
        while (!removed.isEmpty() && retired.size() < 256) {
            retired.add(removed.removeFirstInt());
        }
        if (removed.isEmpty()) {
            while (!pendingNew.isEmpty() && entries.size() < 256) {
                int id = pendingNew.removeFirstInt();
                entries.add(new SummaryPayload.NewEntry(id, templates.get(id)));
                sent.add(id);
            }
        }
        List<SummaryPayload.Total> totals = new ArrayList<>();
        int partialMask = 0;
        for (int offset = 0; offset < 6; offset++) {
            int i = (nextDrainSide + offset) % 6;
            SideScan scan = scans[i];
            if (scan == null) {
                continue;
            }
            if (scan.partial || !scan.complete || !pendingNew.isEmpty()) {
                partialMask |= 1 << i;
            }
            var dirty = scan.dirty.iterator();
            while (dirty.hasNext() && totals.size() < 2048) {
                int id = dirty.nextInt();
                if (!templates.containsKey(id)) {
                    dirty.remove();
                } else if (sent.contains(id)) {
                    totals.add(new SummaryPayload.Total(id, i, scan.totals.get(id), scan.available.get(id)));
                    dirty.remove();
                }
            }
        }
        if (entries.isEmpty() && retired.isEmpty() && totals.isEmpty() && resetMask == 0 && partialMask == sentPartialMask) {
            return null;
        }
        SummaryPayload payload = new SummaryPayload(containerId, entries, totals, partialMask, resetMask, retired.toIntArray());
        resetMask = 0;
        nextDrainSide = (nextDrainSide + 1) % 6;
        sentPartialMask = partialMask;
        return payload;
    }

    private static final class SideScan {
        private final ItemStorage handler;
        private final Int2LongOpenHashMap totals = new Int2LongOpenHashMap();
        private final Int2LongOpenHashMap available = new Int2LongOpenHashMap();
        private final IntLinkedOpenHashSet dirty = new IntLinkedOpenHashSet();
        private int[] slotIds = new int[0];
        private int[] slotCounts = new int[0];
        private int[] slotAvailable = new int[0];
        private int cursor;
        private boolean partial;
        private boolean partialThisRound;
        private boolean complete;

        private SideScan(ItemStorage handler) {
            this.handler = handler;
        }

        private int scan(SummaryTracker tracker, int budget, int maxEntries) {
            int slots = handler.getSlots();
            resize(tracker, slots);
            if (slots == 0) {
                return 0;
            }
            int done = Math.min(budget, slots);
            for (int i = 0; i < done; i++) {
                if (cursor >= slots) {
                    cursor = 0;
                    partial = partialThisRound;
                    partialThisRound = false;
                }
                scanSlot(tracker, cursor, maxEntries);
                cursor++;
                if (cursor == slots) {
                    complete = true;
                    partial = partialThisRound;
                }
            }
            return done;
        }

        private void scanSlot(SummaryTracker tracker, int slot, int maxEntries) {
            ItemStack stack = handler.getStackInSlot(slot);
            int oldId = slotIds[slot];
            int oldCount = slotCounts[slot];
            int accessible = 0;
            if (!stack.isEmpty()) {
                int probe = Math.min(stack.getCount(), stack.getMaxStackSize());
                ItemStack extracted = handler.extractItem(slot, probe, true);
                if (ItemStack.isSameItemSameComponents(stack, extracted)) {
                    accessible = extracted.getCount() == probe ? stack.getCount() : extracted.getCount();
                }
            }
            int known = stack.isEmpty() ? -1 : tracker.idOf(stack);
            if (oldId == known && oldCount == stack.getCount() && slotAvailable[slot] == accessible) {
                return;
            }
            removeSlot(tracker, slot);
            int newId = stack.isEmpty() ? -1 : tracker.idFor(stack, maxEntries);
            if (newId >= 0) {
                totals.addTo(newId, stack.getCount());
                available.addTo(newId, accessible);
                dirty.add(newId);
                slotIds[slot] = newId;
                slotCounts[slot] = stack.getCount();
                slotAvailable[slot] = accessible;
            } else if (!stack.isEmpty()) {
                partialThisRound = true;
            }
            tracker.revision++;
        }

        private void removeSlot(SummaryTracker tracker, int slot) {
            int id = slotIds[slot];
            if (id >= 0) {
                long left = totals.addTo(id, -slotCounts[slot]) - slotCounts[slot];
                available.addTo(id, -slotAvailable[slot]);
                if (left <= 0) {
                    totals.remove(id);
                    available.remove(id);
                    tracker.unused.add(id);
                }
                dirty.add(id);
            }
            slotIds[slot] = -1;
            slotCounts[slot] = 0;
            slotAvailable[slot] = 0;
        }

        private void resize(SummaryTracker tracker, int slots) {
            int old = slotIds.length;
            if (old == slots) {
                return;
            }
            complete = false;
            if (slots < old) {
                for (int i = slots; i < old; i++) {
                    removeSlot(tracker, i);
                    tracker.revision++;
                }
            }
            slotIds = Arrays.copyOf(slotIds, slots);
            slotCounts = Arrays.copyOf(slotCounts, slots);
            slotAvailable = Arrays.copyOf(slotAvailable, slots);
            for (int i = old; i < slots; i++) {
                slotIds[i] = -1;
            }
            if (cursor >= slots) {
                cursor = 0;
            }
        }
    }
}
