package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongMaps;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class ClientSummary {
    private final Int2ObjectMap<ItemStack> templates = new Int2ObjectOpenHashMap<>();
    private final Map<ItemKey, Integer> ids = new HashMap<>();
    private final Int2LongOpenHashMap[] totals = new Int2LongOpenHashMap[6];
    private final Int2LongOpenHashMap[] available = new Int2LongOpenHashMap[6];
    private int partialMask;
    private int revision;

    public ClientSummary() {
        for (int i = 0; i < 6; i++) {
            totals[i] = new Int2LongOpenHashMap();
            available[i] = new Int2LongOpenHashMap();
        }
    }

    public void apply(SummaryPayload payload) {
        for (int i = 0; i < 6; i++) {
            if ((payload.resetMask() & (1 << i)) != 0) {
                totals[i].clear();
                available[i].clear();
            }
        }
        for (int id : payload.removedIds()) {
            ItemStack old = templates.remove(id);
            if (old != null) {
                ids.remove(ItemKey.view(old));
            }
            for (int i = 0; i < 6; i++) {
                totals[i].remove(id);
                available[i].remove(id);
            }
        }
        for (SummaryPayload.NewEntry entry : payload.entries()) {
            ItemStack template = entry.template().copyWithCount(1);
            templates.put(entry.id(), template);
            ids.put(ItemKey.of(template), entry.id());
        }
        for (SummaryPayload.Total total : payload.totals()) {
            if (total.side() < 0 || total.side() >= 6) {
                continue;
            }
            if (total.total() <= 0) {
                totals[total.side()].remove(total.id());
                available[total.side()].remove(total.id());
            } else {
                totals[total.side()].put(total.id(), total.total());
                available[total.side()].put(total.id(), Math.max(0, Math.min(total.total(), total.available())));
            }
        }
        partialMask = payload.partialMask();
        revision++;
    }

    public void clearSide(Direction side) {
        totals[side.get3DDataValue()].clear();
        available[side.get3DDataValue()].clear();
        revision++;
    }

    public int revision() {
        return revision;
    }

    public boolean partial(Direction side) {
        return (partialMask & (1 << side.get3DDataValue())) != 0;
    }

    public ItemStack template(int id) {
        return templates.getOrDefault(id, ItemStack.EMPTY);
    }

    public int idOf(ItemStack stack) {
        Integer id = ids.get(ItemKey.view(stack));
        return id == null ? -1 : id;
    }

    public Int2LongMap totals(Direction side) {
        return Int2LongMaps.unmodifiable(totals[side.get3DDataValue()]);
    }

    public Int2LongMap available(Direction side) {
        return Int2LongMaps.unmodifiable(available[side.get3DDataValue()]);
    }

    public long total(Direction side, ItemStack stack) {
        int id = idOf(stack);
        return id < 0 ? 0 : available[side.get3DDataValue()].get(id);
    }

    public IntSet matching(Predicate<ItemStack> predicate) {
        IntSet out = new IntOpenHashSet();
        for (Int2ObjectMap.Entry<ItemStack> entry : templates.int2ObjectEntrySet()) {
            if (predicate.test(entry.getValue())) {
                out.add(entry.getIntKey());
            }
        }
        return out;
    }

    public long sideTotal(Direction side, IntSet idSet) {
        long sum = 0;
        for (Int2LongMap.Entry entry : totals[side.get3DDataValue()].int2LongEntrySet()) {
            if (idSet.contains(entry.getIntKey())) {
                sum += entry.getLongValue();
            }
        }
        return sum;
    }
}
