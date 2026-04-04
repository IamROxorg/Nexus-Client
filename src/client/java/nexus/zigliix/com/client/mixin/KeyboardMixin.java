package nexus.zigliix.com.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;
import nexus.zigliix.com.client.gui.ClickGuiScreen;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS && event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new ClickGuiScreen());
                ci.cancel();
            }
        }
    }
}
