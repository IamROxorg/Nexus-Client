package nexus.zigliix.com.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import nexus.zigliix.com.client.gui.MainMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void redirectToCustomMenu(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof MainMenuScreen)) {
            mc.setScreen(new MainMenuScreen());
            ci.cancel();
        }
    }
}
