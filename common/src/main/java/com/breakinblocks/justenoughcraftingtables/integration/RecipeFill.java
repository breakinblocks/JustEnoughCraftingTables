package com.breakinblocks.justenoughcraftingtables.integration;

import com.breakinblocks.justenoughcraftingtables.menu.ItemKey;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.net.FillRecipePayload;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.justenoughcraftingtables.platform.ClientNetwork;
import org.jspecify.annotations.Nullable;

public final class RecipeFill {
    public static final int GRID_SIZE = 9;
    public static final int MAX_SETS = 64;

    public record Requirement(Collection<Item> items, Predicate<ItemStack> test) {
    }

    public record Plan(List<ItemStack> templates, List<Integer> missing) {
        public boolean complete() {
            return missing.isEmpty();
        }
    }

    public static final class Pool {
        private final Map<ItemKey, Long> counts;
        private final Map<Item, List<ItemKey>> byItem;

        private Pool(Map<ItemKey, Long> counts) {
            this.counts = Collections.unmodifiableMap(counts);
            Map<Item, List<ItemKey>> index = new HashMap<>();
            for (ItemKey key : counts.keySet()) {
                index.computeIfAbsent(key.stack().getItem(), item -> new ArrayList<>()).add(key);
            }
            this.byItem = index;
        }

        public Map<ItemKey, Long> counts() {
            return counts;
        }

        public long count(ItemKey key) {
            return counts.getOrDefault(key, 0L);
        }

        public List<ItemKey> withItem(Item item) {
            return byItem.getOrDefault(item, List.of());
        }
    }

    private static WeakReference<StationMenu> cachedMenu = new WeakReference<>(null);
    private static int cachedRevision;
    private static int cachedSignature;

    @Nullable
    private static Pool cachedPool;

    private RecipeFill() {
    }

    public static Pool pool(StationMenu menu) {
        int revision = menu.clientRevision();
        int signature = signature(menu);
        if (cachedPool != null && cachedMenu.get() == menu && cachedRevision == revision && cachedSignature == signature) {
            return cachedPool;
        }
        Map<ItemKey, Long> counts = new LinkedHashMap<>();
        for (ItemStack stack : menu.availableStacks()) {
            if (!stack.isEmpty()) {
                counts.merge(ItemKey.of(stack), (long) stack.getCount(), Long::sum);
            }
        }
        Pool pool = new Pool(counts);
        cachedMenu = new WeakReference<>(menu);
        cachedRevision = revision;
        cachedSignature = signature;
        cachedPool = pool;
        return pool;
    }

    private static int signature(StationMenu menu) {
        int hash = 1;
        for (int i = StationMenu.GRID_START; i < StationMenu.GRID_END; i++) {
            hash = hash * 31 + stackHash(menu.getSlot(i).getItem());
        }
        Inventory inventory = menu.player().getInventory();
        for (int i = 0; i < 36; i++) {
            hash = hash * 31 + stackHash(inventory.getItem(i));
        }
        return hash * 31 + stackHash(menu.getCarried());
    }

    private static int stackHash(ItemStack stack) {
        return stack.isEmpty() ? 0 : ItemStack.hashItemAndComponents(stack) * 31 + stack.getCount();
    }

    public static Plan plan(StationMenu menu, List<Requirement> requirements) {
        Pool pool = pool(menu);
        Map<ItemKey, Integer> inGrid = new HashMap<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = gridItem(menu, i);
            if (!stack.isEmpty()) {
                inGrid.merge(ItemKey.of(stack), stack.getCount(), Integer::sum);
            }
        }
        List<List<ItemKey>> candidates = new ArrayList<>(GRID_SIZE);
        for (int i = 0; i < GRID_SIZE; i++) {
            Requirement requirement = i < requirements.size() ? requirements.get(i) : null;
            Set<ItemKey> options = new LinkedHashSet<>();
            if (requirement != null) {
                for (Item item : requirement.items()) {
                    for (ItemKey key : pool.withItem(item)) {
                        if (requirement.test().test(key.stack())) {
                            options.add(key);
                        }
                    }
                }
            }
            ItemKey inSlot = ItemKey.view(gridItem(menu, i));
            List<ItemKey> ordered = new ArrayList<>(options);
            ordered.sort((left, right) -> {
                int preferred = Boolean.compare(right.equals(inSlot), left.equals(inSlot));
                if (preferred == 0) {
                    preferred = Boolean.compare(inGrid.containsKey(right), inGrid.containsKey(left));
                }
                return preferred != 0 ? preferred : Long.compare(pool.count(right), pool.count(left));
            });
            candidates.add(ordered);
        }
        ItemKey[] chosen = new ItemKey[GRID_SIZE];
        List<Integer> missing = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            if (i < requirements.size() && requirements.get(i) != null
                    && !assign(i, candidates, chosen, pool, new boolean[GRID_SIZE])) {
                missing.add(i);
            }
        }
        List<ItemStack> templates = new ArrayList<>(GRID_SIZE);
        for (ItemKey key : chosen) {
            templates.add(key == null ? ItemStack.EMPTY : key.stack().copyWithCount(1));
        }
        return new Plan(List.copyOf(templates), List.copyOf(missing));
    }

    private static boolean assign(int slot, List<List<ItemKey>> candidates, ItemKey[] chosen, Pool pool, boolean[] visited) {
        if (visited[slot]) {
            return false;
        }
        visited[slot] = true;
        for (ItemKey key : candidates.get(slot)) {
            int used = 0;
            for (ItemKey assigned : chosen) {
                if (key.equals(assigned)) {
                    used++;
                }
            }
            if (used < pool.count(key)) {
                chosen[slot] = key;
                return true;
            }
            for (int other = 0; other < GRID_SIZE; other++) {
                if (key.equals(chosen[other]) && assign(other, candidates, chosen, pool, visited)) {
                    chosen[slot] = key;
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack gridItem(StationMenu menu, int index) {
        return menu.getSlot(StationMenu.GRID_START + index).getItem();
    }

    public static int currentSets(StationMenu menu, List<ItemStack> templates) {
        int sets = Integer.MAX_VALUE;
        boolean any = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack template = i < templates.size() ? templates.get(i) : ItemStack.EMPTY;
            ItemStack current = gridItem(menu, i);
            if (template.isEmpty()) {
                if (!current.isEmpty()) {
                    return 0;
                }
                continue;
            }
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, template)) {
                return 0;
            }
            sets = Math.min(sets, current.getCount());
            any = true;
        }
        return any ? sets : 0;
    }

    public static boolean gridMatches(StationMenu menu, List<ItemStack> templates) {
        return currentSets(menu, templates) > 0;
    }

    public static int targetSets(StationMenu menu, List<ItemStack> templates, long extra) {
        if (extra <= 0 || extra >= MAX_SETS) {
            return 0;
        }
        long target = currentSets(menu, templates) + extra;
        return (int) Math.min(MAX_SETS, target);
    }

    public static void send(StationMenu menu, List<ItemStack> templates, int sets) {
        ClientNetwork.sendToServer(new FillRecipePayload(menu.containerId, templates, sets));
    }
}
