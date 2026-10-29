package com.breakinblocks.justenoughcraftingtables.client.gui;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.SideEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SideTabButton extends AbstractWidget {
    public static final Identifier SELECTED = JustEnoughCraftingTables.id("tab_selected");
    public static final Identifier UNSELECTED = JustEnoughCraftingTables.id("tab_unselected");
    public static final int WIDTH = 22;
    public static final int HEIGHT = 28;

    public interface Owner {
        void onTabLeftClick(Direction direction);

        void onTabRightClick(Direction direction);
    }

    private final Owner owner;
    private final Direction direction;

    @Nullable
    private SideEntry entry;

    private boolean selected;
    private boolean dimmed;

    @Nullable
    private String badge;

    private int number;
    private int count;

    @Nullable
    private Component lastTooltip;

    public SideTabButton(Owner owner, Direction direction, int x, int y) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.owner = owner;
        this.direction = direction;
    }

    public Direction direction() {
        return direction;
    }

    public void update(@Nullable SideEntry entry, boolean selected, boolean dimmed, @Nullable String badge, int number, int count) {
        this.entry = entry;
        this.selected = selected;
        this.dimmed = dimmed;
        this.badge = badge;
        this.number = number;
        this.count = count;
        this.visible = entry != null;
        if (entry == null) {
            return;
        }
        Component face = faceName(direction);
        setMessage(entry.name());
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("justenoughcraftingtables.tab.tooltip", entry.name(), face));
        if (entry.hidden()) {
            lines.add(Component.translatable("justenoughcraftingtables.tab.hidden"));
        } else {
            lines.add(Component.translatable("justenoughcraftingtables.tab.hide_hint"));
        }
        Component tooltip = CommonComponents.joinLines(lines);
        if (!tooltip.equals(lastTooltip)) {
            lastTooltip = tooltip;
            setTooltip(Tooltip.create(tooltip));
        }
    }

    public static Component faceName(Direction direction) {
        return Component.translatable("justenoughcraftingtables.face." + direction.getName());
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 0 || buttonInfo.button() == 1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!active || !visible || entry == null || !isValidClickButton(event.buttonInfo()) || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == 1) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            owner.onTabRightClick(direction);
            return true;
        }
        if (entry.hidden()) {
            return true;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        owner.onTabLeftClick(direction);
        return true;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (entry == null) {
            return;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected && !entry.hidden() ? SELECTED : UNSELECTED, getX(), getY(), WIDTH, HEIGHT);
        ItemStack icon = entry.icon();
        int iconX = getX() + 3;
        int iconY = getY() + 3;
        if (!icon.isEmpty()) {
            guiGraphics.item(icon, iconX, iconY);
            if (entry.hidden() || dimmed) {
                guiGraphics.pose().pushMatrix();
                guiGraphics.nextStratum();
                guiGraphics.fill(iconX, iconY, iconX + 16, iconY + 16, 0xA08B8B8B);
                guiGraphics.pose().popMatrix();
            }
        }
        if (badge != null && !entry.hidden()) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.pose().pushMatrix();
            guiGraphics.nextStratum();
            guiGraphics.text(font, badge, iconX + 19 - 2 - font.width(badge), iconY + 6 + 3, 0xFFFFFFFF, true);
            guiGraphics.pose().popMatrix();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (entry == null) {
            return;
        }
        String state = entry.hidden() ? "hidden" : selected ? "selected" : "unselected";
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable(
                "justenoughcraftingtables.narration.tab",
                entry.name(),
                Component.translatable("justenoughcraftingtables.narration.face." + direction.getName()),
                number,
                count,
                Component.translatable("justenoughcraftingtables.narration.tab." + state)));
    }
}
