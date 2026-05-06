package nexus.zigliix.com.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.modules.hud.CPS;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS
            && button.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
            && Minecraft.getInstance().screen == null
            && ModuleManager.isModuleEnabled(CPS.class)) {
            CPS.registerClick();
        }
    }
}
