package nexus.zigliix.com.client.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.ModuleManager;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ClickGuiScreen extends Screen {
    private static final int MIN_WIN_W = 560;
    private static final int MAX_WIN_W = 720;
    private static final int MIN_WIN_H = 350;
    private static final int MAX_WIN_H = 430;
    private static final int PAD = 18;
    private static final int HEADER_H = 58;
    private static final int FOOTER_H = 44;
    private static final int CARD_H = 42;
    private static final int GAP = 10;
    private static final int RADIUS = 12;

    private static final int BG_OVERLAY = 0x8A020508;
    private static final int SHELL_FILL = 0x6610171F;
    private static final int PANEL_FILL = 0x4419232D;
    private static final int ROW_FILL = 0x38202A34;
    private static final int ROW_HOVER = 0x58313D49;
    private static final int BORDER_FAINT = 0x28FFFFFF;
    private static final int BORDER_SOFT = 0x42FFFFFF;
    private static final int BORDER_ACTIVE = 0x72FFFFFF;

    private static final Identifier ASSET_MARK = Identifier.fromNamespaceAndPath("nexus-client", "textures/gui/nexus_mark.png");

    private int winX;
    private int winY;
    private int winW;
    private int winH;
    private double scrollOffset;
    private GameProfile cachedProfile;
    private Supplier<PlayerSkin> cachedSkinLookup;

    public ClickGuiScreen() {
        super(Component.literal("Nexus"));
    }

    @Override
    protected void init() {
        layoutWindow();
        clampScroll();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.blurBeforeThisStratum();
        g.fill(0, 0, width, height, BG_OVERLAY);
        g.text(font, "Nexus Client", 24, 30, NexusTheme.withAlpha(NexusTheme.textMuted(), 150), false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        layoutWindow();

        drawShell(g);
        drawHeader(g);
        drawModuleGrid(g, mouseX, mouseY);
        drawFooter(g);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void layoutWindow() {
        winW = Math.min(MAX_WIN_W, Math.max(MIN_WIN_W, width - 80));
        winH = Math.min(MAX_WIN_H, Math.max(MIN_WIN_H, height - 78));
        winX = (width - winW) / 2;
        winY = Math.max(34, (height - winH) / 2);
    }

    private void drawShell(GuiGraphicsExtractor g) {
        NexusRenderer.drawDropShadow(g, winX, winY, winW, winH, 3);
        drawSurface(g, winX, winY, winW, winH, RADIUS, SHELL_FILL, BORDER_SOFT);
        g.fill(winX + PAD, winY + HEADER_H, winX + winW - PAD, winY + HEADER_H + 1, BORDER_FAINT);
        g.fill(winX + PAD, winY + winH - FOOTER_H, winX + winW - PAD, winY + winH - FOOTER_H + 1, BORDER_FAINT);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        int logoSize = 24;
        int logoX = winX + PAD;
        int logoY = winY + 18;
        g.blit(RenderPipelines.GUI_TEXTURED, ASSET_MARK, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, 64, 64, 64, 64);
        g.text(font, "Nexus", logoX + 34, winY + 18, NexusTheme.text(), false);
    }

    private void drawModuleGrid(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        List<Module> modules = modules();
        int x = winX + PAD;
        int y = winY + HEADER_H + PAD;
        int w = winW - PAD * 2;
        int h = winH - HEADER_H - FOOTER_H - PAD * 2;
        int columns = columns(w);
        int cardW = (w - GAP * (columns - 1)) / columns;

        g.enableScissor(x, y, x + w, y + h);
        for (int i = 0; i < modules.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int cardX = x + col * (cardW + GAP);
            int cardY = y + row * (CARD_H + GAP) - (int) Math.round(scrollOffset);
            if (cardY > y + h || cardY + CARD_H < y) {
                continue;
            }
            drawModuleCard(g, modules.get(i), cardX, cardY, cardW, CARD_H, mouseX, mouseY);
        }
        g.disableScissor();
        drawScrollbar(g, x, y, w, h);
    }

    private void drawModuleCard(GuiGraphicsExtractor g, Module module, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, w, h);
        boolean enabled = module.isEnabled();
        int fill = enabled ? NexusTheme.withAlpha(NexusTheme.accent(), hovered ? 42 : 30) : hovered ? ROW_HOVER : ROW_FILL;
        int border = enabled ? NexusTheme.withAlpha(NexusTheme.accent(), hovered ? 145 : 110) : hovered ? BORDER_ACTIVE : BORDER_SOFT;

        drawSurface(g, x, y, w, h, 8, fill, border);
        g.text(font, trim(module.getName(), w - 34), x + 12, y + 16, enabled ? NexusTheme.text() : NexusTheme.textMuted(), false);
        NexusRenderer.fillRoundRect(g, x + w - 20, y + 17, 7, 7, 4, enabled ? NexusTheme.accent() : BORDER_ACTIVE);
    }

    private void drawFooter(GuiGraphicsExtractor g) {
        int x = winX + PAD;
        int y = winY + winH - FOOTER_H + 10;
        drawPlayerHead(g, x, y - 1, 22);
        g.text(font, trim(currentPlayerName(), 150), x + 32, y, NexusTheme.text(), false);
    }

    private void drawScrollbar(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        int contentH = contentHeight(w);
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) {
            return false;
        }

        Module clicked = moduleAt(event.x(), event.y());
        if (clicked != null) {
            clicked.toggle();
            return true;
        }
        return super.mouseClicked(event, someBoolean);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = winX + PAD;
        int y = winY + HEADER_H + PAD;
        int w = winW - PAD * 2;
        int h = winH - HEADER_H - FOOTER_H - PAD * 2;
        if (isHovered(mouseX, mouseY, x, y, w, h)) {
            scrollOffset = clamp(scrollOffset - scrollY * 26.0, 0.0, maxScroll(w, h));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
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

    private Module moduleAt(double mouseX, double mouseY) {
        int x = winX + PAD;
        int y = winY + HEADER_H + PAD;
        int w = winW - PAD * 2;
        int h = winH - HEADER_H - FOOTER_H - PAD * 2;
        if (!isHovered(mouseX, mouseY, x, y, w, h)) {
            return null;
        }

        int columns = columns(w);
        int cardW = (w - GAP * (columns - 1)) / columns;
        List<Module> modules = modules();
        for (int i = 0; i < modules.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int cardX = x + col * (cardW + GAP);
            int cardY = y + row * (CARD_H + GAP) - (int) Math.round(scrollOffset);
            if (isHovered(mouseX, mouseY, cardX, cardY, cardW, CARD_H)) {
                return modules.get(i);
            }
        }
        return null;
    }

    private List<Module> modules() {
        return ModuleManager.getAll().stream()
            .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private int columns(int width) {
        return width >= 620 ? 4 : 3;
    }

    private int contentHeight(int width) {
        int rows = (int) Math.ceil(modules().size() / (double) columns(width));
        return Math.max(0, rows * CARD_H + Math.max(0, rows - 1) * GAP);
    }

    private double maxScroll(int width, int height) {
        return Math.max(0.0, contentHeight(width) - height);
    }

    private void clampScroll() {
        int w = winW - PAD * 2;
        int h = winH - HEADER_H - FOOTER_H - PAD * 2;
        scrollOffset = clamp(scrollOffset, 0.0, maxScroll(w, h));
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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

    private void drawSurface(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int fill, int border) {
        if (w <= 0 || h <= 0) {
            return;
        }
        NexusRenderer.fillRoundRect(g, x, y, w, h, radius, border);
        if (w > 2 && h > 2) {
            NexusRenderer.fillRoundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), fill);
        }
    }
}
