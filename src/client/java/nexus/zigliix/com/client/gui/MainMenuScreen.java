package nexus.zigliix.com.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import nexus.zigliix.com.client.gui.component.MenuBackdropBlur;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.UiAnimation;

public class MainMenuScreen extends TitleScreen {
    private static final String[] BUTTONS = {"Singleplayer", "Multiplayer", "Store"};
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 28;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_RADIUS = 0;
    private static final int ICON_SIZE = 28;
    private static final int ICON_GAP = 12;
    private static final int LOGO_SIZE = 66;

    private static final int BG_VEIL = 0x5A020508;
    private static final int GLASS = 0x44202A34;
    private static final int GLASS_HOVER = 0x66384450;
    private static final int GLASS_ACTIVE = 0x3CEAFBFF;
    private static final int BORDER = 0x38FFFFFF;
    private static final int BORDER_HOVER = 0x66FFFFFF;
    private static final int TEXT = 0xFFF5F7FB;
    private static final int TEXT_DIM = 0xC8D7DEE8;

    private static final Identifier MARK = Identifier.fromNamespaceAndPath("nexus-client", "textures/gui/nexus_mark.png");

    private final float[] buttonHover = new float[BUTTONS.length];
    private final float[] buttonPress = new float[BUTTONS.length];
    private final float[] iconHover = new float[5];
    private float reveal;

    @Override
    protected void init() {
        // Vanilla title widgets are intentionally replaced by this screen.
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractPanorama(g, partialTick);
        g.blurBeforeThisStratum();
        g.fill(0, 0, width, height, BG_VEIL);
        MenuBackdropBlur.captureMainTarget();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        MenuBackdropBlur.ensureTarget();
        reveal = UiAnimation.approach(reveal, 1.0f, 0.18f);
        float eased = UiAnimation.easeOutCubic(reveal);

        drawPromo(g, eased);
        drawInfo(g);
        drawBrand(g, eased);
        drawButtons(g, mouseX, mouseY, eased);
        drawIconRow(g, mouseX, mouseY, eased);
    }

    private void drawPromo(GuiGraphicsExtractor g, float alpha) {
        int x = 8;
        int y = 10;
        int w = 252;
        int h = 50;
        drawGlass(g, x, y, w, h, 0, withFade(0x62313A46, alpha), withFade(BORDER, alpha));
        g.blit(RenderPipelines.GUI_TEXTURED, MARK, x + 7, y + 8, 0.0F, 0.0F, 34, 34, 64, 64, 64, 64);
        g.text(font, "What's The BEST Minecraft Client in 2026?", x + 48, y + 13, withFade(TEXT, alpha), true);
        g.text(font, "Nexus Client", x + 48, y + 29, withFade(TEXT_DIM, alpha), false);
    }

    private void drawInfo(GuiGraphicsExtractor g) {
        int x = width - 28;
        int y = 15;
        NexusRenderer.fillCircle(g, x + 8.5f, y + 8.5f, 8.0f, 0x34FFFFFF);
        g.centeredText(font, "i", x + 8, y + 5, TEXT);
    }

    private void drawBrand(GuiGraphicsExtractor g, float alpha) {
        int centerX = width / 2;
        int logoY = Math.max(74, height / 2 - 220);
        int logoX = centerX - LOGO_SIZE / 2;
        NexusRenderer.fillCircle(g, centerX + 0.5f, logoY + LOGO_SIZE / 2.0f + 0.5f, LOGO_SIZE / 2.0f + 5.0f, withFade(0x44FFFFFF, alpha));
        NexusRenderer.fillCircle(g, centerX + 0.5f, logoY + LOGO_SIZE / 2.0f + 0.5f, LOGO_SIZE / 2.0f, withFade(0xF4FFFFFF, alpha));
        g.blit(RenderPipelines.GUI_TEXTURED, MARK, logoX + 13, logoY + 13, 0.0F, 0.0F, 40, 40, 64, 64, 64, 64);

        String title = "NEXUS CLIENT";
        int titleY = logoY + LOGO_SIZE + 24;
        g.centeredText(font, title, centerX + 1, titleY + 1, withFade(0x6A000000, alpha));
        g.centeredText(font, title, centerX, titleY, withFade(TEXT, alpha));
    }

