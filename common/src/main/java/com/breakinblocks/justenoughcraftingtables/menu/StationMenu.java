package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.common.block.StationBlocks;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.net.FillRecipePayload;
import com.breakinblocks.justenoughcraftingtables.net.SidesPayload;
import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewStatePayload;
import com.breakinblocks.justenoughcraftingtables.platform.Platform;
import com.breakinblocks.justenoughcraftingtables.platform.StorageLookup;
import com.breakinblocks.justenoughcraftingtables.platform.StorageTransaction;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public class StationMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;
    public static final int GRID_START = 1;
    public static final int GRID_END = 10;
    public static final int WINDOW_START = 10;
    public static final int WINDOW_END = 64;
    public static final int INVENTORY_START = 64;
    public static final int HOTBAR_START = 91;
    public static final int INVENTORY_END = 100;
    public static final int PANEL_LEFT = -130;
    public static final int PANEL_RIGHT = 175;

    public static final int BUTTON_CLEAR = 0;
    public static final int BUTTON_STORE = 7;
    public static final int BUTTON_REPEAT_OFF = 8;
    public static final int BUTTON_REPEAT_ON = 9;
    public static final int BUTTON_ORDER_PLAYER = 10;
    public static final int BUTTON_ORDER_CONNECTED = 11;
    public static final int BUTTON_HIDE_FIRST = 12;

    private final boolean clientSide;
    private final Player player;
    private final PlayerStorage playerStorage;
    private final Level level;
    private final BlockPos pos;
    private final int pageSize;
    private final boolean allowInsert;
    private final CraftingContainer grid;
    private final ResultContainer result = new ResultContainer();
    private final WindowContainer windowContainer = new WindowContainer(this);
    private final WindowSlot[] windowSlots = new WindowSlot[ClientWindow.SIZE];

    private List<SideEntry> sides = List.of();
    private int sidesVersion = 1;

    @Nullable
    private final StationBlockEntity station;

    private final ServerSide[] serverSides = new ServerSide[6];
    private final CacheHolder[] caches = new CacheHolder[6];
    private final List<Direction> discoveryOrder = new ArrayList<>();

    @Nullable
    private final ServerView view;

    @Nullable
    private final SummaryTracker summary;

    private boolean rediscover;
    private long lastTick = Long.MIN_VALUE;
    private long lastFillTick = Long.MIN_VALUE;
    private final ViewRequests viewRequests = new ViewRequests();
    private ItemStack accessKey = ItemStack.EMPTY;
    @Nullable
    private ViewScan viewScan;
    private ViewRequests.@Nullable Pending scanningRequest;
    private long lastScanTick = Long.MIN_VALUE;
    @Nullable
    private int[] serverFilter;
    private Collection<RecipeHolder<?>> recipeSnapshot = List.of();

    private int summaryTimer;
    private boolean closed;
    private SourceOrder sourceOrder = SourceOrder.PLAYER_FIRST;
    private boolean repeatCraft;

    @Nullable
    private RecipeHolder<CraftingRecipe> lastRecipe;

    private final List<OriginEntry> cursorEntries = new ArrayList<>();
    private ItemStack cursorTracked = ItemStack.EMPTY;
    private final List<ItemStack> refunds = new ArrayList<>();
    private boolean inClick;
    private int dragMode = -1;

    @Nullable
    private final ClientWindow clientWindow;

    @Nullable
    private final ClientSummary clientSummary;

    @Nullable
    private int[] activeFilter;

    private int clientRevision;
    private int windowX = PANEL_LEFT + 13;

    public StationMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, StationOpeningData.read(data));
    }

    public StationMenu(int containerId, Inventory inventory, StationOpeningData data) {
        super(ModMenus.CRAFTING_STATION.get(), containerId);
        this.clientSide = true;
        this.player = inventory.player;
        this.playerStorage = new PlayerStorage(inventory);
        this.level = inventory.player.level();
        this.pos = data.pos();
        this.pageSize = data.pageSize();
        this.allowInsert = data.allowInsert();
        this.sidesVersion = data.sidesVersion();
        this.sides = data.sides();
        Direction selected = data.selected();
        this.station = null;
        this.view = null;
        this.summary = null;
        this.clientWindow = new ClientWindow();
        this.clientWindow.setAllowInsert(allowInsert);
        this.clientSummary = new ClientSummary();
        SideEntry entry = selected == null ? null : sideEntry(selected);
        this.clientWindow.initialise(entry == null ? null : selected, entry == null ? 0 : entry.slots(), pageSize);
        this.grid = new TransientCraftingContainer(this, 3, 3);
        addSlots(inventory);
    }

    public StationMenu(int containerId, Inventory inventory, StationBlockEntity station) {
        super(ModMenus.CRAFTING_STATION.get(), containerId);
        this.clientSide = false;
        this.player = inventory.player;
        this.playerStorage = new PlayerStorage(inventory);
        this.level = station.getLevel();
        this.pos = station.getBlockPos();
        this.pageSize = JustEnoughCraftingTablesConfig.pageSize();
        this.allowInsert = JustEnoughCraftingTablesConfig.SERVER.allowInsertIntoSides.get();
        this.station = station;
        this.view = new ServerView(pageSize);
        this.summary = new SummaryTracker();
        this.clientWindow = null;
        this.clientSummary = null;
        this.grid = new StationGrid(station);
        addSlots(inventory);
        runDiscovery(false);
        Direction saved = station.getSelectedSide();
        Direction initial = null;
        if (saved != null && usable(serverSides[saved.get3DDataValue()])) {
            initial = saved;
        } else {
            initial = firstUsableSide();
        }
        view.bind(initial);
        if (initial != null) {
            view.setPosition(serverSides[initial.get3DDataValue()].handler().getSlots(), 0, 0);
        }
        station.addMenu(this);
        updateResult(false);
    }

    private void addSlots(Inventory inventory) {
        addSlot(new StationResultSlot(this, inventory.player, grid, result, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(grid, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            WindowSlot slot = new WindowSlot(this, windowContainer, k, windowX + 18 * (k % 6), 17 + 18 * (k / 6));
            windowSlots[k] = slot;
            addSlot(slot);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    public void layoutWindow(int x0) {
        if (x0 == windowX) {
            return;
        }
        windowX = x0;
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            WindowSlot replacement = new WindowSlot(this, windowContainer, k, x0 + 18 * (k % 6), 17 + 18 * (k / 6));
            replacement.index = WINDOW_START + k;
            windowSlots[k] = replacement;
            slots.set(WINDOW_START + k, replacement);
        }
    }

    public StationOpeningData openingData() {
        return new StationOpeningData(pos, pageSize, allowInsert, sidesVersion, sides, view == null ? null : view.side());
    }

    public void writeOpenData(RegistryFriendlyByteBuf buffer) {
        openingData().write(buffer);
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public BlockPos pos() {
        return pos;
    }

    public Level level() {
        return level;
    }

    public Player player() {
        return player;
    }

    public int pageSize() {
        return pageSize;
    }

    public boolean allowInsert() {
        return allowInsert;
    }

    public List<SideEntry> sides() {
        return sides;
    }

    public int sidesVersion() {
        return sidesVersion;
    }

    @Nullable
    public SideEntry sideEntry(Direction direction) {
        for (SideEntry entry : sides) {
            if (entry.direction() == direction) {
                return entry;
            }
        }
        return null;
    }

    public StationBlockEntity station() {
        if (station == null) {
            throw new IllegalStateException("No station on the client menu");
        }
        return station;
    }

    public GridOrigins gridOrigins() {
        return station().origins();
    }

    @Nullable
    public ServerPlayer serverPlayer() {
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public SourceOrder sourceOrder() {
        return sourceOrder;
    }

    public boolean repeatCraft() {
        return repeatCraft;
    }

    public CraftingContainer craftingGrid() {
        return grid;
    }

    public ItemStack resultStack() {
        return result.getItem(0);
    }

    public WindowSlot windowSlot(int position) {
        return windowSlots[position];
    }

    @Nullable
    public ClientWindow clientWindow() {
        return clientWindow;
    }

    @Nullable
    public ClientSummary clientSummary() {
        return clientSummary;
    }

    public int clientRevision() {
        int revision = clientRevision;
        if (clientWindow != null) {
            revision = revision * 31 + clientWindow.revision();
        }
        if (clientSummary != null) {
            revision = revision * 31 + clientSummary.revision();
        }
        return revision;
    }

    @Nullable
    public int[] activeFilter() {
        return activeFilter;
    }

    @Nullable
    public Direction selectedSide() {
        if (clientWindow != null) {
            return clientWindow.displaySide();
        }
        return view == null ? null : view.side();
    }

    @Nullable
    public ItemStorage windowHandler() {
        if (clientSide) {
            return clientWindow.handler();
        }
        if (view == null || !view.valid() || view.side() == null) {
            return null;
        }
        ServerSide side = serverSides[view.side().get3DDataValue()];
        if (!usable(side)) {
            return null;
        }
        return side.handler();
    }

    public int windowIndex(int position) {
        if (clientSide) {
            return clientWindow.active(position) ? position : -1;
        }
        ItemStorage handler = windowHandler();
        if (handler == null) {
            return -1;
        }
        if (viewScan != null) {
            return -1;
        }
        int real = view.realIndex(position, handler.getSlots());
        if (real >= 0 && serverFilter != null) {
            int id = summary.idOf(handler.getStackInSlot(real));
            if (Arrays.binarySearch(serverFilter, id) < 0) {
                return -1;
            }
        }
        return real;
    }

    public boolean isWindowSlotActive(int position) {
        if (clientSide) {
            return clientWindow.active(position);
        }
        return windowIndex(position) >= 0;
    }

    public void setWindowStack(WindowSlot slot, ItemStack stack) {
        if (clientSide) {
            clientWindow.setStack(slot.position(), stack);
            return;
        }
        ItemStorage handler = windowHandler();
        int real = windowIndex(slot.position());
        if (handler == null || real < 0) {
            refund(stack);
            return;
        }
        ItemStack current = handler.getStackInSlot(real).copy();
        if (stack.isEmpty()) {
            if (!current.isEmpty()) {
                extractFully(handler, real, current.getCount());
            }
        } else if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack)) {
            int diff = stack.getCount() - current.getCount();
            if (diff > 0) {
                if (allowInsert && handler.isItemValid(real, stack)) {
                    refund(handler.insertItem(real, stack.copyWithCount(diff), false));
                } else {
                    refund(stack.copyWithCount(diff));
                }
            } else if (diff < 0) {
                extractFully(handler, real, -diff);
            }
        } else {
            refund(stack);
        }
        onWindowModified();
    }

    private static void extractFully(ItemStorage handler, int slot, int amount) {
        int left = amount;
        while (left > 0) {
            ItemStack extracted = handler.extractItem(slot, left, false);
            if (extracted.isEmpty()) {
                break;
            }
            left -= extracted.getCount();
        }
    }

    private void refund(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (inClick) {
            refunds.add(stack.copy());
        } else {
            player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
        }
    }

    public void onWindowModified() {
        if (clientSide || view == null || view.side() == null) {
            return;
        }
        markSideModified(view.side());
    }

    public void markSideModified(Direction direction) {
        ServerSide side = serverSides[direction.get3DDataValue()];
        if (side != null && !side.invalid()) {
            side.blockEntity().setChanged();
            side.markModified();
        }
    }

    public void beginGridBatch() {
        if (station != null) {
            station.beginBatch();
        }
    }

    public void endGridBatch() {
        if (station != null) {
            station.endBatch();
        }
    }

    private boolean usable(@Nullable ServerSide side) {
        if (side == null || !side.usable() || station != null && station.isHidden(side.direction())) {
            return false;
        }
        if (!StationAccess.allowed(player, pos, side.blockEntity())) {
            side.invalidate();
            rediscover = true;
            return false;
        }
        return true;
    }

    public void requestRediscovery() {
        rediscover = true;
    }

    @Nullable
    private Direction firstUsableSide() {
        for (Direction direction : discoveryOrder) {
            ServerSide side = serverSides[direction.get3DDataValue()];
            if (usable(side)) {
                return direction;
            }
        }
        return null;
    }

    private final class CacheHolder {
        private final StorageLookup cache;

        @Nullable
        private final Direction context;

        private boolean active = true;
        private ItemStorage raw;
        private ItemStorage guarded;
        private boolean valid;

        private CacheHolder(ServerLevel serverLevel, Direction direction, BlockPos target, @Nullable Direction context) {
            this.context = context;
            this.cache = new StorageLookup( serverLevel, target, context, () -> active && !closed, () -> onCapabilityInvalidated(direction));
        }
    }

    private void onCapabilityInvalidated(Direction direction) {
        CacheHolder holder = caches[direction.get3DDataValue()];
        if (holder != null) {
            holder.valid = false;
        }
        ServerSide side = serverSides[direction.get3DDataValue()];
        if (side != null) {
            side.invalidate();
        }
        rediscover = true;
    }

    @Nullable
    private ItemStorage queryHandler(Direction direction, BlockPos target, @Nullable Direction context) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        int index = direction.get3DDataValue();
        CacheHolder holder = caches[index];
        if (holder == null || holder.context != context) {
            if (holder != null) {
                holder.active = false;
            }
            holder = new CacheHolder(serverLevel, direction, target, context);
            caches[index] = holder;
        }
        ItemStorage raw = holder.cache.get();
        holder.valid = true;
        if (raw != holder.raw) {
            holder.raw = raw;
            BlockEntity inventory = level.getBlockEntity(target);
            CacheHolder bound = holder;
            holder.guarded = raw == null ? null : new AccessItemHandler(raw,
                    () -> bound.active && bound.valid && !closed && stillValid(player) && !station().isHidden(direction)
                            && level.getBlockEntity(target) == inventory && StationAccess.allowed(player, pos, inventory, true)
                            && bound.cache.isValid());
        }
        return holder.guarded;
    }

    private void runDiscovery(boolean send) {
        if (station == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ServerSide> found = SideDiscovery.discover(serverLevel, pos, player, station, this::queryHandler);
        ServerSide[] next = new ServerSide[6];
        List<Direction> order = new ArrayList<>();
        for (ServerSide side : found) {
            next[side.direction().get3DDataValue()] = side;
            order.add(side.direction());
        }
        boolean changed = order.size() != discoveryOrder.size();
        for (int i = 0; i < 6 && !changed; i++) {
            ServerSide before = serverSides[i];
            ServerSide after = next[i];
            if ((before == null) != (after == null)) {
                changed = true;
            } else if (before != null && (before.handler() != after.handler() || !before.entry().sameAs(after.entry()))) {
                changed = true;
            }
        }
        boolean viewReset = false;
        if (view != null && view.side() != null) {
            int index = view.side().get3DDataValue();
            ServerSide before = serverSides[index];
            ServerSide after = next[index];
            if (after == null || !after.usable() || before == null || before.handler() != after.handler()) {
                if (view.valid()) {
                    viewReset = true;
                }
                view.invalidate();
            }
        }
        for (int i = 0; i < 6; i++) {
            ServerSide before = serverSides[i];
            ServerSide after = next[i];
            if (before != null && after != null && before.handler() == after.handler() && before.takeModified()) {
                after.markModified();
            }
            serverSides[i] = after;
            if (summary != null) {
                summary.bind(Direction.from3DDataValue(i), after != null && after.usable() ? after.handler() : null);
            }
        }
        discoveryOrder.clear();
        discoveryOrder.addAll(order);
        List<SideEntry> entries = new ArrayList<>();
        for (ServerSide side : found) {
            entries.add(side.entry());
        }
        sides = List.copyOf(entries);
        if (changed || viewReset) {
            if (send) {
                sidesVersion++;
                Direction selected = view != null && view.valid() ? view.side() : firstUsableSide();
                send(new SidesPayload(containerId, sidesVersion, sides, Optional.ofNullable(selected), viewReset));
            }
        }
    }

    private void send(CustomPacketPayload payload) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null && Platform.canSend(serverPlayer, payload)) {
            Platform.send(serverPlayer, payload);
        }
    }

    @Override
    public void broadcastChanges() {
        if (!clientSide && StorageTransaction.isOpen()) {
            return;
        }
        if (!clientSide && !closed) {
            serverTick();
        }
        super.broadcastChanges();
    }

    private void serverTick() {
        long time = level.getGameTime();
        if (time == lastTick) {
            return;
        }
        lastTick = time;
        if (rediscover) {
            rediscover = false;
            runDiscovery(true);
        }
        if (!ItemStack.matches(accessKey, player.getMainHandItem())) {
            accessKey = player.getMainHandItem().copy();
            runDiscovery(true);
        }
        Collection<RecipeHolder<?>> currentRecipes = ((ServerLevel) level).getServer().getRecipeManager().getRecipes();
        if (recipeSnapshot != currentRecipes) {
            recipeSnapshot = currentRecipes;
            lastRecipe = null;
            updateResult(true);
        }
        ViewRequests.Pending pending = viewRequests.pending();
        if (pending != null && viewRequests.ready(time, rebuildsView(pending.request()), minTicksBetweenViewRebuilds())) {
            viewRequests.clear();
            if (pending.version() == sidesVersion) {
                applyViewRequest(pending.id(), pending.request(), time);
            }
        }
        advanceViewScan(time);
        boolean countsChanged = false;
        boolean viewCountChanged = false;
        for (Direction direction : discoveryOrder) {
            ServerSide side = serverSides[direction.get3DDataValue()];
            if (side == null || side.invalid()) {
                continue;
            }
            int slots = side.handler().getSlots();
            if (slots != side.entry().slots()) {
                side.setEntry(side.entry().withSlots(slots));
                countsChanged = true;
                if (view != null && view.valid() && view.side() == direction) {
                    viewCountChanged = true;
                }
            }
        }
        if (countsChanged) {
            List<SideEntry> entries = new ArrayList<>();
            for (Direction direction : discoveryOrder) {
                entries.add(serverSides[direction.get3DDataValue()].entry());
            }
            sides = List.copyOf(entries);
            sidesVersion++;
            send(new SidesPayload(containerId, sidesVersion, sides, Optional.ofNullable(view == null ? null : view.side()), false));
        }
        if (viewCountChanged) {
            ItemStorage handler = windowHandler();
            view.setPosition(handler == null ? 0 : handler.getSlots(), view.page(), view.first());
            sendViewState(0);
        }
        if (summary != null) {
            for (Direction direction : discoveryOrder) {
                ServerSide side = serverSides[direction.get3DDataValue()];
                if (!usable(side)) {
                    summary.bind(direction, null);
                }
            }
            summary.tick(JustEnoughCraftingTablesConfig.SERVER.scanBudget.get(), JustEnoughCraftingTablesConfig.SERVER.maxSummaryEntries.get());
            if (++summaryTimer >= 10) {
                summaryTimer = 0;
                SummaryPayload payload = summary.drain(containerId);
                if (payload != null) {
                    send(payload);
                }
            }
        }
        for (ServerSide side : serverSides) {
            if (side != null && side.takeModified() && !side.invalid()) {
                BlockState state = level.getBlockState(side.pos());
                level.sendBlockUpdated(side.pos(), state, state, 3);
            }
        }
    }

    public void sendViewState(int requestId) {
        if (view == null) {
            return;
        }
        ItemStorage handler = windowHandler();
        int slots = handler == null ? 0 : handler.getSlots();
        int length = handler == null ? 0 : view.length(slots);
        int pages = ServerView.pages(length, pageSize);
        int[] real = new int[ClientWindow.SIZE];
        List<ItemStack> stacks = new ArrayList<>(ClientWindow.SIZE);
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            real[k] = windowIndex(k);
            ItemStack stack = windowSlots[k].getItem();
            stacks.add(stack);
        }
        Direction side = handler == null ? null : view.side();
        ViewStatePayload.Info info = new ViewStatePayload.Info(Optional.ofNullable(side), length, pages, view.page(), view.first(), view.filtered());
        send(new ViewStatePayload(containerId, requestId, info, real, stacks));
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            setRemoteSlot(WINDOW_START + k, stacks.get(k));
        }
    }

    public void handleViewRequest(int requestId, int version, ViewPayload.Request request) {
        if (clientSide || view == null || version != sidesVersion) {
            return;
        }
        long time = level.getGameTime();
        if (requestId <= 0) {
            return;
        }
        if (viewRequests.pending() != null || !viewRequests.ready(time, rebuildsView(request), minTicksBetweenViewRebuilds())) {
            viewRequests.offer(requestId, version, request);
            return;
        }
        applyViewRequest(requestId, request, time);
    }

    private static int minTicksBetweenViewRebuilds() {
        return Math.max(1, Math.min(100, JustEnoughCraftingTablesConfig.SERVER.minTicksBetweenViewRebuilds.get()));
    }

    private boolean rebuildsView(ViewPayload.Request request) {
        Direction side = request.side().orElse(null);
        ServerSide target = side == null ? null : serverSides[side.get3DDataValue()];
        if (!usable(target)) {
            return false;
        }
        if (request.filter().isPresent() != view.filtered() || request.rebuild()) {
            return true;
        }
        return request.filter().isPresent() && (side != view.side() || !view.valid());
    }

    private void applyViewRequest(int requestId, ViewPayload.Request request, long time) {
        viewRequests.applied(time, rebuildsView(request));
        viewScan = null;
        scanningRequest = null;
        Direction side = request.side().orElse(null);
        ServerSide target = side == null ? null : serverSides[side.get3DDataValue()];
        if (!usable(target)) {
            side = null;
        }
        if (side == null) {
            view.bind(null);
            view.setUnfiltered();
            serverFilter = null;
        } else {
            boolean sideChanged = side != view.side() || !view.valid();
            boolean filterChanged = request.filter().isPresent() != view.filtered() || request.rebuild();
            view.bind(side);
            if (request.filter().isPresent()) {
                if (sideChanged || filterChanged) {
                    serverFilter = request.filter().get().clone();
                    Arrays.sort(serverFilter);
                    viewScan = new ViewScan(target.handler(), serverFilter);
                    scanningRequest = new ViewRequests.Pending(requestId, sidesVersion, request);
                    advanceViewScan(time);
                    return;
                }
            } else if (view.filtered() || sideChanged) {
                view.setUnfiltered();
                serverFilter = null;
            }
            view.setPosition(target.handler().getSlots(), request.page(), request.first());
            station().setSelectedSide(side);
        }
        sendViewState(requestId);
    }

    private void advanceViewScan(long time) {
        if (viewScan == null || scanningRequest == null || lastScanTick == time) {
            return;
        }
        lastScanTick = time;
        ServerSide side = view.side() == null ? null : serverSides[view.side().get3DDataValue()];
        if (!usable(side) || scanningRequest.version() != sidesVersion) {
            viewScan = null;
            scanningRequest = null;
            view.invalidate();
            return;
        }
        if (viewScan.advance(summary, JustEnoughCraftingTablesConfig.SERVER.scanBudget.get())) {
            ViewRequests.Pending finished = scanningRequest;
            view.setFiltered(viewScan.indexes());
            viewScan = null;
            scanningRequest = null;
            view.setPosition(side.handler().getSlots(), finished.request().page(), finished.request().first());
            station().setSelectedSide(view.side());
            sendViewState(finished.id());
        }
    }

    public void handleFill(FillRecipePayload payload) {
        if (clientSide || station == null || player.isSpectator() || !stillValid(player)) {
            return;
        }
        long time = level.getGameTime();
        if (time == lastFillTick) {
            return;
        }
        lastFillTick = time;
        int sets = Math.max(0, Math.min(64, payload.sets()));
        List<ItemStack> templates = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            ItemStack template = i < payload.templates().size() ? payload.templates().get(i) : ItemStack.EMPTY;
            templates.add(template.isEmpty() ? ItemStack.EMPTY : template.copyWithCount(1));
        }
        StationOperations.FillResult fillResult = StationOperations.fill(this, templates, sets);
        if (fillResult == StationOperations.FillResult.NOT_ENOUGH) {
            player.sendOverlayMessage(Component.translatable("justenoughcraftingtables.fill.not_enough"));
        } else if (fillResult == StationOperations.FillResult.NO_ROOM) {
            player.sendOverlayMessage(Component.translatable("justenoughcraftingtables.fill.no_room"));
        }
        broadcastChanges();
    }

    public List<Source> sources(SourceOrder order) {
        List<Source> sideSources = new ArrayList<>();
        Direction selected = view != null && view.valid() ? view.side() : null;
        if (selected != null) {
            ServerSide side = serverSides[selected.get3DDataValue()];
            if (usable(side)) {
                sideSources.add(new Source(Origin.of(selected), side.handler()));
            }
        }
        for (Direction direction : discoveryOrder) {
            if (direction == selected) {
                continue;
            }
            ServerSide side = serverSides[direction.get3DDataValue()];
            if (usable(side)) {
                sideSources.add(new Source(Origin.of(direction), side.handler()));
            }
        }
        List<Source> out = new ArrayList<>();
        if (order == SourceOrder.PLAYER_FIRST) {
            out.addAll(playerStorage.sources());
            out.addAll(sideSources);
        } else {
            out.addAll(sideSources);
            out.addAll(playerStorage.sources());
        }
        return out;
    }

    public ItemStorage playerInventoryHandler() {
        return playerStorage.handler();
    }

    @Nullable
    public ItemStorage storeTarget(@Nullable Direction direction) {
        if (direction == null || !allowInsert) {
            return null;
        }
        ServerSide side = serverSides[direction.get3DDataValue()];
        if (!usable(side)) {
            return null;
        }
        return side.handler();
    }

    public void moveToPlayerInventory(ItemStack stack) {
        moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false);
    }

    public void onGridChanged() {
        updateResult(true);
    }

    @Override
    public void slotsChanged(Container container) {
        if (!clientSide && !closed) {
            updateResult(true);
        }
    }

    private ItemStack computeResult(ServerPlayer serverPlayer, boolean remember) {
        CraftingInput input = grid.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> found = StationRecipes.find(this, serverPlayer, input, level, lastRecipe);
        if (found.isEmpty()) {
            if (remember) {
                result.setRecipeUsed(null);
            }
            return ItemStack.EMPTY;
        }
        RecipeHolder<CraftingRecipe> holder = found.get();
        if (remember) {
            lastRecipe = holder;
        }
        boolean allowed = holder.value().isSpecial()
                || !serverPlayer.level().getGameRules().get(GameRules.LIMITED_CRAFTING)
                || serverPlayer.getRecipeBook().contains(holder.id());
        if (!allowed) {
            return ItemStack.EMPTY;
        }
        if (remember) {
            result.setRecipeUsed(holder);
        }
        ItemStack assembled = holder.value().assemble(input);
        return assembled.isItemEnabled(level.enabledFeatures()) ? assembled : ItemStack.EMPTY;
    }

    public void updateResult(boolean sendToClient) {
        if (clientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack out = computeResult(serverPlayer, true);
        result.setItem(0, out);
        if (sendToClient) {
            setRemoteSlot(RESULT_SLOT, out);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), RESULT_SLOT, out));
        }
    }

    public boolean verifyResult() {
        if (clientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        ItemStack expected = computeResult(serverPlayer, true);
        if (ItemStack.matches(expected, result.getItem(0))) {
            return true;
        }
        updateResult(true);
        return false;
    }

    public NonNullList<ItemStack> remainingItems(CraftingInput input) {
        RecipeHolder<CraftingRecipe> holder = lastRecipe;
        if (holder != null && ((ServerLevel) level).getServer().getRecipeManager().byKey(holder.id()).orElse(null) == holder && holder.value().matches(input, level)) {
            return holder.value().getRemainingItems(input);
        }
        return ((ServerLevel) level).getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().getRemainingItems(input)).orElseGet(() -> CraftingRecipe.defaultCraftingReminder(input));
    }

    @Override
    public boolean stillValid(Player player) {
        if (clientSide) {
            return true;
        }
        if (station == null || station.isRemoved()) {
            return false;
        }
        if (player.level() != level || level.getBlockEntity(pos) != station) {
            return false;
        }
        if (!StationBlocks.isStation(level.getBlockState(pos))) {
            return false;
        }
        return player.isWithinBlockInteractionRange(pos, 4.0);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!clientSide && station != null && !closed) {
            closed = true;
            station.removeMenu(this);
            if (view != null && view.valid() && view.side() != null) {
                station.setSelectedSide(view.side());
            }
            for (CacheHolder holder : caches) {
                if (holder != null) {
                    holder.active = false;
                }
            }
            cursorEntries.clear();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (clientSide || station == null || player.isSpectator() || !stillValid(player)) {
            return false;
        }
        switch (id) {
            case BUTTON_CLEAR -> StationOperations.clear(this);
            case BUTTON_STORE -> {
                int leftover = StationOperations.store(this);
                if (leftover > 0) {
                    player.sendOverlayMessage(StationOperations.nowhereMessage(leftover));
                }
            }
            case BUTTON_REPEAT_OFF -> repeatCraft = false;
            case BUTTON_REPEAT_ON -> repeatCraft = true;
            case BUTTON_ORDER_PLAYER -> sourceOrder = SourceOrder.PLAYER_FIRST;
            case BUTTON_ORDER_CONNECTED -> sourceOrder = SourceOrder.CONNECTED_FIRST;
            default -> {
                if (id >= BUTTON_HIDE_FIRST && id < BUTTON_HIDE_FIRST + 6) {
                    Direction direction = Direction.from3DDataValue(id - BUTTON_HIDE_FIRST);
                    station.toggleHidden(direction);
                    if (!station.isHidden(direction) || view == null || view.side() != direction) {
                        return true;
                    }
                    view.invalidate();
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot.index == RESULT_SLOT) {
            return false;
        }
        if (slot instanceof WindowSlot windowSlot) {
            return windowSlot.isActive();
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        if (slot.index == RESULT_SLOT) {
            return false;
        }
        if (slot instanceof WindowSlot windowSlot) {
            if (!windowSlot.isActive()) {
                return false;
            }
            ItemStack current = windowSlot.getItem();
            return current.isEmpty() || current.getCount() < current.getMaxStackSize();
        }
        return true;
    }

    private boolean validClick(int slotId, int button, ContainerInput type) {
        if (slotId != -999 && slotId != -1 && (slotId < 0 || slotId >= slots.size())) {
            return false;
        }
        return switch (type) {
            case PICKUP, QUICK_MOVE, THROW, PICKUP_ALL -> button == 0 || button == 1;
            case SWAP -> button >= 0 && button < 9 || button == 40;
            case CLONE -> button == 2;
            case QUICK_CRAFT -> button >= 0 && button <= 10 && (button & 3) <= 2
                    && ((button & 3) != 1 || slotId >= 0);
        };
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (!validClick(slotId, button, clickType)) {
            dragMode = -1;
            resetQuickCraft();
            return;
        }
        if (clickType == ContainerInput.QUICK_CRAFT) {
            int phase = button & 3;
            int mode = button >> 2 & 3;
            if (phase == 0) {
                dragMode = mode;
            } else if (mode != dragMode) {
                dragMode = -1;
                resetQuickCraft();
                return;
            } else if (phase == 2) {
                dragMode = -1;
            }
        } else {
            dragMode = -1;
        }
        if (clientSide) {
            if (!windowClick(slotId, button, clickType, player)) {
                super.clicked(slotId, button, clickType, player);
            }
            return;
        }
        if (player.isSpectator() || !stillValid(player)) {
            return;
        }
        reconcileCursor();
        if (slotId == RESULT_SLOT && clickType != ContainerInput.QUICK_CRAFT) {
            if (clickType == ContainerInput.QUICK_MOVE && repeatCraft && (button == 0 || button == 1)) {
                repeatCraftClick(player);
            } else {
                super.clicked(slotId, button, clickType, player);
            }
            reconcileCursor();
            return;
        }
        ClickFlow flow = ClickFlow.capture(this);
        beginGridBatch();
        inClick = true;
        try {
            if (!windowClick(slotId, button, clickType, player)) {
                super.clicked(slotId, button, clickType, player);
            }
        } finally {
            inClick = false;
            applyRefunds();
            flow.apply(this);
            endGridBatch();
        }
    }

    private void applyRefunds() {
        if (refunds.isEmpty()) {
            return;
        }
        List<ItemStack> pending = new ArrayList<>(refunds);
        refunds.clear();
        for (ItemStack stack : pending) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                setCarried(stack);
            } else if (ItemStack.isSameItemSameComponents(carried, stack)) {
                carried.grow(stack.getCount());
            } else {
                player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
            }
        }
    }

    private boolean windowClick(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId < WINDOW_START || slotId >= WINDOW_END) {
            return false;
        }
        WindowSlot slot = windowSlots[slotId - WINDOW_START];
        if (clickType == ContainerInput.PICKUP && (button == 0 || button == 1)) {
            windowPickup(slot, button, player);
            return true;
        }
        if (clickType == ContainerInput.SWAP && (button >= 0 && button < 9 || button == 40)) {
            windowSwap(slot, button, player);
            return true;
        }
        return false;
    }

    void windowPickup(WindowSlot slot, int button, Player player) {
        ItemStorage handler = windowHandler();
        int real = slot.realIndex();
        if (handler == null || real < 0) {
            return;
        }
        ItemStack carried = getCarried();
        ItemStack current = handler.getStackInSlot(real);
        if (carried.isEmpty()) {
            if (current.isEmpty() || !slot.mayPickup(player)) {
                return;
            }
            int amount = button == 0 ? current.getCount() : (current.getCount() + 1) / 2;
            amount = Math.min(amount, current.getMaxStackSize());
            ItemStack extracted = handler.extractItem(real, amount, false);
            if (!extracted.isEmpty()) {
                setCarried(extracted);
                slot.onTake(player, extracted);
            }
            return;
        }
        if (slot.mayPlace(carried)) {
            int amount = button == 0 ? carried.getCount() : 1;
            setCarried(slot.safeInsert(carried, amount));
            return;
        }
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, carried) && slot.mayPickup(player)) {
            int space = carried.getMaxStackSize() - carried.getCount();
            if (space > 0) {
                ItemStack extracted = handler.extractItem(real, Math.min(space, current.getMaxStackSize()), false);
                if (!extracted.isEmpty()) {
                    carried.grow(extracted.getCount());
                    slot.onTake(player, extracted);
                }
            }
        }
    }

    private void windowSwap(WindowSlot slot, int button, Player player) {
        ItemStorage handler = windowHandler();
        int real = slot.realIndex();
        if (handler == null || real < 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack held = inventory.getItem(button);
        ItemStack current = handler.getStackInSlot(real);
        if (held.isEmpty()) {
            if (current.isEmpty() || !slot.mayPickup(player)) {
                return;
            }
            ItemStack extracted = handler.extractItem(real, Math.min(current.getCount(), current.getMaxStackSize()), false);
            if (!extracted.isEmpty()) {
                inventory.setItem(button, extracted);
                slot.onTake(player, extracted);
            }
            return;
        }
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, held)) {
            return;
        }
        if (!current.isEmpty() && current.getCount() > current.getMaxStackSize()) {
            return;
        }
        if (!slot.mayPlace(held)) {
            return;
        }
        ItemStack remainder = slot.safeInsert(held.copy(), held.getCount());
        inventory.setItem(button, remainder.isEmpty() ? ItemStack.EMPTY : remainder);
    }

    private void repeatCraftClick(Player player) {
        Slot resultSlot = slots.get(RESULT_SLOT);
        ItemStack first = resultSlot.getItem().copy();
        if (first.isEmpty() || !resultSlot.mayPickup(player)) {
            return;
        }
        ItemStack[] pattern = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack stack = grid.getItem(i);
            pattern[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        }
        int limit = JustEnoughCraftingTablesConfig.SERVER.maxRepeatCrafts.get();
        StationOperations.PullHints hints = new StationOperations.PullHints();
        int crafts = 0;
        while (crafts < limit) {
            ItemStack current = resultSlot.getItem();
            if (current.isEmpty() || !ItemStack.isSameItem(current, first)) {
                break;
            }
            ItemStack moved = quickMoveStack(player, RESULT_SLOT);
            if (moved.isEmpty()) {
                break;
            }
            crafts++;
            if (crafts >= limit || !refill(pattern, hints)) {
                break;
            }
        }
    }

    private boolean refill(ItemStack[] pattern, StationOperations.PullHints hints) {
        StationBlockEntity be = station();
        boolean ok = true;
        beginGridBatch();
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack wanted = pattern[i];
                if (wanted.isEmpty()) {
                    continue;
                }
                ItemStack current = be.getGridItem(i);
                if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, wanted)) {
                    continue;
                }
                if (!current.isEmpty() && !StationOperations.storeSlot(this, i, true)) {
                    ok = false;
                    break;
                }
                StationOperations.Pulled pulled = StationOperations.pullOne(this, wanted, hints);
                if (pulled == null) {
                    ok = false;
                    break;
                }
                be.grid().set(i, pulled.stack());
                be.origins().set(i, pulled.stack(), List.of(new OriginEntry(pulled.origin(), pulled.stack().getCount())));
                be.gridChanged();
            }
        } finally {
            endGridBatch();
        }
        return ok;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !clientSide && (player.isSpectator() || !stillValid(player)
                || index == RESULT_SLOT && !verifyResult())) {
            return ItemStack.EMPTY;
        }
        if (index >= WINDOW_START && index < WINDOW_END) {
            return windowQuickMove(windowSlots[index - WINDOW_START]);
        }
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        moved = stack.copy();
        if (index == RESULT_SLOT) {
            if (!clientSide) {
                stack.getItem().onCraftedBy(stack, player);
            }
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, moved);
        } else if (index >= GRID_START && index < GRID_END) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= INVENTORY_START && index < HOTBAR_START) {
            if (!moveItemStackTo(stack, GRID_START, GRID_END, false) && !moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < INVENTORY_END) {
            if (!moveItemStackTo(stack, GRID_START, GRID_END, false) && !moveItemStackTo(stack, INVENTORY_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        if (index == RESULT_SLOT) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
        return moved;
    }

    private ItemStack windowQuickMove(WindowSlot slot) {
        ItemStorage handler = windowHandler();
        int real = slot.realIndex();
        if (handler == null || real < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack current = handler.getStackInSlot(real);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int wanted = Math.min(current.getCount(), current.getMaxStackSize());
        ItemStack simulated = handler.extractItem(real, wanted, true);
        if (simulated.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int fits = playerCapacity(simulated);
        if (fits <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = handler.extractItem(real, Math.min(fits, simulated.getCount()), false);
        if (extracted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = extracted.copy();
        moveItemStackTo(extracted, INVENTORY_START, INVENTORY_END, false);
        if (!extracted.isEmpty()) {
            ItemStack remainder = handler.insertItem(real, extracted, false);
            if (!remainder.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainder, Prediction.SERVER_ONLY);
            }
            moved.shrink(extracted.getCount());
        }
        onWindowModified();
        return moved;
    }

    private int playerCapacity(ItemStack stack) {
        int capacity = 0;
        int max = stack.getMaxStackSize();
        for (int i = INVENTORY_START; i < INVENTORY_END && capacity < stack.getCount(); i++) {
            Slot slot = slots.get(i);
            ItemStack current = slot.getItem();
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                capacity += Math.max(0, Math.min(max, slot.getMaxStackSize(stack)) - current.getCount());
            }
        }
        for (int i = INVENTORY_START; i < INVENTORY_END && capacity < stack.getCount(); i++) {
            Slot slot = slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                capacity += Math.min(max, slot.getMaxStackSize(stack));
            }
        }
        return Math.min(capacity, stack.getCount());
    }

    private void reconcileCursor() {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            cursorEntries.clear();
            cursorTracked = ItemStack.EMPTY;
            return;
        }
        if (cursorTracked.isEmpty() || !ItemStack.isSameItemSameComponents(cursorTracked, carried)) {
            cursorEntries.clear();
            cursorEntries.add(new OriginEntry(Origin.PLAYER, carried.getCount()));
            cursorTracked = carried.copy();
            return;
        }
        int diff = carried.getCount() - cursorTracked.getCount();
        if (diff > 0) {
            GridOrigins.appendTo(cursorEntries, Origin.PLAYER, diff);
        } else if (diff < 0) {
            GridOrigins.removeOldest(cursorEntries, -diff);
        }
        cursorTracked = carried.copy();
    }

    public void requestView(@Nullable Direction side, int page, int first, @Nullable int[] filter, boolean rebuild, PayloadSender sender) {
        if (!clientSide) {
            return;
        }
        activeFilter = filter;
        int predictedLength = 0;
        if (side != null) {
            SideEntry entry = sideEntry(side);
            if (entry != null) {
                predictedLength = filter == null ? entry.slots() : clientWindow.displayLength();
            }
        }
        if (side != null && filter == null) {
            int pages = ServerView.pages(predictedLength, pageSize);
            page = Math.max(0, Math.min(pages - 1, page));
            first = ServerView.clampFirst(first, ServerView.pageLength(predictedLength, pageSize, page));
        }
        ViewPayload.Request request = new ViewPayload.Request(
                Optional.ofNullable(side), Math.max(0, page), Math.max(0, first), Optional.ofNullable(filter), rebuild);
        int id = clientWindow.beginRequest(request, predictedLength);
        sender.send(new ViewPayload(containerId, id, sidesVersion, request));
    }

    @FunctionalInterface
    public interface PayloadSender {
        void send(CustomPacketPayload payload);
    }

    public void applySides(SidesPayload payload, PayloadSender sender) {
        if (!clientSide) {
            return;
        }
        sidesVersion = payload.version();
        sides = List.copyOf(payload.sides());
        clientRevision++;
        for (Direction direction : Direction.values()) {
            SideEntry entry = sideEntry(direction);
            if (entry == null || entry.hidden()) {
                clientSummary.clearSide(direction);
            }
        }
        Direction current = clientWindow.displaySide();
        SideEntry currentEntry = current == null ? null : sideEntry(current);
        boolean currentGone = current != null && (currentEntry == null || currentEntry.hidden());
        if (payload.viewReset() || currentGone || current == null) {
            Direction next = null;
            if (!currentGone && current != null && currentEntry != null && !currentEntry.hidden()) {
                next = current;
            } else {
                Direction suggested = payload.selected().orElse(null);
                SideEntry suggestedEntry = suggested == null ? null : sideEntry(suggested);
                if (suggestedEntry != null && !suggestedEntry.hidden()) {
                    next = suggested;
                } else {
                    for (SideEntry entry : sides) {
                        if (!entry.hidden()) {
                            next = entry.direction();
                            break;
                        }
                    }
                }
            }
            if (next != null || current != null || clientWindow.side() != null) {
                boolean same = next == current;
                requestView(next, same ? clientWindow.displayPage() : 0, same ? clientWindow.displayFirst() : 0, activeFilter, true, sender);
            }
            return;
        }
        ViewPayload.Request pending = clientWindow.pendingRequest();
        if (pending != null) {
            int id = clientWindow.resendId();
            sender.send(new ViewPayload(containerId, id, sidesVersion, pending));
        }
    }

    public void applyViewState(ViewStatePayload payload) {
        if (clientSide) {
            clientWindow.apply(payload);
        }
    }

    public void applySummary(SummaryPayload payload) {
        if (clientSide) {
            clientSummary.apply(payload);
        }
    }

    public long availableCount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        long total = 0;
        for (int i = GRID_START; i < GRID_END; i++) {
            ItemStack item = slots.get(i).getItem();
            if (ItemStack.isSameItemSameComponents(item, stack)) {
                total += item.getCount();
            }
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(item, stack)) {
                total += item.getCount();
            }
        }
        if (clientSummary != null) {
            for (SideEntry entry : sides) {
                if (!entry.hidden()) {
                    total += clientSummary.total(entry.direction(), stack);
                }
            }
        }
        return total;
    }

    public List<ItemStack> availableStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = GRID_START; i < GRID_END; i++) {
            ItemStack item = slots.get(i).getItem();
            if (!item.isEmpty()) {
                out.add(item.copy());
            }
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                out.add(item.copy());
            }
        }
        if (clientSummary != null) {
            for (SideEntry entry : sides) {
                if (entry.hidden()) {
                    continue;
                }
                clientSummary.available(entry.direction()).forEach((id, count) -> {
                    ItemStack template = clientSummary.template(id);
                    if (!template.isEmpty() && count > 0) {
                        out.add(template.copyWithCount((int) Math.min(Integer.MAX_VALUE, count)));
                    }
                });
            }
        }
        return out;
    }

    private static final class ClickFlow {
        private static final int CURSOR = 0;

        private final ItemStack[] before = new ItemStack[INVENTORY_END];
        private final List<List<OriginEntry>> gridEntries = new ArrayList<>();
        private final List<OriginEntry> cursorEntries;

        @Nullable
        private final Direction windowSide;

        private ClickFlow(StationMenu menu) {
            before[CURSOR] = menu.getCarried().copy();
            for (int i = 1; i < INVENTORY_END; i++) {
                before[i] = menu.slots.get(i).getItem().copy();
            }
            for (int i = 0; i < 9; i++) {
                gridEntries.add(new ArrayList<>(menu.gridOrigins().get(i)));
            }
            cursorEntries = new ArrayList<>(menu.cursorEntries);
            windowSide = menu.view != null && menu.view.valid() ? menu.view.side() : null;
        }

        static ClickFlow capture(StationMenu menu) {
            return new ClickFlow(menu);
        }

        private record Chunk(ItemStack item, Origin origin, int count) {
        }

        void apply(StationMenu menu) {
            ItemStack[] after = new ItemStack[INVENTORY_END];
            after[CURSOR] = menu.getCarried();
            for (int i = 1; i < INVENTORY_END; i++) {
                after[i] = menu.slots.get(i).getItem();
            }
            List<Chunk> pool = new ArrayList<>();
            int[] increase = new int[INVENTORY_END];
            boolean[] replaced = new boolean[INVENTORY_END];
            for (int i = 0; i < INVENTORY_END; i++) {
                ItemStack b = before[i];
                ItemStack a = after[i];
                int decrease = 0;
                if (!b.isEmpty() && (a.isEmpty() || !ItemStack.isSameItemSameComponents(a, b))) {
                    decrease = b.getCount();
                    increase[i] = a.getCount();
                    replaced[i] = true;
                } else if (!b.isEmpty()) {
                    int delta = a.getCount() - b.getCount();
                    if (delta < 0) {
                        decrease = -delta;
                    } else {
                        increase[i] = delta;
                    }
                } else {
                    increase[i] = a.getCount();
                    replaced[i] = true;
                }
                if (decrease <= 0) {
                    continue;
                }
                List<OriginEntry> released;
                if (i == CURSOR) {
                    released = GridOrigins.removeOldest(cursorEntries, decrease);
                } else if (i >= GRID_START && i < GRID_END) {
                    released = GridOrigins.removeNewest(gridEntries.get(i - GRID_START), decrease);
                } else if (i >= WINDOW_START && i < WINDOW_END) {
                    released = List.of(new OriginEntry(windowSide == null ? Origin.PLAYER : Origin.of(windowSide), decrease));
                } else {
                    released = List.of(new OriginEntry(Origin.PLAYER, decrease));
                }
                int total = 0;
                for (OriginEntry entry : released) {
                    pool.add(new Chunk(b, entry.origin(), entry.count()));
                    total += entry.count();
                }
                if (total < decrease) {
                    pool.add(new Chunk(b, Origin.PLAYER, decrease - total));
                }
            }
            for (int i = 0; i < GRID_END; i++) {
                if (i != CURSOR && i < GRID_START) {
                    continue;
                }
                ItemStack a = after[i];
                List<OriginEntry> list = i == CURSOR ? cursorEntries : gridEntries.get(i - GRID_START);
                if (replaced[i]) {
                    list.clear();
                }
                int need = increase[i];
                for (int p = 0; p < pool.size() && need > 0; p++) {
                    Chunk chunk = pool.get(p);
                    if (chunk.count() <= 0 || !ItemStack.isSameItemSameComponents(chunk.item(), a)) {
                        continue;
                    }
                    int take = Math.min(need, chunk.count());
                    GridOrigins.appendTo(list, chunk.origin(), take);
                    pool.set(p, new Chunk(chunk.item(), chunk.origin(), chunk.count() - take));
                    need -= take;
                }
                if (need > 0) {
                    GridOrigins.appendTo(list, Origin.PLAYER, need);
                }
                if (i == CURSOR) {
                    menu.cursorEntries.clear();
                    if (!a.isEmpty()) {
                        menu.cursorEntries.addAll(list);
                        int sum = GridOrigins.total(menu.cursorEntries);
                        if (sum < a.getCount()) {
                            GridOrigins.appendTo(menu.cursorEntries, Origin.PLAYER, a.getCount() - sum);
                        } else if (sum > a.getCount()) {
                            GridOrigins.removeNewest(menu.cursorEntries, sum - a.getCount());
                        }
                    }
                    menu.cursorTracked = a.copy();
                } else {
                    menu.gridOrigins().set(i - GRID_START, a, list);
                }
            }
        }
    }

    public static int panelX(boolean right) {
        return right ? PANEL_RIGHT : PANEL_LEFT;
    }

    public static int windowX(int panelX, boolean scrollbar) {
        return panelX + (scrollbar ? 5 : 13);
    }
}
