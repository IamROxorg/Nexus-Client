package nexus.zigliix.com.client.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.gui.component.NexusUiState;
import nexus.zigliix.com.client.gui.component.UiAnimation;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.setting.BooleanSetting;
import nexus.zigliix.com.client.module.setting.ModeSetting;
import nexus.zigliix.com.client.module.setting.NumberSetting;
import nexus.zigliix.com.client.module.setting.Setting;
import nexus.zigliix.com.client.notify.NotificationManager;
import nexus.zigliix.com.client.util.KeybindUtil;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class ClickGuiScreen extends Screen {
    private static final int MIN_WIN_W = 620;
    private static final int MAX_WIN_W = 760;
    private static final int MIN_WIN_H = 390;
    private static final int MAX_WIN_H = 430;
    private static final int SIDEBAR_W = 132;
    private static final int PAD = 16;
    private static final int HEADER_H = 78;
    private static final int NAV_ROW_H = 34;
    private static final int CARD_GAP = 12;
    private static final int MODULE_ROW_H = 25;
    private static final int ACTION_H = 30;
    private static final int RADIUS = 12;
    private static final int ICON_NAV = 18;
    private static final int ICON_CARD = 18;
    private static final int ICON_ACTION = 16;
    private static final int BG_OVERLAY = 0xC8030507;
    private static final int SHELL_FILL = 0xF005070A;
    private static final int SIDEBAR_FILL = 0xF0020406;
    private static final int PANEL_FILL = 0xE7080B0F;
    private static final int PANEL_HOVER = 0xEA10151A;
    private static final int ROW_FILL = 0x78101519;
    private static final int ROW_HOVER = 0xA8141A20;
    private static final int BORDER_FAINT = 0x30252D35;
    private static final int BORDER_SOFT = 0x55313A43;
    private static final int BORDER_ACTIVE = 0x88445158;
    private static final Identifier ASSET_MARK = guiTexture("nexus_mark.png");
    private static final Identifier ASSET_HOME = guiTexture("home.png");
    private static final Identifier ASSET_HOME_ACTIVE = guiTexture("home_accent.png");
    private static final Identifier ASSET_HUD = guiTexture("hud.png");
    private static final Identifier ASSET_HUD_ACTIVE = guiTexture("hud_accent.png");
    private static final Identifier ASSET_PERFORMANCE = guiTexture("performance.png");
    private static final Identifier ASSET_PERFORMANCE_ACTIVE = guiTexture("performance_accent.png");
    private static final Identifier ASSET_COSMETICS = guiTexture("cosmetics.png");
    private static final Identifier ASSET_COSMETICS_ACTIVE = guiTexture("cosmetics_accent.png");
    private static final Identifier ASSET_REPLAY = guiTexture("replay.png");
    private static final Identifier ASSET_REPLAY_ACTIVE = guiTexture("replay_accent.png");
    private static final Identifier ASSET_SETTINGS = guiTexture("settings.png");
    private static final Identifier ASSET_SETTINGS_ACTIVE = guiTexture("settings_accent.png");
    private static final Identifier ASSET_FOLDER = guiTexture("folder.png");
    private static final Identifier ASSET_PULSE = guiTexture("pulse.png");
    private static final Identifier ASSET_BOLT = guiTexture("bolt.png");
    private static final Identifier ASSET_QUICK_CONNECT = guiTexture("quick_connect.png");
    private static final Identifier ASSET_REFRESH = guiTexture("refresh.png");

    private enum Page {
        HOME("Home"),
        HUD("HUD"),
        PERFORMANCE("Performance"),
        COSMETICS("Cosmetics"),
        REPLAY("Replay"),
        SETTINGS("Settings");

        private final String displayName;

        Page(String displayName) {
            this.displayName = displayName;
        }
    }

    private final EnumMap<Page, Float> navHover = new EnumMap<>(Page.class);
    private final Map<Module, Float> moduleAnim = new HashMap<>();
    private final Map<Module, Boolean> expandedModules = new HashMap<>();
    private final Map<ModeSetting, Boolean> openDropdowns = new HashMap<>();

    private Page selectedPage = Page.HOME;
    private int winX;
    private int winY;
    private int winW;
    private int winH;
    private double scrollOffset;
    private String searchQuery = "";
    private boolean searchFocused;
    private float openAnim;
    private String tooltip;
    private Module bindingModule;
    private NumberSetting draggingSlider;
    private int draggingSliderTrackX;
    private int draggingSliderTrackW;
    private GameProfile cachedProfile;
    private Supplier<PlayerSkin> cachedSkinLookup;

    public ClickGuiScreen() {
        super(Component.literal("Nexus"));
    }

    @Override
    protected void init() {
        layoutWindow();
        for (Page page : Page.values()) {
            navHover.putIfAbsent(page, 0.0f);
        }
        clampScroll();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.blurBeforeThisStratum();
        g.fill(0, 0, width, height, BG_OVERLAY);
        drawOutsideBrand(g);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        layoutWindow();
        openAnim = UiAnimation.approach(openAnim, 1.0f, 0.18f);
        tooltip = null;

        float eased = UiAnimation.easeOutCubic(openAnim);
        int renderY = winY + Math.round((1.0f - eased) * 14.0f);

        drawShell(g, renderY);
        drawSidebar(g, mouseX, mouseY, renderY);
        drawHeader(g, mouseX, mouseY, renderY);
        drawContent(g, mouseX, mouseY, renderY);
        drawTooltip(g, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void layoutWindow() {
        int availableW = Math.max(360, width - 64);
        int availableH = Math.max(320, height - 74);
        winW = Math.min(availableW, Math.min(MAX_WIN_W, Math.max(MIN_WIN_W, availableW)));
        winH = Math.min(availableH, Math.min(MAX_WIN_H, Math.max(MIN_WIN_H, availableH)));
        winX = (width - winW) / 2;
        winY = Math.max(42, (height - winH) / 2 + 8);
    }

    private void drawOutsideBrand(GuiGraphicsExtractor g) {
        int x = 24;
        int y = 21;
        g.text(font, "Nexus Client", x, y + 9, NexusTheme.withAlpha(NexusTheme.textMuted(), 170), false);
    }

    private void drawShell(GuiGraphicsExtractor g, int y) {
        NexusRenderer.drawDropShadow(g, winX, y, winW, winH, 4);
        drawSurface(g, winX, y, winW, winH, RADIUS, SHELL_FILL, BORDER_SOFT);
        g.fill(winX + SIDEBAR_W, y + 1, winX + SIDEBAR_W + 1, y + winH - 1, BORDER_FAINT);
        g.fill(winX + SIDEBAR_W + 1, y + HEADER_H, winX + winW - 1, y + HEADER_H + 1, BORDER_FAINT);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY, int y) {
        NexusRenderer.fillRoundRect(g, winX + 1, y + 1, SIDEBAR_W - 1, winH - 2, RADIUS, SIDEBAR_FILL);
        drawAsset(g, ASSET_MARK, winX + (SIDEBAR_W - 28) / 2, y + 22, 28, 28, 64);

        int navY = y + 82;
        for (Page page : Page.values()) {
            drawNavRow(g, mouseX, mouseY, winX + 8, navY, page, page == selectedPage);
            navY += NAV_ROW_H + 8;
        }

        drawUserBadge(g, y);
    }

    private void drawNavRow(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, Page page, boolean selected) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, SIDEBAR_W - 16, NAV_ROW_H);
        float hover = UiAnimation.toggle(navHover.getOrDefault(page, 0.0f), hovered, 0.14f);
        navHover.put(page, hover);

        int bg = selected ? 0xB811171D : NexusTheme.withAlpha(PANEL_HOVER, Math.round(hover * 180.0f));
        if (selected || hover > 0.01f) {
            NexusRenderer.fillRoundRect(g, x, y, SIDEBAR_W - 16, NAV_ROW_H, 6, bg);
        }
        if (selected) {
            g.fill(x + SIDEBAR_W - 21, y + 8, x + SIDEBAR_W - 19, y + NAV_ROW_H - 8, NexusTheme.withAlpha(NexusTheme.accent(), 180));
        }

        drawPageIcon(g, page, x + 15, y + (NAV_ROW_H - ICON_NAV) / 2, selected);
        g.text(font, page.displayName, x + 40, y + 13, selected ? NexusTheme.text() : NexusTheme.textMuted(), false);
    }

    private void drawUserBadge(GuiGraphicsExtractor g, int y) {
        int x = winX + 12;
        int badgeY = y + winH - 56;
        int w = SIDEBAR_W - 24;
        drawSurface(g, x, badgeY, w, 40, 6, PANEL_FILL, BORDER_SOFT);
        drawPlayerHead(g, x + 8, badgeY + 9, 20);
        g.text(font, trim(currentPlayerName(), w - 42), x + 34, badgeY + 10, NexusTheme.text(), false);
        g.text(font, "Player", x + 34, badgeY + 23, NexusTheme.textDim(), false);
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY, int y) {
        int x = winX + SIDEBAR_W;
        int w = winW - SIDEBAR_W;
        int configW = 78;
        int configX = x + w - PAD - configW;
        int titleX = x + 26;
        g.text(font, headerTitle(), titleX, y + 25, NexusTheme.text(), false);
        g.text(font, trim(headerSubtitle(), Math.max(120, configX - titleX - 16)), titleX, y + 40, NexusTheme.textDim(), false);
        drawHeaderButton(g, configX, y + 24, configW, "Config", mouseX, mouseY);
    }

    private void drawHeaderButton(GuiGraphicsExtractor g, int x, int y, int w, String text, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, 28);
        drawSurface(g, x, y, w, 24, 6, hovered ? ROW_HOVER : ROW_FILL, hovered ? BORDER_ACTIVE : BORDER_SOFT);
        g.centeredText(font, text, x + w / 2, y + 8, hovered ? NexusTheme.text() : NexusTheme.textMuted());
    }

    private void drawStats(GuiGraphicsExtractor g, int x, int y) {
        drawSurface(g, x, y, 154, 42, 8, PANEL_FILL, BORDER_SOFT);
        g.text(font, "FPS", x + 12, y + 9, NexusTheme.textDim(), false);
        g.text(font, Integer.toString(Minecraft.getInstance().getFps()), x + 12, y + 22, NexusTheme.text(), true);
        g.text(font, "RAM", x + 68, y + 9, NexusTheme.textDim(), false);
        g.text(font, usedMemoryText(), x + 68, y + 22, NexusTheme.text(), false);
        g.fill(x + 68, y + 34, x + 138, y + 36, BORDER_FAINT);
        g.fill(x + 68, y + 34, x + 68 + Math.round(70 * memoryProgress()), y + 36, NexusTheme.accent());
    }

    private void drawContent(GuiGraphicsExtractor g, int mouseX, int mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        if (selectedPage == Page.HOME) {
            drawDashboard(g, mouseX, mouseY, contentX, contentY, contentW, contentH);
            return;
        }
        if (selectedPage == Page.SETTINGS) {
            drawSettingsContent(g, mouseX, mouseY, contentX, contentY, contentW, contentH);
            return;
        }

        drawModuleContent(g, mouseX, mouseY, contentX, contentY, contentW, contentH);
    }

    private void drawModuleContent(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        int leftW = modulePanelWidth(w);
        int rightW = w - leftW - CARD_GAP;
        drawSearch(g, x, y, leftW);
        drawModulePanel(g, mouseX, mouseY, x, y + 36, leftW, h - 36);
        drawQuickPanel(g, mouseX, mouseY, x + leftW + CARD_GAP, y, rightW, h);
    }

    private void drawDashboard(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        int topH = dashboardTopHeight(h);
        int bottomH = h - topH - CARD_GAP;
        int leftW = dashboardTopLeftWidth(w);
        int rightW = w - leftW - CARD_GAP;

        drawFeatureCard(g, mouseX, mouseY, x, y, leftW, topH, Page.HUD, hudModules(), "HUD Settings");
        drawFeatureCard(g, mouseX, mouseY, x + leftW + CARD_GAP, y, rightW, topH, Page.PERFORMANCE, performanceModules(), "Performance Settings");

        int replayW = dashboardReplayWidth(w);
        int cosmeticsW = dashboardCosmeticsWidth(w);
        int quickW = w - replayW - cosmeticsW - CARD_GAP * 2;
        int bottomY = y + topH + CARD_GAP;
        drawFeatureCard(g, mouseX, mouseY, x, bottomY, replayW, bottomH, Page.REPLAY, replayModules(), "Replay Settings");
        drawFeatureCard(g, mouseX, mouseY, x + replayW + CARD_GAP, bottomY, cosmeticsW, bottomH, Page.COSMETICS, cosmeticModules(), "Cosmetics Settings");
        drawQuickActionsCard(g, mouseX, mouseY, x + replayW + cosmeticsW + CARD_GAP * 2, bottomY, quickW, bottomH);
    }

    private void drawSettingsContent(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        int appearanceH = settingsAppearanceHeight(h);
        int bottomY = y + appearanceH + CARD_GAP;
        int bottomH = h - appearanceH - CARD_GAP;

        drawSurface(g, x, y, w, appearanceH, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, ASSET_SETTINGS_ACTIVE, x + 14, y + 12, ICON_CARD);
        g.text(font, "Appearance", x + 40, y + 15, NexusTheme.text(), true);
        g.text(font, trim("Choose the client skin and accent used by every Nexus panel.", w - 30), x + 14, y + 35, NexusTheme.textDim(), false);

        g.text(font, "Theme", x + 14, y + 55, NexusTheme.textMuted(), false);
        drawThemePresetRow(g, mouseX, mouseY, x + 12, y + 68, w - 24);
        g.text(font, "Accent", x + 14, y + 108, NexusTheme.textMuted(), false);
        drawAccentPresetRow(g, mouseX, mouseY, x + 12, y + 121, w - 24);

        drawSurface(g, x, bottomY, w, bottomH, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, ASSET_HUD_ACTIVE, x + 14, bottomY + 12, ICON_CARD);
        g.text(font, "Layout", x + 40, bottomY + 15, NexusTheme.text(), true);
        g.text(font, trim("Move misplaced HUD widgets or reset the saved UI layout.", w - 30), x + 14, bottomY + 35, NexusTheme.textDim(), false);

        int actionY = bottomY + 56;
        int actionH = settingsActionHeight(bottomH);
        int gap = 8;
        int actionW = Math.max(1, (w - 24 - gap * 2) / 3);
        drawSettingsAction(g, mouseX, mouseY, x + 12, actionY, actionW, actionH, ASSET_HUD, "Edit HUD", "Move widgets");
        drawSettingsAction(g, mouseX, mouseY, x + 12 + actionW + gap, actionY, actionW, actionH, ASSET_REFRESH, "Reset HUD", "Default spots");
        drawSettingsAction(g, mouseX, mouseY, x + 12 + (actionW + gap) * 2, actionY, w - 24 - (actionW + gap) * 2, actionH, ASSET_SETTINGS, "Reset UI", "Theme + layout");
    }

    private void drawThemePresetRow(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w) {
        NexusUiState.ThemeVariant[] variants = NexusUiState.ThemeVariant.values();
        int gap = 8;
        int chipW = Math.max(1, (w - gap * (variants.length - 1)) / variants.length);
        for (int i = 0; i < variants.length; i++) {
            NexusUiState.ThemeVariant variant = variants[i];
            boolean selected = variant == NexusUiState.getThemeVariant();
            drawPresetChip(g, mouseX, mouseY, x + i * (chipW + gap), y, chipW, displayName(variant.id()), selected, 0);
        }
    }

    private void drawAccentPresetRow(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w) {
        NexusUiState.AccentPreset[] presets = NexusUiState.AccentPreset.values();
        int gap = 8;
        int chipW = Math.max(1, (w - gap * (presets.length - 1)) / presets.length);
        for (int i = 0; i < presets.length; i++) {
            NexusUiState.AccentPreset preset = presets[i];
            boolean selected = preset == NexusUiState.getAccentPreset();
            drawPresetChip(g, mouseX, mouseY, x + i * (chipW + gap), y, chipW, displayName(preset.id()), selected, preset.color());
        }
    }

    private void drawPresetChip(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, String label, boolean selected, int swatchColor) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, 28);
        int fill = selected ? NexusTheme.withAlpha(NexusTheme.accent(), 34) : hovered ? ROW_HOVER : ROW_FILL;
        int border = selected ? NexusTheme.withAlpha(NexusTheme.accent(), 120) : hovered ? BORDER_ACTIVE : BORDER_SOFT;
        drawSurface(g, x, y, w, 28, 6, fill, border);
        int textX = x + 10;
        int textW = w - 20;
        if (swatchColor != 0) {
            NexusRenderer.fillRoundRect(g, x + 9, y + 9, 10, 10, 5, swatchColor);
            textX += 16;
            textW -= 16;
        }
        g.text(font, trim(label, textW), textX, y + 10, selected ? NexusTheme.text() : NexusTheme.textMuted(), false);
    }

    private void drawSettingsAction(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h, Identifier icon, String title, String description) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, h);
        drawSurface(g, x, y, w, h, 7, hovered ? ROW_HOVER : ROW_FILL, hovered ? BORDER_ACTIVE : BORDER_SOFT);
        drawIcon(g, icon, x + 10, y + Math.max(5, (h - ICON_ACTION) / 2), ICON_ACTION);
        g.text(font, trim(title, w - 34), x + 32, y + Math.max(5, h / 2 - 10), NexusTheme.text(), false);
        if (h >= 34) {
            g.text(font, trim(description, w - 34), x + 32, y + Math.max(17, h / 2 + 2), NexusTheme.textDim(), false);
        }
    }

    private void drawFeatureCard(
        GuiGraphicsExtractor g,
        int mouseX,
        int mouseY,
        int x,
        int y,
        int w,
        int h,
        Page page,
        List<Module> modules,
        String buttonText
    ) {
        drawSurface(g, x, y, w, h, 8, PANEL_FILL, page == selectedPage ? NexusTheme.withAlpha(NexusTheme.accent(), 95) : BORDER_SOFT);
        drawIcon(g, activeTextureForPage(page), x + 14, y + 12, ICON_CARD);
        g.text(font, page.displayName, x + 40, y + 15, NexusTheme.text(), true);
        g.fill(x + 10, y + 42, x + w - 10, y + 43, BORDER_FAINT);

        int rowY = y + 54;
        int rowLimit = y + h - 40;
        for (Module module : modules) {
            if (rowY + 24 > rowLimit) {
                break;
            }
            drawDashboardModuleRow(g, x + 12, rowY, w - 24, module, mouseX, mouseY);
            rowY += 27;
        }

        if (modules.isEmpty()) {
            g.text(font, "No modules available", x + 12, y + 58, NexusTheme.textDim(), false);
        }

        drawCardButton(g, mouseX, mouseY, x + 12, y + h - 34, w - 24, buttonText);
    }

    private void drawDashboardModuleRow(GuiGraphicsExtractor g, int x, int y, int w, Module module, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, 24);
        if (hovered) {
            NexusRenderer.fillRoundRect(g, x - 4, y - 3, w + 8, 28, 5, ROW_FILL);
            tooltip = module.getDescription();
        }
        g.text(font, trim(module.getName(), w - 48), x, y + 1, module.isEnabled() ? NexusTheme.text() : NexusTheme.textMuted(), false);
        g.text(font, trim(module.getDescription(), w - 48), x, y + 12, NexusTheme.textDim(), false);
        drawToggle(g, x + w - 28, y + 5, module.isEnabled());
        g.fill(x, y + 25, x + w, y + 26, BORDER_FAINT);
    }

    private void drawCardButton(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, String text) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, 24);
        drawSurface(g, x, y, w, 24, 6, hovered ? ROW_HOVER : ROW_FILL, hovered ? BORDER_ACTIVE : BORDER_SOFT);
        g.centeredText(font, trim(text, w - 28), x + w / 2, y + 8, hovered ? NexusTheme.text() : NexusTheme.textMuted());
        g.text(font, ">", x + w - 18, y + 8, hovered ? NexusTheme.accent() : NexusTheme.textDim(), false);
    }

    private void drawQuickActionsCard(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        drawSurface(g, x, y, w, h, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, ASSET_BOLT, x + 14, y + 12, ICON_CARD);
        g.text(font, "Quick Actions", x + 40, y + 15, NexusTheme.text(), true);

        int actionY = y + 42;
        int actionH = quickActionHeight(h);
        int step = actionH + 6;
        drawDashboardAction(g, x + 12, actionY, w - 24, actionH, ASSET_QUICK_CONNECT, "Quick Connect", "Connect to a server quickly.", mouseX, mouseY);
        drawDashboardAction(g, x + 12, actionY + step, w - 24, actionH, ASSET_FOLDER, "Open Config Folder", "Manage your configurations.", mouseX, mouseY);
        drawDashboardAction(g, x + 12, actionY + step * 2, w - 24, actionH, ASSET_REFRESH, "Check for Updates", "You're on the latest version.", mouseX, mouseY);
    }

    private void drawDashboardAction(GuiGraphicsExtractor g, int x, int y, int w, int h, Identifier icon, String title, String description, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, h);
        drawSurface(g, x, y, w, h, 7, hovered ? ROW_HOVER : ROW_FILL, hovered ? BORDER_ACTIVE : BORDER_SOFT);
        int iconY = y + Math.max(4, (h - ICON_ACTION) / 2);
        drawIcon(g, icon, x + 12, iconY, ICON_ACTION);
        g.text(font, trim(title, w - 58), x + 36, y + Math.max(5, h / 2 - 10), NexusTheme.text(), false);
        if (h >= 30) {
            g.text(font, trim(description, w - 52), x + 36, y + Math.max(17, h / 2 + 2), NexusTheme.textDim(), false);
        }
        g.text(font, ">", x + w - 17, y + Math.max(8, h / 2 - 4), hovered ? NexusTheme.accent() : NexusTheme.textDim(), false);
    }

    private void drawSearch(GuiGraphicsExtractor g, int x, int y, int w) {
        drawSurface(g, x, y, w, 26, 7, searchFocused ? ROW_HOVER : ROW_FILL, searchFocused ? NexusTheme.withAlpha(NexusTheme.accent(), 110) : BORDER_SOFT);
        String text = searchQuery.isBlank() ? "Search modules" : searchQuery;
        g.text(font, trim(text, w - 18), x + 9, y + 9, searchQuery.isBlank() ? NexusTheme.textDim() : NexusTheme.text(), false);
        if (searchFocused) {
            int caretX = Math.min(x + w - 9, x + 9 + font.width(searchQuery));
            g.fill(caretX, y + 7, caretX + 1, y + 20, NexusTheme.text());
        }
    }

    private void drawModulePanel(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        drawSurface(g, x, y, w, h, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, activeTextureForPage(selectedPage), x + 14, y + 12, ICON_CARD);
        g.text(font, selectedPage.displayName, x + 40, y + 15, NexusTheme.text(), true);
        g.fill(x + 10, y + 42, x + w - 10, y + 43, BORDER_FAINT);

        int listX = x + 10;
        int listY = y + 52;
        int listW = w - 20;
        int listH = h - 62;
        g.enableScissor(listX, listY, listX + listW, listY + listH);
        int rowY = listY - (int) Math.round(scrollOffset);
        for (Module module : filteredModules()) {
            int rowH = moduleRowHeight(module);
            drawModuleRow(g, module, listX, rowY, listW, rowH, mouseX, mouseY);
            rowY += rowH + 4;
        }
        if (filteredModules().isEmpty()) {
            g.centeredText(font, "No modules found", x + w / 2, listY + 22, NexusTheme.textDim());
        }
        g.disableScissor();
        drawScrollbar(g, listX, listY, listW, listH);
    }

    private void drawModuleRow(GuiGraphicsExtractor g, Module module, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, MODULE_ROW_H);
        boolean enabled = module.isEnabled();
        boolean expanded = expandedModules.getOrDefault(module, false);
        float anim = UiAnimation.toggle(moduleAnim.getOrDefault(module, 0.0f), hovered || enabled, 0.14f);
        moduleAnim.put(module, anim);

        if (hovered || enabled || expanded) {
            NexusRenderer.fillRoundRect(g, x, y, w, h, 5, enabled ? NexusTheme.withAlpha(NexusTheme.accent(), 22) : ROW_FILL);
        }
        g.text(font, trim(module.getName(), w - 64), x + 8, y + 5, enabled ? NexusTheme.text() : NexusRenderer.lerpColor(NexusTheme.textMuted(), NexusTheme.text(), anim), false);
        g.text(font, trim(module.getDescription(), w - 64), x + 8, y + 16, NexusTheme.textDim(), false);
        drawToggle(g, x + w - 28, y + 6, enabled);

        if (hovered) {
            tooltip = module.getDescription();
        }
        if (!expanded) {
            return;
        }

        int settingY = y + MODULE_ROW_H + 6;
        for (Setting<?> setting : module.getSettings()) {
            int settingH = drawSetting(g, setting, x + 8, settingY, w - 16, mouseX, mouseY);
            settingY += settingH + 5;
        }
        String bind = module == bindingModule ? "Press key" : "Bind: " + module.getKeybindName();
        drawSurface(g, x + 8, settingY, w - 16, 20, 4, module == bindingModule ? NexusTheme.withAlpha(NexusTheme.accent(), 40) : ROW_FILL, BORDER_SOFT);
        g.text(font, trim(bind, w - 30), x + 15, settingY + 6, module == bindingModule ? NexusTheme.accentSoft() : NexusTheme.textDim(), false);
    }

    private void drawQuickPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, int w, int h) {
        int topH = moduleQuickTopHeight(h);
        drawSurface(g, x, y, w, topH, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, ASSET_PERFORMANCE, x + 14, y + 12, ICON_CARD);
        g.text(font, "Performance", x + 40, y + 15, NexusTheme.text(), true);
        g.fill(x + 10, y + 42, x + w - 10, y + 43, BORDER_FAINT);

        List<Module> enabledModules = ModuleManager.getEnabled();
        int rowY = y + 54;
        for (int i = 0; i < Math.min(4, enabledModules.size()); i++) {
            Module module = enabledModules.get(i);
            drawSmallStatus(g, x + 12, rowY, w - 24, module.getName(), "Enabled module", true);
            rowY += 27;
        }
        while (rowY < y + topH - 34) {
            drawSmallStatus(g, x + 12, rowY, w - 24, "Available Slot", "Enable modules to fill this list.", false);
            rowY += 27;
        }

        int bottomY = y + topH + CARD_GAP;
        int bottomH = h - topH - CARD_GAP;
        drawSurface(g, x, bottomY, w, bottomH, 8, PANEL_FILL, BORDER_SOFT);
        drawIcon(g, ASSET_BOLT, x + 14, bottomY + 12, ICON_CARD);
        g.text(font, "Quick Actions", x + 40, bottomY + 15, NexusTheme.text(), true);

        int actionY = bottomY + 46;
        int actionH = largeActionHeight(bottomH);
        int step = actionH + 6;
        drawLargeAction(g, x + 12, actionY, w - 24, actionH, ASSET_QUICK_CONNECT, "Quick Connect", "Connect to a server quickly.", mouseX, mouseY);
        drawLargeAction(g, x + 12, actionY + step, w - 24, actionH, ASSET_FOLDER, "Open Config Folder", "Manage your configurations.", mouseX, mouseY);
        drawLargeAction(g, x + 12, actionY + step * 2, w - 24, actionH, ASSET_REFRESH, "Check for Updates", "You're on the latest version.", mouseX, mouseY);
    }

    private void drawSmallStatus(GuiGraphicsExtractor g, int x, int y, int w, String title, String description, boolean enabled) {
        g.text(font, trim(title, w - 48), x, y, enabled ? NexusTheme.text() : NexusTheme.textDim(), false);
        g.text(font, trim(description, w - 48), x, y + 11, NexusTheme.textDim(), false);
        drawToggle(g, x + w - 28, y + 4, enabled);
        g.fill(x, y + 25, x + w, y + 26, BORDER_FAINT);
    }

    private void drawLargeAction(GuiGraphicsExtractor g, int x, int y, int w, int h, Identifier icon, String title, String description, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, h);
        drawSurface(g, x, y, w, h, 7, hovered ? ROW_HOVER : ROW_FILL, hovered ? BORDER_ACTIVE : BORDER_SOFT);
        drawIcon(g, icon, x + 12, y + Math.max(5, (h - ICON_ACTION) / 2), ICON_ACTION);
        g.text(font, trim(title, w - 58), x + 36, y + Math.max(5, h / 2 - 10), NexusTheme.text(), false);
        if (h >= 30) {
            g.text(font, trim(description, w - 52), x + 36, y + Math.max(17, h / 2 + 2), NexusTheme.textDim(), false);
        }
        g.text(font, ">", x + w - 17, y + Math.max(8, h / 2 - 4), hovered ? NexusTheme.accent() : NexusTheme.textDim(), false);
    }

    private int drawSetting(GuiGraphicsExtractor g, Setting<?> setting, int x, int y, int w, int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting booleanSetting) {
            drawSurface(g, x, y, w, 22, 4, ROW_FILL, BORDER_SOFT);
            g.text(font, setting.getName(), x + 7, y + 8, NexusTheme.textMuted(), false);
            drawToggle(g, x + w - 28, y + 5, booleanSetting.getValue());
            setTooltipIfHovered(mouseX, mouseY, x, y, w, 22, setting.getDescription());
            return 22;
        }
        if (setting instanceof NumberSetting numberSetting) {
            drawSurface(g, x, y, w, 34, 4, ROW_FILL, BORDER_SOFT);
            g.text(font, setting.getName(), x + 7, y + 7, NexusTheme.textMuted(), false);
            String value = numberSetting.getDisplayValue();
            g.text(font, value, x + w - 8 - font.width(value), y + 7, NexusTheme.accentSoft(), false);
            int trackX = x + 8;
            int trackY = y + 23;
            int trackW = w - 16;
            double range = Math.max(0.0001, numberSetting.getMax() - numberSetting.getMin());
            double progress = (numberSetting.getValue() - numberSetting.getMin()) / range;
            int fillW = Math.max(4, (int) Math.round(trackW * progress));
            NexusRenderer.fillRoundRect(g, trackX, trackY, trackW, 4, 2, BORDER_FAINT);
            NexusRenderer.fillRoundRect(g, trackX, trackY, fillW, 4, 2, NexusTheme.accent());
            NexusRenderer.fillRoundRect(g, trackX + fillW - 4, trackY - 2, 8, 8, 4, NexusTheme.text());
            setTooltipIfHovered(mouseX, mouseY, x, y, w, 34, setting.getDescription());
            return 34;
        }
        if (setting instanceof ModeSetting modeSetting) {
            boolean open = openDropdowns.getOrDefault(modeSetting, false);
            int h = getSettingHeight(setting);
            drawSurface(g, x, y, w, h, 4, ROW_FILL, BORDER_SOFT);
            g.text(font, setting.getName(), x + 7, y + 8, NexusTheme.textMuted(), false);
            String value = trim(modeSetting.getDisplayValue(), Math.max(30, w / 2));
            g.text(font, value, x + w - 20 - font.width(value), y + 8, NexusTheme.accentSoft(), false);
            g.text(font, open ? "^" : "v", x + w - 11, y + 8, NexusTheme.textDim(), false);
            if (open) {
                int optionY = y + 26;
                for (String mode : modeSetting.getModes()) {
                    boolean selected = mode.equalsIgnoreCase(modeSetting.getValue());
                    NexusRenderer.fillRoundRect(g, x + 6, optionY, w - 12, 16, 3, selected ? NexusTheme.withAlpha(NexusTheme.accent(), 34) : 0x66101519);
                    g.text(font, trim(mode, w - 24), x + 12, optionY + 5, selected ? NexusTheme.text() : NexusTheme.textMuted(), false);
                    optionY += 18;
                }
            }
            setTooltipIfHovered(mouseX, mouseY, x, y, w, h, setting.getDescription());
            return h;
        }
        return 0;
    }

    private void drawToggle(GuiGraphicsExtractor g, int x, int y, boolean enabled) {
        NexusRenderer.fillRoundRect(g, x, y, 24, 12, 6, enabled ? NexusTheme.withAlpha(NexusTheme.accent(), 190) : 0x66313A43);
        int knobX = enabled ? x + 12 : x;
        NexusRenderer.fillRoundRect(g, knobX + 2, y + 2, 8, 8, 4, enabled ? NexusTheme.text() : NexusTheme.textDim());
    }

    private void drawScrollbar(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        int contentH = contentHeight();
        if (contentH <= h) {
            return;
        }
        int thumbH = Math.max(24, (int) (h * (h / (double) contentH)));
        double maxScroll = Math.max(1.0, contentH - h);
        int thumbY = y + (int) ((scrollOffset / maxScroll) * (h - thumbH));
        int barX = x + w - 3;
        g.fill(barX, y, barX + 2, y + h, BORDER_FAINT);
        g.fill(barX, thumbY, barX + 2, thumbY + thumbH, NexusTheme.withAlpha(NexusTheme.accent(), 145));
    }

    private void drawTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.isBlank()) {
            return;
        }
        int tooltipW = Math.min(230, font.width(tooltip) + 14);
        int tooltipX = Math.min(width - tooltipW - 4, mouseX + 10);
        int tooltipY = Math.min(height - 21, mouseY + 14);
        NexusRenderer.fillRoundRect(g, tooltipX, tooltipY, tooltipW, 17, 5, 0xE0101214);
        g.text(font, trim(tooltip, tooltipW - 12), tooltipX + 7, tooltipY + 5, NexusTheme.textMuted(), false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) {
            return false;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int y = winY;

        if (handleHeaderClick(mouseX, mouseY, y) || handleNavClick(mouseX, mouseY, y)) {
            return true;
        }

        if (selectedPage == Page.SETTINGS) {
            searchFocused = false;
            return handleSettingsClick(mouseX, mouseY, y) || super.mouseClicked(event, someBoolean);
        }

        if (selectedPage != Page.HOME && isSearchHovered(mouseX, mouseY, y)) {
            searchFocused = true;
            playClick(1.0f);
            return true;
        }

        searchFocused = false;
        if (selectedPage == Page.HOME) {
            return handleQuickClick(mouseX, mouseY, y) || handleDashboardClick(mouseX, mouseY, y) || super.mouseClicked(event, someBoolean);
        }
        return handleModuleClick(mouseX, mouseY, y) || handleModuleQuickClick(mouseX, mouseY, y) || super.mouseClicked(event, someBoolean);
    }

    private boolean handleHeaderClick(double mouseX, double mouseY, int y) {
        int x = winX + SIDEBAR_W;
        int w = winW - SIDEBAR_W;
        int configW = 78;
        int configX = x + w - PAD - configW;
        if (isHovered(mouseX, mouseY, configX, y + 24, configW, 24)) {
            openConfigFolder();
            return true;
        }
        return false;
    }

    private boolean handleNavClick(double mouseX, double mouseY, int y) {
        int navY = y + 82;
        for (Page page : Page.values()) {
            if (isHovered(mouseX, mouseY, winX + 8, navY, SIDEBAR_W - 16, NAV_ROW_H)) {
                selectedPage = page;
                searchFocused = false;
                scrollOffset = 0.0;
                playClick(1.0f);
                return true;
            }
            navY += NAV_ROW_H + 8;
        }
        return false;
    }

    private boolean handleQuickClick(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int topH = dashboardTopHeight(contentH);
        int bottomH = contentH - topH - CARD_GAP;
        int replayW = dashboardReplayWidth(contentW);
        int cosmeticsW = dashboardCosmeticsWidth(contentW);
        int quickX = contentX + replayW + cosmeticsW + CARD_GAP * 2;
        int quickW = contentW - replayW - cosmeticsW - CARD_GAP * 2;
        int actionY = contentY + topH + CARD_GAP + 42;
        int actionH = quickActionHeight(bottomH);
        int step = actionH + 6;
        if (isHovered(mouseX, mouseY, quickX + 12, actionY, quickW - 24, actionH)) {
            openQuickConnect();
            return true;
        }
        if (isHovered(mouseX, mouseY, quickX + 12, actionY + step, quickW - 24, actionH)) {
            openConfigFolder();
            return true;
        }
        if (isHovered(mouseX, mouseY, quickX + 12, actionY + step * 2, quickW - 24, actionH)) {
            checkForUpdates();
            return true;
        }
        return false;
    }

    private boolean handleModuleQuickClick(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int leftW = modulePanelWidth(contentW);
        int quickX = contentX + leftW + CARD_GAP;
        int quickY = contentY;
        int quickW = contentW - leftW - CARD_GAP;
        int topH = moduleQuickTopHeight(contentH);
        int bottomY = quickY + topH + CARD_GAP;
        int bottomH = contentH - topH - CARD_GAP;
        int actionY = bottomY + 46;
        int actionH = largeActionHeight(bottomH);
        int step = actionH + 6;

        if (isHovered(mouseX, mouseY, quickX + 12, actionY, quickW - 24, actionH)) {
            openQuickConnect();
            return true;
        }
        if (isHovered(mouseX, mouseY, quickX + 12, actionY + step, quickW - 24, actionH)) {
            openConfigFolder();
            return true;
        }
        if (isHovered(mouseX, mouseY, quickX + 12, actionY + step * 2, quickW - 24, actionH)) {
            checkForUpdates();
            return true;
        }
        return false;
    }

    private boolean handleDashboardClick(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int topH = dashboardTopHeight(contentH);
        int bottomH = contentH - topH - CARD_GAP;
        int leftW = dashboardTopLeftWidth(contentW);
        int rightW = contentW - leftW - CARD_GAP;

        if (handleFeatureCardClick(Page.HUD, hudModules(), contentX, contentY, leftW, topH, mouseX, mouseY)) {
            return true;
        }
        if (handleFeatureCardClick(Page.PERFORMANCE, performanceModules(), contentX + leftW + CARD_GAP, contentY, rightW, topH, mouseX, mouseY)) {
            return true;
        }

        int replayW = dashboardReplayWidth(contentW);
        int cosmeticsW = dashboardCosmeticsWidth(contentW);
        int bottomY = contentY + topH + CARD_GAP;
        if (handleFeatureCardClick(Page.REPLAY, replayModules(), contentX, bottomY, replayW, bottomH, mouseX, mouseY)) {
            return true;
        }
        if (handleFeatureCardClick(Page.COSMETICS, cosmeticModules(), contentX + replayW + CARD_GAP, bottomY, cosmeticsW, bottomH, mouseX, mouseY)) {
            return true;
        }
        return false;
    }

    private boolean handleSettingsClick(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int appearanceH = settingsAppearanceHeight(contentH);
        int chipW;
        int gap = 8;

        NexusUiState.ThemeVariant[] variants = NexusUiState.ThemeVariant.values();
        chipW = Math.max(1, (contentW - 24 - gap * (variants.length - 1)) / variants.length);
        int themeY = contentY + 68;
        for (int i = 0; i < variants.length; i++) {
            if (isHovered(mouseX, mouseY, contentX + 12 + i * (chipW + gap), themeY, chipW, 28)) {
                NexusUiState.setThemeVariant(variants[i]);
                playClick(1.0f);
                return true;
            }
        }

        NexusUiState.AccentPreset[] presets = NexusUiState.AccentPreset.values();
        chipW = Math.max(1, (contentW - 24 - gap * (presets.length - 1)) / presets.length);
        int accentY = contentY + 121;
        for (int i = 0; i < presets.length; i++) {
            if (isHovered(mouseX, mouseY, contentX + 12 + i * (chipW + gap), accentY, chipW, 28)) {
                NexusUiState.setAccentPreset(presets[i]);
                playClick(1.0f);
                return true;
            }
        }

        int bottomY = contentY + appearanceH + CARD_GAP;
        int bottomH = contentH - appearanceH - CARD_GAP;
        int actionY = bottomY + 56;
        int actionH = settingsActionHeight(bottomH);
        int actionW = Math.max(1, (contentW - 24 - gap * 2) / 3);
        int firstX = contentX + 12;
        if (isHovered(mouseX, mouseY, firstX, actionY, actionW, actionH)) {
            minecraft.setScreen(new HudEditorScreen(this));
            playClick(1.0f);
            return true;
        }
        if (isHovered(mouseX, mouseY, firstX + actionW + gap, actionY, actionW, actionH)) {
            NexusUiState.resetHudPositions();
            NotificationManager.info("HUD layout reset");
            playClick(1.03f);
            return true;
        }
        if (isHovered(mouseX, mouseY, firstX + (actionW + gap) * 2, actionY, contentW - 24 - (actionW + gap) * 2, actionH)) {
            NexusUiState.resetUiPreferences();
            NotificationManager.info("UI preferences reset");
            playClick(0.95f);
            return true;
        }
        return false;
    }

    private boolean handleFeatureCardClick(Page page, List<Module> modules, int x, int y, int w, int h, double mouseX, double mouseY) {
        int rowY = y + 54;
        int rowLimit = y + h - 40;
        for (Module module : modules) {
            if (rowY + 24 > rowLimit) {
                break;
            }
            if (isHovered(mouseX, mouseY, x + 12, rowY, w - 24, 24)) {
                module.toggle();
                playClick(module.isEnabled() ? 1.1f : 0.9f);
                return true;
            }
            rowY += 27;
        }

        if (isHovered(mouseX, mouseY, x + 12, y + h - 34, w - 24, 24)) {
            selectedPage = page;
            searchFocused = false;
            scrollOffset = 0.0;
            playClick(1.0f);
            return true;
        }
        return false;
    }

    private boolean handleModuleClick(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int leftW = modulePanelWidth(contentW);
        int panelY = contentY + 36;
        int panelH = contentH - 36;
        int listX = contentX + 10;
        int listY = panelY + 52;
        int listW = leftW - 20;
        int listH = panelH - 62;
        if (!isHovered(mouseX, mouseY, listX, listY, listW, listH)) {
            return false;
        }

        int rowY = listY - (int) Math.round(scrollOffset);
        for (Module module : filteredModules()) {
            int rowH = moduleRowHeight(module);
            if (handleModuleRowClick(module, listX, rowY, listW, rowH, mouseX, mouseY)) {
                return true;
            }
            rowY += rowH + 4;
        }
        return false;
    }

    private boolean handleModuleRowClick(Module module, int x, int y, int w, int h, double mouseX, double mouseY) {
        if (!isHovered(mouseX, mouseY, x, y, w, h)) {
            return false;
        }
        if (isHovered(mouseX, mouseY, x + w - 38, y, 38, MODULE_ROW_H)) {
            module.toggle();
            playClick(module.isEnabled() ? 1.1f : 0.9f);
            return true;
        }
        if (isHovered(mouseX, mouseY, x, y, w, MODULE_ROW_H)) {
            expandedModules.put(module, !expandedModules.getOrDefault(module, false));
            playClick(1.02f);
            return true;
        }
        if (expandedModules.getOrDefault(module, false)) {
            int settingY = y + MODULE_ROW_H + 6;
            for (Setting<?> setting : module.getSettings()) {
                int settingH = getSettingHeight(setting);
                if (handleSettingClick(setting, x + 8, settingY, w - 16, settingH, mouseX, mouseY)) {
                    return true;
                }
                settingY += settingH + 5;
            }
            if (isHovered(mouseX, mouseY, x + 8, settingY, w - 16, 20)) {
                bindingModule = bindingModule == module ? null : module;
                playClick(1.0f);
                return true;
            }
        }
        return true;
    }

    private boolean handleSettingClick(Setting<?> setting, int x, int y, int w, int h, double mouseX, double mouseY) {
        if (!isHovered(mouseX, mouseY, x, y, w, h)) {
            return false;
        }
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
            playClick(booleanSetting.getValue() ? 1.08f : 0.92f);
            return true;
        }
        if (setting instanceof NumberSetting numberSetting) {
            updateSlider(numberSetting, x + 8, w - 16, mouseX);
            draggingSlider = numberSetting;
            draggingSliderTrackX = x + 8;
            draggingSliderTrackW = w - 16;
            return true;
        }
        if (setting instanceof ModeSetting modeSetting) {
            boolean open = openDropdowns.getOrDefault(modeSetting, false);
            if (mouseY <= y + 24) {
                openDropdowns.put(modeSetting, !open);
                playClick(1.0f);
                return true;
            }
            if (open) {
                int optionY = y + 26;
                for (String mode : modeSetting.getModes()) {
                    if (isHovered(mouseX, mouseY, x + 6, optionY, w - 12, 16)) {
                        modeSetting.setValue(mode);
                        openDropdowns.put(modeSetting, false);
                        playClick(1.04f);
                        return true;
                    }
                    optionY += 18;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingSlider != null) {
            draggingSlider = null;
            draggingSliderTrackX = 0;
            draggingSliderTrackW = 0;
            NexusConfigManager.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingSlider != null) {
            updateSlider(draggingSlider, draggingSliderTrackX, draggingSliderTrackW, event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = winY + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int contentH = winH - HEADER_H - PAD * 2;
        int leftW = modulePanelWidth(contentW);
        int panelY = contentY + 36;
        int panelH = contentH - 36;
        int listX = contentX + 10;
        int listY = panelY + 52;
        int listW = leftW - 20;
        int listH = panelH - 62;
        if (isHovered(mouseX, mouseY, listX, listY, listW, listH)) {
            double max = Math.max(0.0, contentHeight() - listH);
            scrollOffset = Math.max(0.0, Math.min(max, scrollOffset - scrollY * 18.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!searchFocused || !event.isAllowedChatCharacter()) {
            return super.charTyped(event);
        }
        searchQuery += event.codepointAsString();
        scrollOffset = 0.0;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (bindingModule != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                return true;
            }
            int key = event.key() == GLFW.GLFW_KEY_DELETE || event.key() == GLFW.GLFW_KEY_BACKSPACE ? KeybindUtil.NONE : event.key();
            if (KeybindUtil.isReserved(key)) {
                NotificationManager.error("That key is reserved for the GUI");
                bindingModule = null;
                playClick(0.85f);
                return true;
            }
            bindingModule.setKeybind(key);
            bindingModule = null;
            playClick(1.05f);
            return true;
        }
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                scrollOffset = 0.0;
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isSearchHovered(double mouseX, double mouseY, int y) {
        int contentX = winX + SIDEBAR_W + PAD;
        int contentY = y + HEADER_H + PAD;
        int contentW = winW - SIDEBAR_W - PAD * 2;
        int leftW = modulePanelWidth(contentW);
        return isHovered(mouseX, mouseY, contentX, contentY, leftW, 26);
    }

    private List<Module> filteredModules() {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        return modulesForPage(selectedPage).stream()
            .filter(module -> query.isBlank()
                || module.getName().toLowerCase(Locale.ROOT).contains(query)
                || module.getDescription().toLowerCase(Locale.ROOT).contains(query)
                || module.getSettings().stream().anyMatch(setting -> setting.getName().toLowerCase(Locale.ROOT).contains(query)))
            .toList();
    }

    private int moduleRowHeight(Module module) {
        if (!expandedModules.getOrDefault(module, false)) {
            return MODULE_ROW_H;
        }
        int height = MODULE_ROW_H + 6;
        for (Setting<?> setting : module.getSettings()) {
            height += getSettingHeight(setting) + 5;
        }
        return height + 22;
    }

    private int getSettingHeight(Setting<?> setting) {
        if (setting instanceof ModeSetting modeSetting && openDropdowns.getOrDefault(modeSetting, false)) {
            return 24 + modeSetting.getModes().size() * 18 + 4;
        }
        return setting instanceof NumberSetting ? 34 : 22;
    }

    private int contentHeight() {
        int height = 0;
        for (Module module : filteredModules()) {
            height += moduleRowHeight(module) + 4;
        }
        return Math.max(0, height - 4);
    }

    private void clampScroll() {
        int contentH = winH - HEADER_H - PAD * 2;
        int listH = contentH - 36 - 62;
        scrollOffset = Math.max(0.0, Math.min(Math.max(0, contentHeight() - listH), scrollOffset));
    }

    private void updateSlider(NumberSetting setting, int trackX, int trackW, double mouseX) {
        double raw = Math.max(0.0, Math.min(1.0, (mouseX - trackX) / Math.max(1, trackW)));
        setting.setValue(setting.getMin() + (setting.getMax() - setting.getMin()) * raw, false);
    }

    private void setTooltipIfHovered(int mouseX, int mouseY, int x, int y, int w, int h, String text) {
        if (text != null && !text.isBlank() && isHovered(mouseX, mouseY, x, y, w, h)) {
            tooltip = text;
        }
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private String trim(String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String value = text;
        while (value.length() > 3 && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    private void playClick(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    private String usedMemoryText() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return (used / 1024L / 1024L) + "M / " + (max / 1024L / 1024L) + "M";
    }

    private float memoryProgress() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = Math.max(1L, runtime.maxMemory());
        return Math.max(0.0f, Math.min(1.0f, used / (float) max));
    }

    private String headerSubtitle() {
        return switch (selectedPage) {
            case HOME -> "Optimize your experience. See more. Play smoother.";
            case HUD -> "Show your key information directly on screen.";
            case PERFORMANCE -> "Optimize your experience. See more. Play smoother.";
            case COSMETICS -> "Adjust visual polish and personal style.";
            case REPLAY -> "Capture, inspect, and replay your sessions.";
            case SETTINGS -> "Tune modules, keybinds, and client settings.";
        };
    }

    private String headerTitle() {
        return selectedPage == Page.HOME ? "Nexus Hub" : selectedPage.displayName + " Hub";
    }

    private int dashboardTopHeight(int contentHeight) {
        int desired = Math.min(188, Math.max(172, (contentHeight - CARD_GAP) * 56 / 100));
        return Math.min(desired, Math.max(148, contentHeight - CARD_GAP - 120));
    }

    private int dashboardTopLeftWidth(int contentWidth) {
        return (contentWidth - CARD_GAP) * 44 / 100;
    }

    private int dashboardReplayWidth(int contentWidth) {
        return (contentWidth - CARD_GAP * 2) * 28 / 100;
    }

    private int dashboardCosmeticsWidth(int contentWidth) {
        return (contentWidth - CARD_GAP * 2) * 32 / 100;
    }

    private int modulePanelWidth(int contentWidth) {
        int desired = Math.max(310, (contentWidth - CARD_GAP) * 58 / 100);
        return Math.min(desired, Math.max(250, contentWidth - CARD_GAP - 210));
    }

    private int quickActionHeight(int cardHeight) {
        int available = Math.max(66, cardHeight - 48);
        return Math.max(22, Math.min(34, (available - 12) / 3));
    }

    private int moduleQuickTopHeight(int contentHeight) {
        return Math.max(110, contentHeight - CARD_GAP - 168);
    }

    private int largeActionHeight(int cardHeight) {
        int available = Math.max(54, cardHeight - 52);
        return Math.max(24, Math.min(34, (available - 12) / 3));
    }

    private int settingsAppearanceHeight(int contentHeight) {
        return Math.min(166, Math.max(154, contentHeight * 52 / 100));
    }

    private int settingsActionHeight(int cardHeight) {
        return Math.max(30, Math.min(40, cardHeight - 68));
    }

    private List<Module> hudModules() {
        return modulesByName("Keystrokes", "Armor Status", "Coordinates", "FPS Display", "CPS", "Clock");
    }

    private List<Module> performanceModules() {
        return modulesByName("Sprint", "Full Bright", "Zoom", "Auto Respawn");
    }

    private List<Module> cosmeticModules() {
        return ModuleManager.getByCategory(Category.COSMETIC);
    }

    private List<Module> replayModules() {
        return modulesByName("Zoom", "Auto Respawn", "Coordinates");
    }

    private List<Module> modulesForPage(Page page) {
        return switch (page) {
            case HOME, SETTINGS -> ModuleManager.getAll();
            case HUD -> hudModules();
            case PERFORMANCE -> performanceModules();
            case COSMETICS -> cosmeticModules();
            case REPLAY -> replayModules();
        };
    }

    private List<Module> modulesByName(String... names) {
        List<Module> modules = new ArrayList<>();
        for (String name : names) {
            Module module = ModuleManager.findByName(name);
            if (module != null && !modules.contains(module)) {
                modules.add(module);
            }
        }
        return modules;
    }

    private String currentPlayerName() {
        Minecraft client = Minecraft.getInstance();
        GameProfile profile = client.getGameProfile();
        if (profile != null && profile.name() != null && !profile.name().isBlank()) {
            return profile.name();
        }
        if (client.getUser() != null && client.getUser().getName() != null && !client.getUser().getName().isBlank()) {
            return client.getUser().getName();
        }
        return "Player";
    }

    private String displayName(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }
        String[] parts = id.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private void drawPlayerHead(GuiGraphicsExtractor g, int x, int y, int size) {
        NexusRenderer.fillRoundRect(g, x - 1, y - 1, size + 2, size + 2, 4, BORDER_SOFT);
        PlayerSkin skin = currentPlayerSkin();
        Identifier texture = skin.body().texturePath();
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 8.0F, 8.0F, size, size, 8, 8, 64, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 40.0F, 8.0F, size, size, 8, 8, 64, 64);
    }

    private PlayerSkin currentPlayerSkin() {
        Minecraft client = Minecraft.getInstance();
        GameProfile profile = client.getGameProfile();
        if (profile == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }

        if (cachedProfile != profile) {
            cachedProfile = profile;
            cachedSkinLookup = client.getSkinManager().createLookup(profile, true);
        }
        return cachedSkinLookup != null ? cachedSkinLookup.get() : DefaultPlayerSkin.get(profile);
    }

    private void openQuickConnect() {
        minecraft.setScreen(new JoinMultiplayerScreen(this));
        playClick(1.0f);
    }

    private void openConfigFolder() {
        NexusConfigManager.save();
        Util.getPlatform().openPath(NexusConfigManager.getConfigDirectory());
        NotificationManager.info("Opened config folder");
        playClick(1.03f);
    }

    private void checkForUpdates() {
        NotificationManager.info("You're on the latest version");
        playClick(1.08f);
    }

    private static Identifier guiTexture(String name) {
        return Identifier.fromNamespaceAndPath("nexus-client", "textures/gui/" + name);
    }

    private void drawIcon(GuiGraphicsExtractor g, Identifier texture, int x, int y, int size) {
        drawAsset(g, texture, x, y, size, size, 32);
    }

    private void drawAsset(GuiGraphicsExtractor g, Identifier texture, int x, int y, int w, int h, int textureSize) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, w, h, textureSize, textureSize, textureSize, textureSize);
    }

    private void drawSurface(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int fill, int border) {
        if (w <= 0 || h <= 0) {
            return;
        }
        NexusRenderer.fillRoundRect(g, x, y, w, h, radius, border);
        if (w > 2 && h > 2) {
            NexusRenderer.fillRoundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), fill);
        }
    }

    private void drawPageIcon(GuiGraphicsExtractor g, Page page, int x, int y, boolean active) {
        drawIcon(g, active ? activeTextureForPage(page) : textureForPage(page), x, y, ICON_NAV);
    }

    private Identifier textureForPage(Page page) {
        return switch (page) {
            case HOME -> ASSET_HOME;
            case HUD -> ASSET_HUD;
            case PERFORMANCE -> ASSET_PERFORMANCE;
            case COSMETICS -> ASSET_COSMETICS;
            case REPLAY -> ASSET_REPLAY;
            case SETTINGS -> ASSET_SETTINGS;
        };
    }

    private Identifier activeTextureForPage(Page page) {
        return switch (page) {
            case HOME -> ASSET_HOME_ACTIVE;
            case HUD -> ASSET_HUD_ACTIVE;
            case PERFORMANCE -> ASSET_PERFORMANCE_ACTIVE;
            case COSMETICS -> ASSET_COSMETICS_ACTIVE;
            case REPLAY -> ASSET_REPLAY_ACTIVE;
            case SETTINGS -> ASSET_SETTINGS_ACTIVE;
        };
    }
}
