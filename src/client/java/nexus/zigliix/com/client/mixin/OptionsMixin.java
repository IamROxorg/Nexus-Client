package nexus.zigliix.com.client.mixin;

import net.minecraft.client.Minecraft;
import nexus.zigliix.com.client.gui.ClickGuiScreen;
import nexus.zigliix.com.client.gui.HudEditorScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.Options.class)
public class OptionsMixin {
    private static final int NEXUS_BLUR_RADIUS = 10;

    @Inject(method = "getMenuBackgroundBlurriness", at = @At("HEAD"), cancellable = true)
    private void nexus$forceBlurForNexusScreens(CallbackInfoReturnable<Integer> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.screen == null) {
            return;
        }

        if (minecraft.screen instanceof ClickGuiScreen
            || minecraft.screen instanceof HudEditorScreen) {
            cir.setReturnValue(NEXUS_BLUR_RADIUS);
        }
    }
}
