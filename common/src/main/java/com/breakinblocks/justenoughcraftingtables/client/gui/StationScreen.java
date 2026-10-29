package com.breakinblocks.justenoughcraftingtables.client.gui;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.config.ButtonsLayout;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.config.PanelSide;
import com.breakinblocks.justenoughcraftingtables.menu.ClientSummary;
import com.breakinblocks.justenoughcraftingtables.menu.ClientWindow;
import com.breakinblocks.justenoughcraftingtables.menu.ServerView;
import com.breakinblocks.justenoughcraftingtables.menu.SideEntry;
import com.breakinblocks.justenoughcraftingtables.menu.SourceOrder;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import com.breakinblocks.justenoughcraftingtables.platform.StationScreenBase;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.justenoughcraftingtables.platform.Platform;
import com.breakinblocks.justenoughcraftingtables.platform.ClientNetwork;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class StationScreen extends StationScreenBase implements SideTabButton.Owner {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final Identifier PANEL = JustEnoughCraftingTables.id("panel");
    private static final Identifier PANEL_TALL = JustEnoughCraftingTables.id("panel_tall");
    private static final Identifier SLOT = JustEnoughCraftingTables.id("slot");
    private static final Identifier SCROLLBAR_TRACK = JustEnoughCraftingTables.id("scrollbar_track");
    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final IconButton.Sprites STORE = sprites("store", true);
    private static final IconButton.Sprites CLEAR = sprites("clear", true);
    private static final IconButton.Sprites ORDER_PLAYER = sprites("order_player", false);
    private static final IconButton.Sprites ORDER_CONNECTED = sprites("order_connected", false);
    private static final IconButton.Sprites REPEAT_OFF = sprites("repeat_off", false);
    private static final IconButton.Sprites REPEAT_ON = sprites("repeat_on", false);
    private static final IconButton.Sprites PAGE_PREV = sprites("page_prev", true);
    private static final IconButton.Sprites PAGE_NEXT = sprites("page_next", true);

    public static final int PANEL_WIDTH = 131;
    public static final int PANEL_HEIGHT = 183;
    public static final int PANEL_TALL_HEIGHT = 199;
    public static final int TAB_Y = -22;
    public static final int FOOTER_Y = 182;
    private static final int EXCLUSION_MARGIN = 2;
    private static final long DEBOUNCE_MS = 150;

    private final SideTabButton[] tabs = new SideTabButton[6];
    private final ViewMemory.Station memory;
    private final boolean clearAllowed;

    @Nullable
    private EditBox search;

    @Nullable
    private IconButton storeButton;

    @Nullable
    private IconButton clearButton;

    @Nullable
    private IconButton orderButton;

    @Nullable
    private IconButton repeatButton;

    @Nullable
    private IconButton prevButton;

    @Nullable
    private IconButton nextButton;

    @Nullable
    private PageLabel pageLabel;

    private boolean opened;
    private boolean draggingScroll;
    private String query = "";
    private boolean queryDirty;
    private long queryChangedAt;

    @Nullable
    private int[] filterIds;

    @Nullable
    private IntSet badgeIds;

    private boolean filterStale;
    private boolean searchPartial;
    private int badgeSummaryRevision = -1;
    private int staleSummaryRevision = -1;

    public StationScreen(StationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
        memory = ViewMemory.get(playerInventory.player.level().dimension(), menu.pos());
        clearAllowed = !Platform.isLoaded("craftingtweaks");
    }

    private static IconButton.Sprites sprites(String name, boolean disabled) {
        return new IconButton.Sprites(
                JustEnoughCraftingTables.id(name),
                JustEnoughCraftingTables.id(name + "_highlighted"),
                disabled ? JustEnoughCraftingTables.id(name + "_disabled") : null);
    }

    private ClientWindow window() {
        return menu.clientWindow();
    }

    private ClientSummary summary() {
        return menu.clientSummary();
    }

    public int panelX() {
        return JustEnoughCraftingTablesConfig.CLIENT.panelSide.get() == PanelSide.RIGHT ? StationMenu.PANEL_RIGHT : StationMenu.PANEL_LEFT;
    }

    public boolean panelShown() {
        return !menu.sides().isEmpty();
    }

    public boolean footerShown() {
        if (!panelShown()) {
            return false;
        }
        if (!query.isBlank()) {
            return true;
        }
        for (SideEntry entry : menu.sides()) {
            if (!entry.hidden() && entry.slots() > ClientWindow.SIZE) {
                return true;
            }
        }
        return false;
    }

    private int panelHeight() {
        return footerShown() ? PANEL_TALL_HEIGHT : PANEL_HEIGHT;
    }

    private int displayPageLength() {
        return ServerView.pageLength(window().displayLength(), menu.pageSize(), window().displayPage());
    }

    private int displayPages() {
        return ServerView.pages(window().displayLength(), menu.pageSize());
    }

    private boolean scrollbarShown() {
        return panelShown() && window().displaySide() != null && displayPageLength() > ClientWindow.SIZE;
    }

    private boolean pagesShown() {
        return footerShown() && window().displaySide() != null && displayPages() > 1;
    }

    private boolean anySideVisible() {
        for (SideEntry entry : menu.sides()) {
            if (!entry.hidden()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int px = panelX();
        if (panelShown()) {
            if (px < 0 && leftPos + px < 0) {
                leftPos = Math.min(Math.max(0, width - imageWidth), -px);
            } else if (px > 0 && leftPos + px + PANEL_WIDTH > width) {
                leftPos = Math.max(0, width - (px + PANEL_WIDTH));
            }
        }
        for (Direction direction : Direction.values()) {
            SideTabButton tab = new SideTabButton(this, direction, 0, 0);
            tabs[direction.get3DDataValue()] = tab;
            addRenderableWidget(tab);
        }
        storeButton = addRenderableWidget(new IconButton(0, 0, () -> STORE,
                () -> Component.translatable("justenoughcraftingtables.button.store"), () -> pressButton(StationMenu.BUTTON_STORE)));
        clearButton = addRenderableWidget(new IconButton(0, 0, () -> CLEAR,
                () -> Component.translatable("justenoughcraftingtables.button.clear"), () -> pressButton(StationMenu.BUTTON_CLEAR)));
        orderButton = addRenderableWidget(new IconButton(0, 0,
                () -> sourceOrder() == SourceOrder.PLAYER_FIRST ? ORDER_PLAYER : ORDER_CONNECTED,
                () -> Component.translatable(sourceOrder() == SourceOrder.PLAYER_FIRST
                        ? "justenoughcraftingtables.button.order.player"
                        : "justenoughcraftingtables.button.order.connected"),
                this::toggleSourceOrder));
        repeatButton = addRenderableWidget(new IconButton(0, 0,
                () -> repeatCraft() ? REPEAT_ON : REPEAT_OFF,
                () -> Component.translatable(repeatCraft()
                        ? "justenoughcraftingtables.button.repeat.on"
                        : "justenoughcraftingtables.button.repeat.off"),
                this::toggleRepeat));
        search = new EditBox(font, 0, 0, 64, 12, Component.translatable("justenoughcraftingtables.narration.search"));
        search.setMaxLength(64);
        search.setHint(Component.translatable("justenoughcraftingtables.search.hint"));
        search.setValue(query);
        search.setResponder(this::onSearchChanged);
        searchPartial = false;
        addRenderableWidget(search);
        prevButton = addRenderableWidget(new IconButton(0, 0, () -> PAGE_PREV,
                () -> Component.translatable("justenoughcraftingtables.narration.previous_page"), () -> changePage(-1)));
        nextButton = addRenderableWidget(new IconButton(0, 0, () -> PAGE_NEXT,
                () -> Component.translatable("justenoughcraftingtables.narration.next_page"), () -> changePage(1)));
        pageLabel = addRenderableWidget(new PageLabel(0, 0));
        if (!opened) {
            opened = true;
            onFirstOpen();
        }
        relayout();
    }

    private void onFirstOpen() {
        pressButton(repeatCraft() ? StationMenu.BUTTON_REPEAT_ON : StationMenu.BUTTON_REPEAT_OFF);
        pressButton(sourceOrder() == SourceOrder.PLAYER_FIRST ? StationMenu.BUTTON_ORDER_PLAYER : StationMenu.BUTTON_ORDER_CONNECTED);
        String remembered = memory.query();
        if (!remembered.isBlank()) {
            query = remembered;
            if (search != null) {
                search.setValue(remembered);
            }
            queryDirty = false;
            computeFilter();
        }
        Direction side = window().displaySide();
        if (side != null && (memory.page(side) != 0 || memory.first(side) != 0 || filterIds != null)) {
            request(side, memory.page(side), memory.first(side), true);
        }
    }

    private SourceOrder sourceOrder() {
        return JustEnoughCraftingTablesConfig.CLIENT.fillSourceOrder.get();
    }

    private boolean repeatCraft() {
        return JustEnoughCraftingTablesConfig.CLIENT.repeatCraft.get();
    }

    private void toggleSourceOrder() {
        SourceOrder next = sourceOrder() == SourceOrder.PLAYER_FIRST ? SourceOrder.CONNECTED_FIRST : SourceOrder.PLAYER_FIRST;
        JustEnoughCraftingTablesConfig.CLIENT.fillSourceOrder.set(next);
        JustEnoughCraftingTablesConfig.CLIENT.fillSourceOrder.save();
        pressButton(next == SourceOrder.PLAYER_FIRST ? StationMenu.BUTTON_ORDER_PLAYER : StationMenu.BUTTON_ORDER_CONNECTED);
    }

    private void toggleRepeat() {
        boolean next = !repeatCraft();
        JustEnoughCraftingTablesConfig.CLIENT.repeatCraft.set(next);
        JustEnoughCraftingTablesConfig.CLIENT.repeatCraft.save();
        pressButton(next ? StationMenu.BUTTON_REPEAT_ON : StationMenu.BUTTON_REPEAT_OFF);
    }

    private void pressButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private boolean gridEmpty() {
        for (int i = StationMenu.GRID_START; i < StationMenu.GRID_END; i++) {
            if (menu.getSlot(i).hasItem()) {
                return false;
            }
        }
        return true;
    }

    private void relayout() {
        int px = panelX();
        boolean panel = panelShown();
        menu.layoutWindow(StationMenu.windowX(px, scrollbarShown()));
        Direction selected = window().displaySide();
        List<SideEntry> entries = menu.sides();
        refreshBadgeIds();
        int number = 0;
        for (Direction direction : Direction.values()) {
            SideTabButton tab = tabs[direction.get3DDataValue()];
            if (tab == null) {
                continue;
            }
            SideEntry entry = panel ? menu.sideEntry(direction) : null;
            String badge = null;
            boolean dimmed = false;
            if (entry != null) {
                number++;
                if (badgeIds != null && !entry.hidden()) {
                    long total = summary().sideTotal(direction, badgeIds);
                    if (total > 0) {
                        badge = CompactCount.format(total);
                    } else {
                        dimmed = true;
                    }
                }
            }
            tab.setX(leftPos + px + 2 + 21 * direction.get3DDataValue());
            tab.setY(topPos + TAB_Y);
            tab.update(entry, direction == selected, dimmed, badge, number, entries.size());
        }
        if (storeButton != null) {
            storeButton.active = !gridEmpty();
            storeButton.refreshLabel();
        }
        if (clearButton != null) {
            clearButton.visible = clearAllowed;
            clearButton.active = !gridEmpty();
            clearButton.refreshLabel();
        }
        if (orderButton != null) {
            orderButton.visible = anySideVisible();
            orderButton.refreshLabel();
        }
        if (repeatButton != null) {
            repeatButton.refreshLabel();
        }
        layoutActionButtons();
        boolean footer = footerShown();
        boolean pages = pagesShown();
        if (search != null) {
            search.visible = footer;
            search.setX(leftPos + px + 4);
            search.setY(topPos + FOOTER_Y);
            search.setWidth(pages ? 64 : 122);
            boolean partial = false;
            for (SideEntry entry : entries) {
                if (!entry.hidden() && summary().partial(entry.direction())) {
                    partial = true;
                }
            }
            if (partial != searchPartial) {
                searchPartial = partial;
                search.setTooltip(partial ? Tooltip.create(Component.translatable("justenoughcraftingtables.search.partial")) : null);
            }
            if (!footer && search.isFocused()) {
                search.setFocused(false);
            }
        }
        int page = window().displayPage();
        int pageCount = displayPages();
        if (prevButton != null) {
            prevButton.visible = pages;
            prevButton.active = page > 0;
            prevButton.setX(leftPos + px + 70);
            prevButton.setY(topPos + FOOTER_Y);
            prevButton.refreshLabel();
        }
        if (nextButton != null) {
            nextButton.visible = pages;
            nextButton.active = page < pageCount - 1;
            nextButton.setX(leftPos + px + 114);
            nextButton.setY(topPos + FOOTER_Y);
            nextButton.refreshLabel();
        }
        if (pageLabel != null) {
            pageLabel.visible = pages;
            pageLabel.setX(leftPos + px + 83);
            pageLabel.setY(topPos + FOOTER_Y);
            pageLabel.update(page, pageCount);
        }
    }

    private void layoutActionButtons() {
        JustEnoughCraftingTablesConfig.ClientConfig config = JustEnoughCraftingTablesConfig.CLIENT;
        boolean horizontal = config.buttonsLayout.get() == ButtonsLayout.HORIZONTAL;
        int spacing = config.buttonsSpacing.get();
        boolean collapse = config.collapseHiddenButtons.get();
        int baseX = leftPos + config.buttonsX.get();
        int baseY = topPos + config.buttonsY.get();
        IconButton[] buttons = {storeButton, clearButton, orderButton, repeatButton};
        JustEnoughCraftingTablesConfig.ButtonOffset[] offsets = {
                config.storeButtonOffset, config.clearButtonOffset, config.sourceOrderButtonOffset, config.repeatButtonOffset};
        int index = 0;
        for (int i = 0; i < buttons.length; i++) {
            IconButton button = buttons[i];
            if (button == null) {
                continue;
            }
            if (collapse && !button.visible) {
                continue;
            }
            int step = index * spacing;
            button.setX(baseX + (horizontal ? step : 0) + offsets[i].x.get());
            button.setY(baseY + (horizontal ? 0 : step) + offsets[i].y.get());
            index++;
        }
    }

    private List<IconButton> visibleActionButtons() {
        List<IconButton> result = new ArrayList<>(4);
        for (IconButton button : new IconButton[] {storeButton, clearButton, orderButton, repeatButton}) {
            if (button != null && button.visible) {
                result.add(button);
            }
        }
        return result;
    }

    private boolean overActionButton(double mouseX, double mouseY) {
        for (IconButton button : visibleActionButtons()) {
            if (mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
                    && mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
                return true;
            }
        }
        return false;
    }

    private void refreshBadgeIds() {
        if (query.isBlank()) {
            badgeIds = null;
            return;
        }
        if (badgeIds == null || badgeSummaryRevision != summary().revision()) {
            badgeSummaryRevision = summary().revision();
            badgeIds = summary().matching(SearchQuery.parse(query));
        }
    }

    private void computeFilter() {
        if (query.isBlank()) {
            filterIds = null;
            filterStale = false;
            return;
        }
        IntSet ids = summary().matching(SearchQuery.parse(query));
        filterIds = ids.toIntArray();
        filterStale = true;
        staleSummaryRevision = summary().revision();
    }

    private void onSearchChanged(String value) {
        if (!value.equals(query)) {
            query = value;
            queryDirty = true;
            queryChangedAt = Util.getMillis();
        }
    }

    private void applyQuery() {
        queryDirty = false;
        memory.setQuery(query);
        computeFilter();
        Direction side = window().displaySide();
        if (side != null) {
            request(side, 0, 0, true);
        }
    }

    private void request(@Nullable Direction side, int page, int first, boolean rebuild) {
        menu.requestView(side, page, first, filterIds, rebuild, ClientNetwork::sendToServer);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (queryDirty && Util.getMillis() - queryChangedAt >= DEBOUNCE_MS) {
            applyQuery();
        }
        if (filterIds != null && filterStale && !queryDirty && !window().pending() && summary().revision() != staleSummaryRevision) {
            computeFilter();
            Direction side = window().displaySide();
            if (side != null) {
                request(side, window().displayPage(), window().displayFirst(), true);
            }
        }
        ClientWindow window = window();
        if (!window.pending() && window.side() != null) {
            memory.remember(window.side(), window.page(), window.first());
        }
    }

    @Override
    public void onTabLeftClick(Direction direction) {
        SideEntry entry = menu.sideEntry(direction);
        if (entry == null || entry.hidden() || direction == window().displaySide()) {
            return;
        }
        request(direction, memory.page(direction), memory.first(direction), true);
    }

    @Override
    public void onTabRightClick(Direction direction) {
        pressButton(StationMenu.BUTTON_HIDE_FIRST + direction.get3DDataValue());
    }

    private void scrollRows(int delta) {
        Direction side = window().displaySide();
        if (side == null) {
            return;
        }
        int pageLength = displayPageLength();
        int current = window().displayFirst();
        int next = ServerView.clampFirst(current + delta * 6, pageLength);
        if (next != current) {
            request(side, window().displayPage(), next, false);
        }
    }

    private void setPage(int page) {
        Direction side = window().displaySide();
        if (side == null) {
            return;
        }
        int clamped = Mth.clamp(page, 0, displayPages() - 1);
        if (clamped != window().displayPage()) {
            request(side, clamped, 0, false);
        }
    }

    private void changePage(int delta) {
        setPage(window().displayPage() + delta);
    }

    private void scrollTo(double mouseY) {
        Direction side = window().displaySide();
        if (side == null) {
            return;
        }
        int maxFirst = ServerView.maxFirst(displayPageLength());
        int maxRow = maxFirst / 6;
        float fraction = Mth.clamp((float) ((mouseY - topPos - 24.5) / 145.0), 0.0F, 1.0F);
        int row = Math.round(fraction * maxRow);
        int first = row * 6;
        if (first != window().displayFirst()) {
            request(side, window().displayPage(), first, false);
        }
    }

    private boolean inTrack(double mouseX, double mouseY) {
        int x = leftPos + panelX() + 113;
        int y = topPos + 16;
        return mouseX >= x && mouseX < x + 14 && mouseY >= y && mouseY < y + 162;
    }

    private boolean overPanel(double mouseX, double mouseY) {
        int x = leftPos + panelX();
        return mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= topPos && mouseY < topPos + panelHeight();
    }

    private boolean overWheelArea(double mouseX, double mouseY) {
        if (JustEnoughCraftingTablesConfig.CLIENT.wheelScrollWholePanel.get()) {
            return overPanel(mouseX, mouseY);
        }
        int x = leftPos + panelX() + 111;
        return mouseX >= x && mouseX < x + 19;
    }

    private boolean overPanelOrTabs(double mouseX, double mouseY) {
        if (!panelShown()) {
            return false;
        }
        if (overPanel(mouseX, mouseY)) {
            return true;
        }
        int px = leftPos + panelX();
        for (SideEntry entry : menu.sides()) {
            int x = px + 2 + 21 * entry.direction().get3DDataValue();
            int y = topPos + TAB_Y;
            if (mouseX >= x && mouseX < x + SideTabButton.WIDTH && mouseY >= y && mouseY < y + SideTabButton.HEIGHT) {
                return true;
            }
        }
        return false;
    }

    public List<Rect2i> getExclusionAreas() {
        List<Rect2i> areas = new ArrayList<>();
        if (panelShown()) {
            int x = leftPos + panelX() - EXCLUSION_MARGIN;
            int y = topPos + TAB_Y - EXCLUSION_MARGIN;
            int height = panelHeight() - TAB_Y + EXCLUSION_MARGIN * 2;
            areas.add(new Rect2i(x, y, PANEL_WIDTH + EXCLUSION_MARGIN * 2, height));
        }
        for (IconButton button : visibleActionButtons()) {
            boolean inside = button.getX() >= leftPos && button.getY() >= topPos
                    && button.getX() + button.getWidth() <= leftPos + imageWidth
                    && button.getY() + button.getHeight() <= topPos + imageHeight;
            if (!inside) {
                areas.add(new Rect2i(button.getX() - EXCLUSION_MARGIN, button.getY() - EXCLUSION_MARGIN,
                        button.getWidth() + EXCLUSION_MARGIN * 2, button.getHeight() + EXCLUSION_MARGIN * 2));
            }
        }
        return areas;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && scrollbarShown() && inTrack(mouseX, mouseY)) {
            draggingScroll = true;
            scrollTo(mouseY);
            return true;
        }
        if (search != null && search.visible && !search.isMouseOver(mouseX, mouseY) && search.isFocused()) {
            search.setFocused(false);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScroll) {
            scrollTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingScroll && event.button() == 0) {
            draggingScroll = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (panelShown() && window().displaySide() != null && scrollY != 0) {
            if (minecraft.hasShiftDown() && overPanel(mouseX, mouseY)) {
                changePage(scrollY > 0 ? -1 : 1);
                return true;
            }
            if (scrollbarShown() && overWheelArea(mouseX, mouseY)) {
                scrollRows(scrollY > 0 ? -1 : 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private double mouseX() {
        return minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth();
    }

    private double mouseY() {
        return minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (search != null && search.visible) {
            if (keyCode == GLFW.GLFW_KEY_F && minecraft.hasControlDown() && !minecraft.hasShiftDown() && !minecraft.hasAltDown()) {
                setFocused(search);
                search.setFocused(true);
                return true;
            }
            if (search.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    if (!search.getValue().isEmpty()) {
                        search.setValue("");
                        applyQuery();
                        return true;
                    }
                    search.setFocused(false);
                    return super.keyPressed(event);
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    applyQuery();
                    return true;
                }
                if (search.keyPressed(event)) {
                    return true;
                }
                if (search.canConsumeInput()) {
                    return true;
                }
            }
        }
        if (panelShown() && overPanel(mouseX(), mouseY())) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_PAGE_UP -> {
                    changePage(-1);
                    return true;
                }
                case GLFW.GLFW_KEY_PAGE_DOWN -> {
                    changePage(1);
                    return true;
                }
                case GLFW.GLFW_KEY_HOME -> {
                    setPage(0);
                    return true;
                }
                case GLFW.GLFW_KEY_END -> {
                    setPage(displayPages() - 1);
                    return true;
                }
                default -> {
                }
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (search != null && search.visible && search.isFocused()) {
            return search.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ContainerInput type) {
        int index = slot != null ? slot.index : slotId;
        if (index >= StationMenu.WINDOW_START && index < StationMenu.WINDOW_END && !menu.isWindowSlotActive(index - StationMenu.WINDOW_START)) {
            return;
        }
        if (type == ContainerInput.PICKUP_ALL && window().pending()) {
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        if (overPanelOrTabs(mouseX, mouseY) || overActionButton(mouseX, mouseY)) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        relayout();
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (!panelShown()) {
            return;
        }
        int px = leftPos + panelX();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, footerShown() ? PANEL_TALL : PANEL, px, topPos, PANEL_WIDTH, panelHeight());
        ClientWindow window = window();
        boolean scrollbar = scrollbarShown();
        int x0 = leftPos + StationMenu.windowX(panelX(), scrollbar);
        for (int k = 0; k < ClientWindow.SIZE; k++) {
            if (window.active(k) || window.previewSlot(k, menu.pageSize())) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x0 - 1 + 18 * (k % 6), topPos + 16 + 18 * (k / 6), 18, 18);
            }
        }
        if (scrollbar) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TRACK, px + 113, topPos + 16, 14, 162);
            int maxFirst = ServerView.maxFirst(displayPageLength());
            float fraction = maxFirst == 0 ? 0.0F : (float) window.displayFirst() / maxFirst;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER, px + 114, topPos + 17 + Mth.floor(145 * fraction), 12, 15);
        }
        if (window.pending()) {
            for (int k = 0; k < ClientWindow.SIZE; k++) {
                if (!window.previewSlot(k, menu.pageSize())) {
                    continue;
                }
                ItemStack preview = window.preview(k, menu.pageSize());
                if (preview.isEmpty()) {
                    continue;
                }
                int x = x0 + 18 * (k % 6);
                int y = topPos + 17 + 18 * (k / 6);
                guiGraphics.item(preview, x, y);
                guiGraphics.itemDecorations(font, preview, x, y, countText(preview));
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        if (!panelShown()) {
            return;
        }
        Direction side = window().displaySide();
        SideEntry entry = side == null ? null : menu.sideEntry(side);
        if (entry != null) {
            String name = font.plainSubstrByWidth(entry.name().getString(), 122);
            guiGraphics.text(font, name, panelX() + 8, 6, 0xFF404040, false);
        }
    }

    @Nullable
    private String countText(ItemStack stack) {
        if (stack.getCount() > 999 && JustEnoughCraftingTablesConfig.CLIENT.compactCounts.get()) {
            return CompactCount.format(stack.getCount());
        }
        return null;
    }

}
