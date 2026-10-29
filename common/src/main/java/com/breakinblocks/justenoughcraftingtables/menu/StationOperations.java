package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class StationOperations {
    public enum FillResult {
        FILLED,
        NOTHING,
        NOT_ENOUGH,
        NO_ROOM
    }

    private StationOperations() {
    }

    public static int store(StationMenu menu) {
        StationBlockEntity station = menu.station();
        int leftover = 0;
        menu.beginGridBatch();
        try {
            for (int slot = 0; slot < StationBlockEntity.GRID_SIZE; slot++) {
                storeSlot(menu, slot, false);
                leftover += station.getGridItem(slot).getCount();
            }
        } finally {
            menu.endGridBatch();
        }
        return leftover;
    }

    private static int returnTo(StationMenu menu, Origin origin, ItemStack stack) {
        ItemStorage target;
        if (origin.isPlayer()) {
            target = menu.playerInventoryHandler();
        } else {
            target = menu.storeTarget(origin.side());
        }
        if (target == null) {
            return 0;
        }
        ItemStack remainder = ItemStorage.insertItemStacked(target, stack, false);
        int accepted = stack.getCount() - remainder.getCount();
        if (accepted > 0 && origin.side() != null) {
            menu.markSideModified(origin.side());
        }
        return accepted;
    }

    public static boolean storeSlot(StationMenu menu, int slot, boolean fallbackToPlayer) {
        StationBlockEntity station = menu.station();
        GridOrigins origins = station.origins();
        ItemStack stack = station.getGridItem(slot);
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack remaining = stack.copy();
        List<OriginEntry> kept = new ArrayList<>();
        for (OriginEntry entry : origins.get(slot)) {
            int amount = Math.min(entry.count(), remaining.getCount());
            if (amount <= 0) {
                continue;
            }
            int accepted = returnTo(menu, entry.origin(), remaining.copyWithCount(amount));
            remaining.shrink(accepted);
            if (amount - accepted > 0) {
                kept.add(entry.withCount(amount - accepted));
            }
        }
        if (fallbackToPlayer && !remaining.isEmpty()) {
            ItemStack remainder = ItemStorage.insertItemStacked(menu.playerInventoryHandler(), remaining, false);
            int accepted = remaining.getCount() - remainder.getCount();
            if (accepted > 0) {
                GridOrigins.removeOldest(kept, accepted);
                remaining = remainder;
            }
        }
        ItemStack result = remaining.isEmpty() ? ItemStack.EMPTY : remaining;
        station.grid().set(slot, result);
        origins.set(slot, result, kept);
        station.gridChanged();
        return result.isEmpty();
    }

    public static void clear(StationMenu menu) {
        StationBlockEntity station = menu.station();
        menu.beginGridBatch();
        try {
            for (int slot = 0; slot < StationBlockEntity.GRID_SIZE; slot++) {
                ItemStack stack = station.getGridItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                menu.moveToPlayerInventory(stack);
                if (stack.isEmpty()) {
                    station.grid().set(slot, ItemStack.EMPTY);
                }
                station.gridChanged();
            }
        } finally {
            menu.endGridBatch();
        }
    }

    private record Chunk(ItemKey key, Origin origin, int count) {
        Chunk withCount(int newCount) {
            return new Chunk(key, origin, newCount);
        }
    }

    private record Portion(PlannedHandler planner, int slot, int count) {
    }

    public static FillResult fill(StationMenu menu, List<ItemStack> templates, int target) {
        StationBlockEntity station = menu.station();
        GridOrigins origins = station.origins();
        ItemKey[] pattern = new ItemKey[9];
        Map<ItemKey, Integer> uses = new LinkedHashMap<>();
        for (int i = 0; i < 9; i++) {
            ItemStack template = i < templates.size() ? templates.get(i) : ItemStack.EMPTY;
            if (template.isEmpty()) {
                continue;
            }
            ItemKey key = ItemKey.of(template);
            pattern[i] = key;
            uses.merge(key, 1, Integer::sum);
        }
        if (uses.isEmpty()) {
            return FillResult.NOTHING;
        }
        List<List<Chunk>> gridPool = new ArrayList<>();
        Map<ItemKey, Integer> gridCounts = new HashMap<>();
        for (int slot = 0; slot < 9; slot++) {
            List<Chunk> chunks = new ArrayList<>();
            ItemStack stack = station.getGridItem(slot);
            if (!stack.isEmpty()) {
                ItemKey key = ItemKey.of(stack);
                int total = 0;
                for (OriginEntry entry : origins.get(slot)) {
                    int amount = Math.min(entry.count(), stack.getCount() - total);
                    if (amount > 0) {
                        chunks.add(new Chunk(key, entry.origin(), amount));
                        total += amount;
                    }
                }
                if (total < stack.getCount()) {
                    chunks.add(new Chunk(key, Origin.PLAYER, stack.getCount() - total));
                }
                gridCounts.merge(key, stack.getCount(), Integer::sum);
            }
            gridPool.add(chunks);
        }
        List<Source> sources = menu.sources(menu.sourceOrder());

        int sets = Integer.MAX_VALUE;
        Map<ItemKey, List<Portion>> probe = planSources(sources, uses, gridCounts, key -> key.stack().getMaxStackSize());
        for (Map.Entry<ItemKey, Integer> entry : uses.entrySet()) {
            ItemKey key = entry.getKey();
            int perSet = entry.getValue();
            int available = gridCounts.getOrDefault(key, 0) + total(probe.get(key));
            sets = Math.min(sets, Math.min(key.stack().getMaxStackSize(), available / perSet));
        }
        if (target > 0) {
            sets = Math.min(sets, target);
        }
        if (sets <= 0) {
            return FillResult.NOT_ENOUGH;
        }
        final int finalSets = sets;
        Map<ItemKey, List<Portion>> plan = planSources(sources, uses, gridCounts, key -> finalSets);

        List<List<OriginEntry>> newEntries = new ArrayList<>();
        ItemStack[] newStacks = new ItemStack[9];
        Map<ItemKey, Integer> portionIndex = new HashMap<>();
        Map<ItemKey, Integer> portionUsed = new HashMap<>();
        for (int slot = 0; slot < 9; slot++) {
            List<OriginEntry> entries = new ArrayList<>();
            newEntries.add(entries);
            ItemKey key = pattern[slot];
            if (key == null) {
                newStacks[slot] = ItemStack.EMPTY;
                continue;
            }
            newStacks[slot] = key.stack().copyWithCount(sets);
            int need = takeFromPool(gridPool.get(slot), key, sets, entries);
            for (int other = 0; other < 9 && need > 0; other++) {
                if (other != slot) {
                    need = takeFromPool(gridPool.get(other), key, need, entries);
                }
            }
            if (need > 0) {
                List<Portion> portions = plan.getOrDefault(key, List.of());
                int index = portionIndex.getOrDefault(key, 0);
                int used = portionUsed.getOrDefault(key, 0);
                while (need > 0 && index < portions.size()) {
                    Portion portion = portions.get(index);
                    int take = Math.min(need, portion.count() - used);
                    GridOrigins.appendTo(entries, portion.planner().origin(), take);
                    need -= take;
                    used += take;
                    if (used >= portion.count()) {
                        index++;
                        used = 0;
                    }
                }
                portionIndex.put(key, index);
                portionUsed.put(key, used);
                if (need > 0) {
                    return FillResult.NOT_ENOUGH;
                }
            }
        }

        try (InventoryTransaction transaction = new InventoryTransaction(menu)) {
            for (Map.Entry<ItemKey, List<Portion>> entry : plan.entrySet()) {
                for (Portion portion : entry.getValue()) {
                    List<ItemStack> got = transaction.extract(portion.planner(), portion.slot(), entry.getKey().stack(), portion.count());
                    int count = 0;
                    boolean wrong = false;
                    for (ItemStack stack : got) {
                        if (!ItemStack.isSameItemSameComponents(stack, entry.getKey().stack())) {
                            wrong = true;
                        }
                        count += stack.getCount();
                    }
                    if (wrong || count != portion.count()) {
                        transaction.rollback();
                        return FillResult.NOT_ENOUGH;
                    }
                    if (portion.planner().origin().side() != null) {
                        menu.markSideModified(portion.planner().origin().side());
                    }
                }
            }

            Map<Origin, PlannedHandler> returnPlanners = new HashMap<>();
            PlannedHandler playerReturns = new PlannedHandler(menu.playerInventoryHandler(), Origin.PLAYER);
            for (List<Chunk> chunks : gridPool) {
                for (Chunk chunk : chunks) {
                    if (chunk.count() <= 0) {
                        continue;
                    }
                    int left = chunk.count();
                    if (!chunk.origin().isPlayer()) {
                        ItemStorage side = menu.storeTarget(chunk.origin().side());
                        if (side != null) {
                            PlannedHandler planner = returnPlanners.computeIfAbsent(chunk.origin(), origin -> new PlannedHandler(side, origin));
                            int accepted = planner.planInsertStacked(chunk.key().stack(), left);
                            if (accepted > 0) {
                                left -= accepted;
                            }
                        }
                    }
                    if (left > 0) {
                        int accepted = playerReturns.planInsertStacked(chunk.key().stack(), left);
                        if (accepted > 0) {
                            left -= accepted;
                        }
                    }
                    if (left > 0) {
                        return FillResult.NO_ROOM;
                    }
                }
            }

            for (PlannedHandler planner : returnPlanners.values()) {
                if (!planner.executeInserts().isEmpty()) {
                    return FillResult.NO_ROOM;
                }
                if (planner.origin().side() != null) {
                    menu.markSideModified(planner.origin().side());
                }
            }
            if (!playerReturns.executeInserts().isEmpty()) {
                return FillResult.NO_ROOM;
            }
            menu.beginGridBatch();
            try {
                for (int slot = 0; slot < 9; slot++) {
                    station.grid().set(slot, newStacks[slot]);
                    origins.set(slot, newStacks[slot], newEntries.get(slot));
                }
                transaction.commit();
                station.gridChanged();
            } finally {
                menu.endGridBatch();
            }
            return FillResult.FILLED;
        }
    }

    private static int takeFromPool(List<Chunk> chunks, ItemKey key, int need, List<OriginEntry> into) {
        int left = need;
        for (int i = 0; i < chunks.size() && left > 0; i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.count() <= 0 || !chunk.key().equals(key)) {
                continue;
            }
            int take = Math.min(left, chunk.count());
            GridOrigins.appendTo(into, chunk.origin(), take);
            chunks.set(i, chunk.withCount(chunk.count() - take));
            left -= take;
        }
        return left;
    }

    private static int total(@Nullable List<Portion> portions) {
        if (portions == null) {
            return 0;
        }
        int sum = 0;
        for (Portion portion : portions) {
            sum += portion.count();
        }
        return sum;
    }

    private static Map<ItemKey, List<Portion>> planSources(
            List<Source> sources, Map<ItemKey, Integer> uses, Map<ItemKey, Integer> gridCounts, ToIntFunction<ItemKey> setsFor) {
        Map<ItemKey, Integer> need = new HashMap<>();
        for (Map.Entry<ItemKey, Integer> entry : uses.entrySet()) {
            int wanted = setsFor.applyAsInt(entry.getKey()) * entry.getValue() - gridCounts.getOrDefault(entry.getKey(), 0);
            if (wanted > 0) {
                need.put(entry.getKey(), wanted);
            }
        }
        Map<ItemKey, List<Portion>> plan = new LinkedHashMap<>();
        if (need.isEmpty()) {
            return plan;
        }
        for (Source source : sources) {
            PlannedHandler planner = new PlannedHandler(source.handler(), source.origin());
            ItemStorage handler = source.handler();
            int slots = handler.getSlots();
            for (int slot = 0; slot < slots && !need.isEmpty(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemKey key = ItemKey.view(stack);
                Integer wanted = need.get(key);
                if (wanted == null) {
                    continue;
                }
                ItemKey stored = null;
                for (ItemKey candidate : need.keySet()) {
                    if (candidate.equals(key)) {
                        stored = candidate;
                        break;
                    }
                }
                int planned = planner.planExtract(slot, stored.stack(), wanted);
                if (planned <= 0) {
                    continue;
                }
                plan.computeIfAbsent(stored, ignored -> new ArrayList<>()).add(new Portion(planner, slot, planned));
                if (planned >= wanted) {
                    need.remove(stored);
                } else {
                    need.put(stored, wanted - planned);
                }
            }
            if (need.isEmpty()) {
                break;
            }
        }
        return plan;
    }

    @Nullable
    public static Pulled pullOne(StationMenu menu, ItemStack template, PullHints hints) {
        List<Source> sources = menu.sources(menu.sourceOrder());
        ItemKey key = ItemKey.of(template);
        int[] hint = hints.get(key);
        for (int s = hint[0]; s < sources.size(); s++) {
            Source source = sources.get(s);
            ItemStorage handler = source.handler();
            int slots = handler.getSlots();
            for (int slot = s == hint[0] ? hint[1] : 0; slot < slots; slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, template)) {
                    continue;
                }
                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (extracted.isEmpty()) {
                    continue;
                }
                if (!ItemStack.isSameItemSameComponents(extracted, template)) {
                    ItemStack remainder = handler.insertItem(slot, extracted, false);
                    if (!remainder.isEmpty()) {
                        menu.player().getInventory().placeItemBackInInventory(remainder, Prediction.SERVER_ONLY);
                    }
                    continue;
                }
                hint[0] = s;
                hint[1] = slot;
                Direction side = source.origin().side();
                if (side != null) {
                    menu.markSideModified(side);
                }
                return new Pulled(extracted, source.origin());
            }
        }
        hint[0] = sources.size();
        hint[1] = 0;
        return null;
    }

    public record Pulled(ItemStack stack, Origin origin) {
    }

    public static final class PullHints {
        private final Map<ItemKey, int[]> hints = new HashMap<>();

        int[] get(ItemKey key) {
            return hints.computeIfAbsent(key, ignored -> new int[] {0, 0});
        }
    }

    public static Component nowhereMessage(int count) {
        return Component.translatable("justenoughcraftingtables.store.nowhere", count);
    }
}
