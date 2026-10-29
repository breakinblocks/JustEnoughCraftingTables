package com.breakinblocks.justenoughcraftingtables.client.gui;

import com.breakinblocks.justenoughcraftingtables.config.ConfigSpec;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class StationConfigScreen extends Screen {
    private record Field(ConfigSpec.Value<?> value, String key) {
        Component label() {
            List<String> path = List.of(key.split("\\."));
            Component label = Component.translatable("justenoughcraftingtables.configuration." + path.get(path.size()-1));
            return path.size() > 2
                    ? Component.translatable("justenoughcraftingtables.configuration." + path.get(path.size() - 2)).append(": ").append(label)
                    : label;
        }
    }

    private final Screen parent;
    private final List<Field> fields = new ArrayList<>();
    private int page;
    private int rows;

    public StationConfigScreen(Screen parent) {
        super(Component.literal("Just Enough Crafting Tables"));
        this.parent = parent;
        JustEnoughCraftingTablesConfig.clientSpec.entries().forEach((key, value) -> fields.add(new Field(value, key)));
    }

    @Override
    protected void init() {
        rows = Math.max(1, (height - 96) / 24);
        int pages = Math.max(1, (fields.size() + rows - 1) / rows);
        page = Math.min(page, pages - 1);
        for (int i = page * rows; i < fields.size() && i < (page + 1) * rows; i++) {
            Field field = fields.get(i);
            int y = 38 + (i - page * rows) * 24;
            Object value = field.value().get();
            if (value instanceof Integer) {
                EditBox edit = new EditBox(font, width / 2 + 70, y, 80, 20, field.label());
                edit.setValue(value.toString());
                edit.setMaxLength(6);
                edit.setTooltip(Tooltip.create(Component.translatable("justenoughcraftingtables.configuration." + field.key().substring(field.key().lastIndexOf('.') + 1) + ".tooltip")));
                edit.setResponder(text -> {
                    try {
                        int next = Integer.parseInt(text);
                        if (!field.value().accepts(next)) { edit.setTextColor(0xFF5555); return; }
                        save(field.value(), next);
                        edit.setTextColor(0xE0E0E0);
                    } catch (NumberFormatException exception) {
                        edit.setTextColor(0xFF5555);
                    }
                });
                addRenderableWidget(edit);
            } else {
                Button button = Button.builder(text(field), clicked -> {
                    Object current = field.value().get();
                    if (current instanceof Boolean bool) save(field.value(), !bool);
                    else if (current instanceof Enum<?> enumeration) {
                        Object[] values = enumeration.getDeclaringClass().getEnumConstants();
                        save(field.value(), values[(enumeration.ordinal() + 1) % values.length]);
                    }
                    clicked.setMessage(text(field));
                }).bounds(width / 2 - 150, y, 300, 20).build();
                button.setTooltip(Tooltip.create(Component.translatable("justenoughcraftingtables.configuration." + field.key().substring(field.key().lastIndexOf('.') + 1) + ".tooltip")));
                addRenderableWidget(button);
            }
        }
        addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; rebuildWidgets(); })
                .bounds(width / 2 - 150, height - 34, 40, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 80, height - 34, 160, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; rebuildWidgets(); })
                .bounds(width / 2 + 110, height - 34, 40, 20).build()).active = page + 1 < pages;
    }

    private static Component text(Field field) {
        return field.label().copy().append(": " + field.value().get());
    }

    @SuppressWarnings("unchecked")
    private static void save(ConfigSpec.Value<?> value, Object next) {
        ((ConfigSpec.Value<Object>) value).set(next);
        value.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        for (int i = page * rows; i < fields.size() && i < (page + 1) * rows; i++) {
            Field field = fields.get(i);
            if (field.value().get() instanceof Integer) {
                graphics.drawString(font, font.plainSubstrByWidth(field.label().getString(), 210), width / 2 - 150,
                        44 + (i - page * rows) * 24, 0xFFFFFF);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
}
