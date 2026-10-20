package com.breakinblocks.justenoughcraftingtables.config;

import com.breakinblocks.justenoughcraftingtables.menu.SourceOrder;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class JustEnoughCraftingTablesConfig {
    public static final ForgeConfigSpec clientSpec;
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec serverSpec;
    public static final ServerConfig SERVER;

    static {
        Pair<ClientConfig, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = client.getLeft();
        clientSpec = client.getRight();
        Pair<ServerConfig, ForgeConfigSpec> server = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = server.getLeft();
        serverSpec = server.getRight();
    }

    private JustEnoughCraftingTablesConfig() {
    }

    public static int pageSize() {
        int value = SERVER.pageSize.get();
        value = Math.max(54, Math.min(5400, value));
        return value - value % 54;
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.EnumValue<AccessContext> accessContext;
        public final ForgeConfigSpec.BooleanValue allowInsertIntoSides;
        public final ForgeConfigSpec.IntValue pageSize;
        public final ForgeConfigSpec.IntValue scanBudget;
        public final ForgeConfigSpec.IntValue maxSummaryEntries;
        public final ForgeConfigSpec.IntValue minTicksBetweenViewRebuilds;
        public final ForgeConfigSpec.IntValue maxRepeatCrafts;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("sides");
            accessContext = builder.translation("justenoughcraftingtables.configuration.accessContext")
                    .defineEnum("accessContext", AccessContext.INTERNAL);
            allowInsertIntoSides = builder.translation("justenoughcraftingtables.configuration.allowInsertIntoSides")
                    .define("allowInsertIntoSides", true);
            builder.pop();
            builder.push("view");
            pageSize = builder.translation("justenoughcraftingtables.configuration.pageSize")
                    .defineInRange("pageSize", 540, 54, 5400);
            scanBudget = builder.translation("justenoughcraftingtables.configuration.scanBudget")
                    .defineInRange("scanBudget", 2048, 256, 65536);
            maxSummaryEntries = builder.translation("justenoughcraftingtables.configuration.maxSummaryEntries")
                    .defineInRange("maxSummaryEntries", 8192, 256, 65536);
            minTicksBetweenViewRebuilds = builder.translation("justenoughcraftingtables.configuration.minTicksBetweenViewRebuilds")
                    .defineInRange("minTicksBetweenViewRebuilds", 1, 1, 100);
            builder.pop();
            builder.push("crafting");
            maxRepeatCrafts = builder.translation("justenoughcraftingtables.configuration.maxRepeatCrafts")
                    .defineInRange("maxRepeatCrafts", 2304, 1, 2304);
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.BooleanValue renderGridItems;
        public final ForgeConfigSpec.BooleanValue wheelScrollWholePanel;
        public final ForgeConfigSpec.EnumValue<SourceOrder> fillSourceOrder;
        public final ForgeConfigSpec.BooleanValue repeatCraft;
        public final ForgeConfigSpec.EnumValue<PanelSide> panelSide;
        public final ForgeConfigSpec.BooleanValue compactCounts;
        public final ForgeConfigSpec.IntValue buttonsX;
        public final ForgeConfigSpec.IntValue buttonsY;
        public final ForgeConfigSpec.EnumValue<ButtonsLayout> buttonsLayout;
        public final ForgeConfigSpec.IntValue buttonsSpacing;
        public final ForgeConfigSpec.BooleanValue collapseHiddenButtons;
        public final ButtonOffset storeButtonOffset;
        public final ButtonOffset clearButtonOffset;
        public final ButtonOffset sourceOrderButtonOffset;
        public final ButtonOffset repeatButtonOffset;

        ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("rendering");
            renderGridItems = builder.translation("justenoughcraftingtables.configuration.renderGridItems")
                    .define("renderGridItems", true);
            compactCounts = builder.translation("justenoughcraftingtables.configuration.compactCounts")
                    .define("compactCounts", true);
            builder.pop();
            builder.push("screen");
            panelSide = builder.translation("justenoughcraftingtables.configuration.panelSide")
                    .defineEnum("panelSide", PanelSide.LEFT);
            wheelScrollWholePanel = builder.translation("justenoughcraftingtables.configuration.wheelScrollWholePanel")
                    .define("wheelScrollWholePanel", true);
            builder.pop();
            builder.push("buttons");
            buttonsX = builder.translation("justenoughcraftingtables.configuration.buttonsX")
                    .defineInRange("buttonsX", 152, -1000, 1000);
            buttonsY = builder.translation("justenoughcraftingtables.configuration.buttonsY")
                    .defineInRange("buttonsY", 17, -1000, 1000);
            buttonsLayout = builder.translation("justenoughcraftingtables.configuration.buttonsLayout")
                    .defineEnum("buttonsLayout", ButtonsLayout.VERTICAL);
            buttonsSpacing = builder.translation("justenoughcraftingtables.configuration.buttonsSpacing")
                    .defineInRange("buttonsSpacing", 14, 12, 64);
            collapseHiddenButtons = builder.translation("justenoughcraftingtables.configuration.collapseHiddenButtons")
                    .define("collapseHiddenButtons", false);
            storeButtonOffset = new ButtonOffset(builder, "storeButtonOffset");
            clearButtonOffset = new ButtonOffset(builder, "clearButtonOffset");
            sourceOrderButtonOffset = new ButtonOffset(builder, "sourceOrderButtonOffset");
            repeatButtonOffset = new ButtonOffset(builder, "repeatButtonOffset");
            builder.pop();
            builder.push("crafting");
            fillSourceOrder = builder.translation("justenoughcraftingtables.configuration.fillSourceOrder")
                    .defineEnum("fillSourceOrder", SourceOrder.PLAYER_FIRST);
            repeatCraft = builder.translation("justenoughcraftingtables.configuration.repeatCraft")
                    .define("repeatCraft", false);
            builder.pop();
        }
    }

    public static class ButtonOffset {
        public final ForgeConfigSpec.IntValue x;
        public final ForgeConfigSpec.IntValue y;

        ButtonOffset(ForgeConfigSpec.Builder builder, String name) {
            String key = "justenoughcraftingtables.configuration." + name;
            builder.translation(key).push(name);
            x = builder.translation(key + ".x").defineInRange("x", 0, -1000, 1000);
            y = builder.translation(key + ".y").defineInRange("y", 0, -1000, 1000);
            builder.pop();
        }
    }
}
