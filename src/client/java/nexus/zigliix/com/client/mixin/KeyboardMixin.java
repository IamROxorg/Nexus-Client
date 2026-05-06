package nexus.zigliix.com.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import nexus.zigliix.com.client.gui.ClickGuiScreen;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.util.KeybindUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public class KeyboardMixin {
    private static long lastToggleTime;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.key() != KeybindUtil.GUI_TOGGLE_KEY) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                ModuleManager.handleKeyPress(event.key());
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastToggleTime < 150L) {
            ci.cancel();
            return;
        }
        lastToggleTime = now;

        Minecraft mc = Minecraft.getInstance();
        Screen current = mc.screen;

        if (current instanceof ClickGuiScreen) {
            mc.setScreen(null);
            ci.cancel();
            return;
        }

        if (current == null) {
            mc.setScreen(new ClickGuiScreen());
            ci.cancel();
        }
    }
}
