package nexus.zigliix.com.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import nexus.zigliix.com.client.gui.component.MenuBackdropBlur;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.UiAnimation;

public class MainMenuScreen extends TitleScreen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final String[] BUTTONS    = {"Singleplayer", "Multiplayer"};
    private static final int BUTTON_WIDTH    = 260;
    private static final int BUTTON_HEIGHT   = 44;
    private static final int BUTTON_GAP      = 10;
    private static final int BUTTON_RADIUS   = 10;

    /** Vertical offset of the button block relative to screen centre. */
    private static final int BLOCK_OFFSET_Y  = 28;

    /** Gap between brand block bottom and first button top. */
    private static final int BRAND_GAP       = 30;

    // ── Small icon buttons (bottom-right row) ────────────────────────────────
    private static final int ICON_SIZE       = 28;
    private static final int ICON_RADIUS     = 7;
    private static final int ICON_MARGIN     = 18;
    private static final int ICON_GAP        = 8;

    // ── Palette — Lunar-style matte greys, nearly no blue tint ───────────────
    /**
     * Button base background: very dark grey, semi-transparent.
     * ~32 % opacity so the blur shows through nicely.
     */
    private static final int BUTTON_BASE     = 0x52161718;
    /** Button hover: slightly more opaque, 2-3 % lighter. */
    private static final int BUTTON_HOVER    = 0x6A1E2022;
    /**
     * Thin outer border stroke in both states —
     * very low alpha so it reads as a subtle outline, not a glow.
     */
    private static final int BORDER_IDLE     = 0x1EFFFFFF;
    private static final int BORDER_HOVER    = 0x30FFFFFF;

    /** Icon button colours — match button material but smaller alpha. */
    private static final int ICON_BASE       = 0x44141516;
    private static final int ICON_HOVER      = 0x60191B1D;
    private static final int ICON_BORDER     = 0x18FFFFFF;
    private static final int ICON_BORDER_HOV = 0x28FFFFFF;

    /** Text: near-white, barely tinted. */
    private static final int TEXT_COLOR      = 0xFFF0F2F5;
    private static final int TEXT_MUTED      = 0xBBCED4DC;

    /** Title: pure white with very subtle warm tint. */
    private static final int TITLE_COLOR     = 0xFFF8FAFB;


    /** Background veil drawn over the panorama — darker than before for Lunar feel. */
    private static final int BG_VEIL         = 0x72060810;

    // ── State ──────────────────────────────────────────────────────────────────
    private final float[] buttonHover  = new float[BUTTONS.length];
    private final float[] buttonPress  = new float[BUTTONS.length];
    private float settingsHover;
    private float settingsPress;
    private float quitHover;
    private float quitPress;
    private float reveal;

    @Override
    protected void init() {
        // Keep the custom menu as the only interactive layer.
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Background pass
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractPanorama(g, partialTick);
        // Heavier dark veil for the Lunar "clean, dim" feel
        g.fill(0, 0, width, height, BG_VEIL);
        MenuBackdropBlur.captureMainTarget();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Render pass
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        MenuBackdropBlur.ensureTarget();
        reveal = UiAnimation.approach(reveal, 1.0f, 0.20f);
        float eased = UiAnimation.easeOutCubic(reveal);

        int centerX = width / 2;
        int buttonWidth = mainButtonWidth();

        // ── Button block ─────────────────────────────────────────────────────
        int totalBtnH = BUTTONS.length * BUTTON_HEIGHT + (BUTTONS.length - 1) * BUTTON_GAP;
        int btnStartY = height / 2 - totalBtnH / 2 + BLOCK_OFFSET_Y;
        float introSlide = (1.0f - eased) * 14.0f;

        for (int i = 0; i < BUTTONS.length; i++) {
            int bx = centerX - buttonWidth / 2;
            int by = btnStartY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            boolean hov = contains(mouseX, mouseY, bx, by, buttonWidth, BUTTON_HEIGHT);
            buttonHover[i] = UiAnimation.toggle(buttonHover[i], hov, 0.16f);
            buttonPress[i] = UiAnimation.toggle(buttonPress[i], false, 0.28f);
            renderButton(g, bx, Math.round(by + introSlide), buttonWidth, BUTTONS[i], buttonHover[i], buttonPress[i], eased);
        }

        // ── Brand block (title above buttons) ────────────────────────────────
        int brandBaseY = btnStartY - BRAND_GAP;
        g.nextStratum();
        renderBrand(g, centerX, brandBaseY, eased);

        // ── Icon buttons ────────────────────────────────────────────────────────
        int iconY  = height - ICON_SIZE - ICON_MARGIN;
        // Utility cluster — bottom-right
        int settingsX = width - ICON_SIZE - ICON_MARGIN;
        boolean sHov = contains(mouseX, mouseY, settingsX, iconY, ICON_SIZE, ICON_SIZE);
        settingsHover = UiAnimation.toggle(settingsHover, sHov, 0.16f);
        settingsPress = UiAnimation.toggle(settingsPress, false, 0.30f);
        renderIconButton(g, settingsX, iconY, settingsHover, settingsPress, eased, this::drawGearIcon);
        int quitX = settingsX - ICON_SIZE - ICON_GAP;
        boolean qHov = contains(mouseX, mouseY, quitX, iconY, ICON_SIZE, ICON_SIZE);
        quitHover = UiAnimation.toggle(quitHover, qHov, 0.16f);
        quitPress = UiAnimation.toggle(quitPress, false, 0.30f);
        renderIconButton(g, quitX, iconY, quitHover, quitPress, eased, this::drawQuitIcon);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Input
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) return false;

        int centerX  = width / 2;
        int buttonWidth = mainButtonWidth();
        int totalBtnH = BUTTONS.length * BUTTON_HEIGHT + (BUTTONS.length - 1) * BUTTON_GAP;
        int btnStartY = height / 2 - totalBtnH / 2 + BLOCK_OFFSET_Y;

        for (int i = 0; i < BUTTONS.length; i++) {
            int bx = centerX - buttonWidth / 2;
            int by = btnStartY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            if (contains(event.x(), event.y(), bx, by, buttonWidth, BUTTON_HEIGHT)) {
                buttonPress[i] = 1.0f;
                playClick(1.0f);
                Minecraft mc = Minecraft.getInstance();
                switch (i) {
                    case 0 -> mc.setScreen(new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(this));
                    case 1 -> mc.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(this));
                    default -> { }
                }
                return true;
            }
        }

        int iconY     = height - ICON_SIZE - ICON_MARGIN;
        int settingsX = width  - ICON_SIZE - ICON_MARGIN;
        if (contains(event.x(), event.y(), settingsX, iconY, ICON_SIZE, ICON_SIZE)) {
            settingsPress = 1.0f;
            playClick(1.0f);
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(this, mc.options, false));
            return true;
        }
        int quitX = settingsX - ICON_SIZE - ICON_GAP;
        if (contains(event.x(), event.y(), quitX, iconY, ICON_SIZE, ICON_SIZE)) {
            quitPress = 1.0f;
            playClick(0.9f);
            Minecraft.getInstance().stop();
            return true;
        }

        return super.mouseClicked(event, someBoolean);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Draw helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Brand block:
     *   - Large client name (drawn 2× with shadow offset to 'upscale' MC's fixed font)
     *   - Small subtitle line below
     */
    private void renderBrand(GuiGraphicsExtractor g, int cx, int bottomY, float rev) {
        // Title — two shadow passes + crisp top layer for a pseudo-bold look
        int titleAlpha  = Math.round(255 * rev);
        int shadowAlpha = Math.round(90 * rev);

        int titleY = bottomY - 12; // top of the title text

        int sha = NexusRenderer.withAlpha(0xFF050810, shadowAlpha);
        g.centeredText(font, "Nexus Client", cx + 1, titleY + 2, sha);
        g.centeredText(font, "Nexus Client", cx,     titleY + 1, sha);
        g.centeredText(font, "Nexus Client", cx, titleY, NexusRenderer.withAlpha(TITLE_COLOR, titleAlpha));
    }

    /**
     * Main navigation button — flat matte style, single border stroke, no sheen/glow.
     */
    private void renderButton(GuiGraphicsExtractor g, int x, int y, int buttonWidth, String label,
                              float hover, float press, float rev) {
        float scale    = 1.0f - press * 0.025f;
        int rw = Math.max(1, Math.round(buttonWidth  * scale));
        int rh = Math.max(1, Math.round(BUTTON_HEIGHT * scale));
        int rx = x + (buttonWidth  - rw) / 2;
        int ry = y + (BUTTON_HEIGHT - rh) / 2;

        // Lerp bg colour on hover
        int bg     = NexusRenderer.lerpColor(BUTTON_BASE, BUTTON_HOVER, hover);
        int bgA    = Math.round(((bg >>> 24) & 0xFF) * rev);
        int border = NexusRenderer.lerpColor(BORDER_IDLE, BORDER_HOVER, hover);
        int borderA = Math.round(((border >>> 24) & 0xFF) * rev);
        int textA  = Math.round(255 * rev);
        int txtSha = NexusRenderer.withAlpha(0xFF02040A, Math.round(50 * rev));

        bg     = NexusRenderer.withAlpha(bg,     bgA);
        border = NexusRenderer.withAlpha(border, borderA);

        // Try hardware blur, else boost bg alpha slightly
        boolean blurred = MenuBackdropBlur.addBlurredRoundRect(g, rx, ry, rw, rh, BUTTON_RADIUS);
        if (!blurred) bg = boostAlpha(bg, 20);

        g.nextStratum();

        // 1px border stroke
        NexusRenderer.fillRoundRect(g, rx, ry, rw, rh, BUTTON_RADIUS, border);
        // Inner fill (inset 1px)
        NexusRenderer.fillRoundRect(g, rx + 1, ry + 1,
                Math.max(1, rw - 2), Math.max(1, rh - 2),
                Math.max(1, BUTTON_RADIUS - 1), bg);

        // Label
        int textY = ry + (rh - 8) / 2;
        g.centeredText(font, label, rx + rw / 2, textY + 1, txtSha);
        g.centeredText(font, label, rx + rw / 2, textY,
                NexusRenderer.withAlpha(TEXT_COLOR, textA));
    }

    /**
     * Small square icon button — same material as main buttons.
     * The {@code iconDrawer} lambda receives the pixel-centre coordinates.
     */
    @FunctionalInterface
    private interface IconDrawer {
        void draw(GuiGraphicsExtractor g, int cx, int cy, int color);
    }

    private void renderIconButton(GuiGraphicsExtractor g, int x, int y,
                                  float hover, float press, float rev,
                                  IconDrawer iconDrawer) {
        float scale  = 1.0f - press * 0.04f;
        int rs  = Math.max(1, Math.round(ICON_SIZE * scale));
        int rx  = x + (ICON_SIZE - rs) / 2;
        int ry  = y + (ICON_SIZE - rs) / 2;

        int bg     = NexusRenderer.lerpColor(ICON_BASE, ICON_HOVER, hover);
        int bgA    = Math.round(((bg >>> 24) & 0xFF) * rev);
        int border = NexusRenderer.lerpColor(ICON_BORDER, ICON_BORDER_HOV, hover);
        int borderA = Math.round(((border >>> 24) & 0xFF) * rev);

        bg     = NexusRenderer.withAlpha(bg,     bgA);
        border = NexusRenderer.withAlpha(border, borderA);

        boolean blurred = MenuBackdropBlur.addBlurredRoundRect(g, rx, ry, rs, rs, ICON_RADIUS);
        if (!blurred) bg = boostAlpha(bg, 18);

        g.nextStratum();
        NexusRenderer.fillRoundRect(g, rx, ry, rs, rs, ICON_RADIUS, border);
        NexusRenderer.fillRoundRect(g, rx + 1, ry + 1,
                Math.max(1, rs - 2), Math.max(1, rs - 2),
                Math.max(1, ICON_RADIUS - 1), bg);

        int iconColor = NexusRenderer.withAlpha(
                NexusRenderer.lerpColor(TEXT_MUTED, TEXT_COLOR, hover),
                Math.round(255 * rev));
        iconDrawer.draw(g, rx + rs / 2, ry + rs / 2, iconColor);
    }

    // ── Gear icon ──────────────────────────────────────────────────────────────

    private void drawGearIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c    = NexusRenderer.withAlpha(color, 242);
        int hole = 0xD8020407;

        // ── Cardinal teeth — 2 px wide, 3 px visible beyond ring (r=3.5) ────
        g.fill(cx - 1, cy - 6, cx + 1, cy - 3, c);   // top
        g.fill(cx - 1, cy + 3, cx + 1, cy + 6, c);   // bottom
        g.fill(cx - 6, cy - 1, cx - 3, cy + 1, c);   // left
        g.fill(cx + 3, cy - 1, cx + 6, cy + 1, c);   // right

        // ── Diagonal teeth — 3×3 blocks, inner corner INSIDE ring so ────────
        //    they are welded to it (no floating sun-ray gap)
        //    Ring edge at 45° ≈ r/√2 ≈ 2.47 px per axis  →  inner corner at
        //    (±2,±2) is already inside ring at dist=2.83 < 3.5 ✓
        g.fill(cx - 5, cy - 5, cx - 2, cy - 2, c);   // TL
        g.fill(cx + 2, cy - 5, cx + 5, cy - 2, c);   // TR
        g.fill(cx - 5, cy + 2, cx - 2, cy + 5, c);   // BL
        g.fill(cx + 2, cy + 2, cx + 5, cy + 5, c);   // BR

        // ── Ring body — smaller disc so teeth protrude clearly ───────────────
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 3.5f, c);

        // ── Centre hole ───────────────────────────────────────────────────────
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 1.8f, hole);
    }

    // ── Quit (power) icon ─────────────────────────────────────────────────────

    private void drawQuitIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 220);
        // × symbol — two diagonal strokes made of small rectangles
        // top-left → bottom-right
        g.fill(cx - 4, cy - 4, cx - 2, cy - 2, c);
        g.fill(cx - 2, cy - 2, cx,     cy,     c);
        g.fill(cx,     cy,     cx + 2, cy + 2, c);
        g.fill(cx + 2, cy + 2, cx + 4, cy + 4, c);
        // top-right → bottom-left
        g.fill(cx + 2, cy - 4, cx + 4, cy - 2, c);
        g.fill(cx,     cy - 2, cx + 2, cy,     c);
        g.fill(cx - 2, cy,     cx,     cy + 2, c);
        g.fill(cx - 4, cy + 2, cx - 2, cy + 4, c);
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    private void playClick(float pitch) {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    private boolean contains(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int boostAlpha(int color, int extra) {
        int a = Math.min(255, ((color >>> 24) & 0xFF) + extra);
        return NexusRenderer.withAlpha(color, a);
    }

    private int mainButtonWidth() {
        return Math.max(120, Math.min(BUTTON_WIDTH, width - 48));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
