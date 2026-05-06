package nexus.zigliix.com.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.gui.component.NexusUiState;
import nexus.zigliix.com.client.hud.HudRenderer;
import nexus.zigliix.com.client.notify.NotificationManager;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends Screen {
    private final Screen parent;
    private String draggingId;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.blurBeforeThisStratum();
        g.fill(0, 0, width, height, NexusTheme.withAlpha(0xFF050810, 132));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        drawEditorGrid(g);
        HudRenderer.renderEditor(g, width, height, mouseX, mouseY);

        int w = Math.min(220, width - 28);
        int h = 46;
        int x = (width - w) / 2;
        int y = Math.max(14, height - h - 14);
        NexusRenderer.fillRoundRect(g, x, y, w, h, 8, NexusTheme.withAlpha(NexusTheme.panel(), 235));
        NexusRenderer.drawOutline(g, x, y, w, h, NexusTheme.withAlpha(NexusTheme.separator(), 95));
        g.text(font, "HUD Editor", x + 10, y + 10, NexusTheme.text(), true);
        g.text(font, trim("Drag widgets. R resets. ESC returns.", w - 20), x + 10, y + 25, NexusTheme.textDim(), false);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void drawEditorGrid(GuiGraphicsExtractor g) {
        int gridColor = NexusTheme.withAlpha(NexusTheme.separator(), 38);
        for (int x = 8; x < width; x += 24) {
            g.fill(x, 0, x + 1, height, gridColor);
        }
        for (int y = 8; y < height; y += 24) {
            g.fill(0, y, width, y + 1, gridColor);
        }
        g.fill(width / 2, 0, width / 2 + 1, height, NexusTheme.withAlpha(NexusTheme.accent(), 40));
        g.fill(0, height / 2, width, height / 2 + 1, NexusTheme.withAlpha(NexusTheme.accent(), 40));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) {
            return false;
        }
        for (HudRenderer.HudBounds bounds : HudRenderer.getActiveBounds(width, height)) {
            if (contains(event.x(), event.y(), bounds.x(), bounds.y(), bounds.width(), bounds.height())) {
                draggingId = bounds.id();
                dragOffsetX = (int) event.x() - bounds.x();
                dragOffsetY = (int) event.y() - bounds.y();
                return true;
            }
        }
        return super.mouseClicked(event, someBoolean);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingId != null) {
            HudRenderer.HudBounds bounds = findBounds(draggingId);
            int widgetWidth = bounds == null ? 1 : bounds.width();
            int widgetHeight = bounds == null ? 1 : bounds.height();
            int x = clamp((int) event.x() - dragOffsetX, 2, Math.max(2, width - widgetWidth - 2));
            int y = clamp((int) event.y() - dragOffsetY, 2, Math.max(2, height - widgetHeight - 2));
            NexusUiState.setHudPosition(draggingId, x, y, false);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingId != null) {
            draggingId = null;
            NexusConfigManager.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_R) {
            NexusUiState.resetHudPositions();
            NotificationManager.info("HUD layout reset");
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean contains(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private HudRenderer.HudBounds findBounds(String id) {
        for (HudRenderer.HudBounds bounds : HudRenderer.getActiveBounds(width, height)) {
            if (bounds.id().equals(id)) {
                return bounds;
            }
        }
        return null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String trim(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String value = text;
        while (value.length() > 3 && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }
}
