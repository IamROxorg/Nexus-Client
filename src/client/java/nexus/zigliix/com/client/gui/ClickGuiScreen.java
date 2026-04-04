package nexus.zigliix.com.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.ModuleManager;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClickGuiScreen extends Screen {

    private Category currentCategory = Category.COMBAT;
    
    // GUI Dimensions
    private final int guiWidth = 600;
    private final int guiHeight = 360;

    // Layout margins
    private final int sideBarWidth = 140;
    
    // Smooth animation 
    private float animationProgress = 0f;

    public ClickGuiScreen() {
        super(Component.literal("Nexus Mods"));
    }

    @Override
    protected void init() {
        animationProgress = 0f;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Animation
        animationProgress = Math.min(1f, animationProgress + 0.08f);
        float ease = 1f - (float) Math.pow(1f - animationProgress, 3);
        
        // Dark Overlay background
        int overlayAlpha = (int)(0x88 * ease);
        g.fill(0, 0, width, height, overlayAlpha << 24);

        int startX = (width - guiWidth) / 2;
        int startY = (height - guiHeight) / 2;
        
        // Animate pop-in
        g.pose().pushMatrix();
        float scale = 0.95f + (0.05f * ease);
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(scale, scale);
        g.pose().translate(-width / 2f, -height / 2f);

        // Main Window Background (Glassmorphism feel)
        NexusRenderer.fillRoundRect(g, startX, startY, guiWidth, guiHeight, 10, NexusTheme.PANEL);
        NexusRenderer.drawCenteredString(g, getFont(), "NEXUS MODS", startX + sideBarWidth / 2, startY + 20, NexusTheme.TEXT);

        // Sidebar Categories
        int catY = startY + 50;
        int catIndex = 0;
        for (Category cat : Category.values()) {
            int cx = startX + 10;
            int cy = catY + (catIndex * 35);
            int cw = sideBarWidth - 20;
            int ch = 30;
            
            boolean isHovered = mouseX >= cx && mouseX <= cx + cw && mouseY >= cy && mouseY <= cy + ch;
            boolean isSelected = (currentCategory == cat);
            
            int color = isSelected ? NexusTheme.ACCENT : (isHovered ? NexusTheme.PANEL_ALT : 0);
            
            if (color != 0) {
                NexusRenderer.fillRoundRect(g, cx, cy, cw, ch, 6, color);
            }
            
            int textColor = isSelected ? 0xFFFFFFFF : NexusTheme.TEXT_MUTED;
            NexusRenderer.drawString(g, getFont(), cat.getDisplayName(), cx + 15, cy + 11, textColor);
            
            catIndex++;
        }

        // Separator line
        g.fill(startX + sideBarWidth, startY + 15, startX + sideBarWidth + 1, startY + guiHeight - 15, NexusTheme.SEPARATOR);

        // Grid Area for Modules
        int gridX = startX + sideBarWidth + 20;
        int gridY = startY + 20;
        int moduleW = 200;
        int moduleH = 45;
        
        List<Module> modules = ModuleManager.getByCategory(currentCategory);
        int col = 0;
        int row = 0;

        for (Module mod : modules) {
            int mx = gridX + col * (moduleW + 15);
            int my = gridY + row * (moduleH + 15);
            
            boolean mHovered = mouseX >= mx && mouseX <= mx + moduleW && mouseY >= my && mouseY <= my + moduleH;
            
            // Tile background
            int tileBg = mHovered ? 0x22FFFFFF : NexusTheme.PANEL_ALT;
            NexusRenderer.fillRoundRect(g, mx, my, moduleW, moduleH, 6, tileBg);
            
            // Name
            NexusRenderer.drawString(g, getFont(), mod.getName(), mx + 15, my + 13, NexusTheme.TEXT);
            NexusRenderer.drawString(g, getFont(), mod.isEnabled() ? "En ligne" : "Hors ligne", mx + 15, my + 25, mod.isEnabled() ? NexusTheme.ACCENT : NexusTheme.TEXT_MUTED);

            // Toggle Switch Visual
            int switchW = 30;
            int switchH = 16;
            int switchX = mx + moduleW - switchW - 15;
            int switchY = my + (moduleH - switchH) / 2;
            
            int switchColor = mod.isEnabled() ? NexusTheme.ACCENT : NexusTheme.SEPARATOR;
            NexusRenderer.fillRoundRect(g, switchX, switchY, switchW, switchH, switchH / 2, switchColor);
            
            // Switch knob
            int knobOffset = mod.isEnabled() ? switchW - switchH : 0;
            g.fill(switchX + knobOffset + 2, switchY + 2, switchX + knobOffset + switchH - 2, switchY + switchH - 2, 0xFFFFFFFF);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        g.pose().popMatrix();

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean someBoolean) {
        if (event.button() != 0) return false;
        
        double mx = event.x();
        double my = event.y();
        
        int startX = (width - guiWidth) / 2;
        int startY = (height - guiHeight) / 2;
        
        // Sidebar click
        int catY = startY + 50;
        int catIndex = 0;
        for (Category cat : Category.values()) {
            int cx = startX + 10;
            int cy = catY + (catIndex * 35);
            int cw = sideBarWidth - 20;
            int ch = 30;
            
            if (mx >= cx && mx <= cx + cw && my >= cy && my <= cy + ch) {
                currentCategory = cat;
                // Play click sound
                net.minecraft.client.resources.sounds.SimpleSoundInstance sound = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F);
                Minecraft.getInstance().getSoundManager().play(sound);
                return true;
            }
            catIndex++;
        }

        // Module Toggle Click
        int gridX = startX + sideBarWidth + 20;
        int gridY = startY + 20;
        int moduleW = 200;
        int moduleH = 45;
        
        List<Module> modules = ModuleManager.getByCategory(currentCategory);
        int col = 0;
        int row = 0;

        for (Module mod : modules) {
            int modX = gridX + col * (moduleW + 15);
            int modY = gridY + row * (moduleH + 15);
            
            if (mx >= modX && mx <= modX + moduleW && my >= modY && my <= modY + moduleH) {
                mod.toggle();
                net.minecraft.client.resources.sounds.SimpleSoundInstance sound = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, mod.isEnabled() ? 1.2F : 0.8F);
                Minecraft.getInstance().getSoundManager().play(sound);
                return true;
            }
            
            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
        
        return super.mouseClicked(event, someBoolean);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
