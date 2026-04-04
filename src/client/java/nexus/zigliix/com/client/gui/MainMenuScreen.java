package nexus.zigliix.com.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;

public class MainMenuScreen extends Screen {

    // Hover animation per button [0..1]
    private final float[] btnHover = new float[4];
    private static final String[] BTN_LABELS = {
            "Singleplayer", "Multiplayer", "Options", "Quit"
    };

    // Subtle animated offset for the decorative grid
    private long initTime;

    public MainMenuScreen() {
        super(Component.literal("Nexus Client"));
    }

    @Override
    protected void init() {
        initTime = System.currentTimeMillis();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // ── Background ──────────────────────────────────────────
        NexusRenderer.fillGradientV(g, 0, 0, width, height, NexusTheme.BG, 0xFF0A0C14);

        // Subtle grid overlay (low-alpha horizontal lines)
        for (int ly = 0; ly < height; ly += 28) {
            g.fill(0, ly, width, ly + 1, 0x08FFFFFF);
        }

        // Side vignettes
        NexusRenderer.fillGradientV(g, 0, 0, width, 80, 0x40000000, 0x00000000);
        NexusRenderer.fillGradientV(g, 0, height - 80, width, 80, 0x00000000, 0x40000000);

        int cx = width / 2;

        // ── Logo area ────────────────────────────────────────────
        int logoY = height / 2 - 100;

        // Thin top accent line
        NexusRenderer.fillGradientV(g, cx - 60, logoY - 10, 120, 1, 0x00000000, NexusTheme.ACCENT);
        g.fill(cx - 60, logoY - 10, cx + 60, logoY - 9, NexusTheme.ACCENT);

        // "NEXUS" — large, spaced out, rendered per-character with accent shading
        String logoText = "N E X U S";
        int logoW = font.width(logoText) * 2;
        g.pose().pushMatrix();
        g.pose().scale(2f, 2f);
        NexusRenderer.drawCenteredString(g, font, logoText, cx / 2, (logoY) / 2, NexusTheme.TEXT);
        g.pose().popMatrix();

        // Subtitle
        NexusRenderer.drawCenteredString(g, font, "CLIENT  ·  v1.0.0", cx, logoY + 24, NexusTheme.ACCENT_SOFT);

        // Separator below logo
        int sepY = logoY + 36;
        g.fill(cx - 80, sepY, cx + 80, sepY + 1, NexusTheme.SEPARATOR);

        // ── Buttons ──────────────────────────────────────────────
        int btnAreaY = sepY + 16;
        int bw = NexusTheme.BTN_WIDTH;
        int bh = NexusTheme.BTN_HEIGHT;
        int bx = cx - bw / 2;

        for (int i = 0; i < BTN_LABELS.length; i++) {
            int by = btnAreaY + i * (bh + 6);
            boolean hov = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
            btnHover[i] = hov
                    ? Math.min(1f, btnHover[i] + 0.15f)
                    : Math.max(0f, btnHover[i] - 0.10f);
            renderButton(g, bx, by, bw, bh, BTN_LABELS[i], btnHover[i]);
        }

        // ── Version watermark ─────────────────────────────────────
        NexusRenderer.drawString(g, font, "Nexus Client  |  Fabric 26.1",
                NexusTheme.HUD_PADDING, height - 12, NexusTheme.TEXT_MUTED);
        NexusRenderer.drawString(g, font, "zigliix",
                width - font.width("zigliix") - NexusTheme.HUD_PADDING, height - 12, NexusTheme.TEXT_MUTED);

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void renderButton(GuiGraphicsExtractor g, int bx, int by, int bw, int bh, String label, float hoverT) {
        // Background interpolated on hover
        int bg = NexusRenderer.lerpColor(NexusTheme.PANEL, NexusTheme.PANEL_ALT, hoverT);
        NexusRenderer.fillRoundRect(g, bx, by, bw, bh, NexusTheme.CORNER_RADIUS, bg);

        // Thin top accent line on hover
        if (hoverT > 0.01f) {
            int accentAlpha = (int)(hoverT * 0xFF);
            int accentColor = (accentAlpha << 24) | (NexusTheme.ACCENT & 0x00FFFFFF);
            g.fill(bx + NexusTheme.CORNER_RADIUS, by, bx + bw - NexusTheme.CORNER_RADIUS, by + 1, accentColor);
        }

        // Border
        NexusRenderer.drawSeparator(g, bx, by + bh - 1, bw, NexusTheme.SEPARATOR);

        // Label
        int textColor = NexusRenderer.lerpColor(NexusTheme.TEXT_MUTED, NexusTheme.TEXT, hoverT);
        NexusRenderer.drawCenteredString(g, font, label, bx + bw / 2, by + (bh - 7) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) return false;
        double mx = event.x();
        double my = event.y();
        int cx = width / 2;
        int bw = NexusTheme.BTN_WIDTH, bh = NexusTheme.BTN_HEIGHT;
        int bx = cx - bw / 2;
        int logoY = height / 2 - 100;
        int btnAreaY = logoY + 36 + 16 + 1;

        for (int i = 0; i < BTN_LABELS.length; i++) {
            int by = btnAreaY + i * (bh + 6);
            if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
                handleButton(i);
                return true;
            }
        }
        return super.mouseClicked(event, someBoolean);
    }

    private void handleButton(int index) {
        Minecraft mc = Minecraft.getInstance();
        switch (index) {
            case 0 -> mc.setScreen(new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(this));
            case 1 -> mc.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(this));
            case 2 -> mc.setScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(this, mc.options, false));
            case 3 -> mc.stop();
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