    private void drawButtons(GuiGraphicsExtractor g, int mouseX, int mouseY, float alpha) {
        int centerX = width / 2;
        int buttonWidth = mainButtonWidth();
        int totalHeight = BUTTONS.length * BUTTON_HEIGHT + (BUTTONS.length - 1) * BUTTON_GAP;
        int startY = Math.max(190, height / 2 + 34);
        float slide = (1.0f - alpha) * 12.0f;

        for (int i = 0; i < BUTTONS.length; i++) {
            int x = centerX - buttonWidth / 2;
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP) + Math.round(slide);
            boolean hovered = contains(mouseX, mouseY, x, y, buttonWidth, BUTTON_HEIGHT);
            buttonHover[i] = UiAnimation.toggle(buttonHover[i], hovered, 0.14f);
            buttonPress[i] = UiAnimation.toggle(buttonPress[i], false, 0.25f);
            renderButton(g, x, y, buttonWidth, BUTTONS[i], buttonHover[i], buttonPress[i], alpha);
        }
    }

    private void renderButton(GuiGraphicsExtractor g, int x, int y, int w, String label, float hover, float press, float alpha) {
        int fill = NexusRenderer.lerpColor(GLASS, GLASS_HOVER, hover);
        if ("Store".equals(label)) {
            fill = NexusRenderer.lerpColor(GLASS_ACTIVE, 0x58EAFBFF, hover);
        }
        int border = NexusRenderer.lerpColor(BORDER, BORDER_HOVER, hover);

        int inset = Math.round(press * 1.0f);
        drawGlass(g, x + inset, y + inset, w - inset * 2, BUTTON_HEIGHT - inset * 2, BUTTON_RADIUS, withFade(fill, alpha), withFade(border, alpha));
        g.centeredText(font, label, x + w / 2, y + 10, withFade(TEXT, alpha));
    }

    private void drawIconRow(GuiGraphicsExtractor g, int mouseX, int mouseY, float alpha) {
        int count = iconHover.length;
        int totalW = count * ICON_SIZE + (count - 1) * ICON_GAP;
        int x = width / 2 - totalW / 2;
        int buttonBlockY = Math.max(190, height / 2 + 34);
        int y = buttonBlockY + BUTTONS.length * BUTTON_HEIGHT + (BUTTONS.length - 1) * BUTTON_GAP + 26;

        for (int i = 0; i < count; i++) {
            int ix = x + i * (ICON_SIZE + ICON_GAP);
            boolean hovered = contains(mouseX, mouseY, ix, y, ICON_SIZE, ICON_SIZE);
            iconHover[i] = UiAnimation.toggle(iconHover[i], hovered, 0.14f);
            drawIconButton(g, ix, y, i, iconHover[i], alpha);
        }
    }

    private void drawIconButton(GuiGraphicsExtractor g, int x, int y, int index, float hover, float alpha) {
        int fill = NexusRenderer.lerpColor(GLASS, GLASS_HOVER, hover);
        int border = NexusRenderer.lerpColor(BORDER, BORDER_HOVER, hover);
        drawGlass(g, x, y, ICON_SIZE, ICON_SIZE, 0, withFade(fill, alpha), withFade(border, alpha));

        int color = withFade(TEXT, alpha);
        int cx = x + ICON_SIZE / 2;
        int cy = y + ICON_SIZE / 2;
        switch (index) {
            case 0 -> drawGearIcon(g, cx, cy, color);
            case 1 -> drawWardrobeIcon(g, cx, cy, color);
            case 2 -> drawLoginIcon(g, cx, cy, color);
            case 3 -> drawGlobeIcon(g, cx, cy, color);
            default -> drawQuitIcon(g, cx, cy, color);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) {
            return false;
        }

        int centerX = width / 2;
        int buttonWidth = mainButtonWidth();
        int startY = Math.max(190, height / 2 + 34);
        for (int i = 0; i < BUTTONS.length; i++) {
            int x = centerX - buttonWidth / 2;
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            if (contains(event.x(), event.y(), x, y, buttonWidth, BUTTON_HEIGHT)) {
                buttonPress[i] = 1.0f;
                playClick(1.0f);
                handleButton(i);
                return true;
            }
        }

        int iconIndex = iconAt(event.x(), event.y());
        if (iconIndex >= 0) {
            playClick(iconIndex == 4 ? 0.9f : 1.0f);
            handleIcon(iconIndex);
            return true;
        }

        return super.mouseClicked(event, someBoolean);
    }

    private void handleButton(int index) {
        Minecraft mc = Minecraft.getInstance();
        switch (index) {
            case 0 -> mc.setScreen(new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(this));
            case 1 -> mc.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(this));
            case 2 -> mc.setScreen(new ClickGuiScreen());
            default -> { }
        }
    }

    private void handleIcon(int index) {
        Minecraft mc = Minecraft.getInstance();
        switch (index) {
            case 0 -> mc.setScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(this, mc.options, false));
            case 1, 2 -> mc.setScreen(new ClickGuiScreen());
            case 3 -> mc.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(this));
            case 4 -> mc.stop();
            default -> { }
        }
    }

    private int iconAt(double mouseX, double mouseY) {
        int count = iconHover.length;
        int totalW = count * ICON_SIZE + (count - 1) * ICON_GAP;
        int x = width / 2 - totalW / 2;
        int buttonBlockY = Math.max(190, height / 2 + 34);
        int y = buttonBlockY + BUTTONS.length * BUTTON_HEIGHT + (BUTTONS.length - 1) * BUTTON_GAP + 26;
        for (int i = 0; i < count; i++) {
            int ix = x + i * (ICON_SIZE + ICON_GAP);
            if (contains(mouseX, mouseY, ix, y, ICON_SIZE, ICON_SIZE)) {
                return i;
            }
        }
        return -1;
    }

    private void drawGlass(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int fill, int border) {
        boolean blurred = MenuBackdropBlur.addBlurredRoundRect(g, x, y, w, h, 0);
        int bg = blurred ? fill : NexusRenderer.withAlpha(fill, Math.min(255, ((fill >>> 24) & 0xFF) + 28));
        g.nextStratum();
        NexusRenderer.fillRoundRect(g, x, y, w, h, 0, border);
        NexusRenderer.fillRoundRect(g, x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2), 0, bg);
    }

    private void drawGearIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 230);
        g.fill(cx - 1, cy - 7, cx + 1, cy - 4, c);
        g.fill(cx - 1, cy + 4, cx + 1, cy + 7, c);
        g.fill(cx - 7, cy - 1, cx - 4, cy + 1, c);
        g.fill(cx + 4, cy - 1, cx + 7, cy + 1, c);
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 5.0f, c);
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 2.1f, 0x99000000);
    }

    private void drawWardrobeIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 230);
        g.fill(cx - 5, cy - 7, cx + 5, cy - 5, c);
        g.fill(cx - 7, cy - 5, cx - 4, cy + 7, c);
        g.fill(cx + 4, cy - 5, cx + 7, cy + 7, c);
        g.fill(cx - 4, cy + 5, cx + 4, cy + 7, c);
        g.fill(cx - 1, cy - 4, cx + 1, cy + 5, c);
    }

    private void drawLoginIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 230);
        NexusRenderer.drawOutline(g, cx - 7, cy - 7, 13, 14, c);
        g.fill(cx - 2, cy - 1, cx + 6, cy + 1, c);
        g.fill(cx + 3, cy - 4, cx + 7, cy, c);
        g.fill(cx + 3, cy, cx + 7, cy + 4, c);
    }

    private void drawGlobeIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 230);
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 7.0f, c);
        NexusRenderer.fillCircle(g, cx + 0.5f, cy + 0.5f, 5.2f, 0x99000000);
        g.fill(cx - 7, cy, cx + 8, cy + 1, c);
        g.fill(cx, cy - 7, cx + 1, cy + 8, c);
    }

    private void drawQuitIcon(GuiGraphicsExtractor g, int cx, int cy, int color) {
        int c = NexusRenderer.withAlpha(color, 230);
        g.fill(cx - 5, cy - 5, cx - 3, cy - 3, c);
        g.fill(cx - 3, cy - 3, cx - 1, cy - 1, c);
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        g.fill(cx + 1, cy + 1, cx + 3, cy + 3, c);
        g.fill(cx + 3, cy + 3, cx + 5, cy + 5, c);
        g.fill(cx + 3, cy - 5, cx + 5, cy - 3, c);
        g.fill(cx + 1, cy - 3, cx + 3, cy - 1, c);
        g.fill(cx - 3, cy + 1, cx - 1, cy + 3, c);
        g.fill(cx - 5, cy + 3, cx - 3, cy + 5, c);
    }

    private void playClick(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    private boolean contains(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int withFade(int color, float alpha) {
        return NexusRenderer.withAlpha(color, Math.round(((color >>> 24) & 0xFF) * alpha));
    }

    private int mainButtonWidth() {
        return Math.max(190, Math.min(BUTTON_WIDTH, width - 64));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
