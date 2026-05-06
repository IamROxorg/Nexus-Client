package nexus.zigliix.com.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.modules.cosmetic.NexusCape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void nexus$applyClientCape(CallbackInfoReturnable<PlayerSkin> cir) {
        NexusCape cape = ModuleManager.getModule(NexusCape.class);
        GameProfile profile = ((AbstractClientPlayer) (Object) this).getGameProfile();
        if (cape == null || !cape.isEnabled() || !cape.appliesTo(profile)) {
            return;
        }

        ClientAsset.ResourceTexture texture = new ClientAsset.ResourceTexture(cape.getCapeTexture());
        PlayerSkin.Patch patch = PlayerSkin.Patch.create(Optional.empty(), Optional.of(texture), Optional.of(texture), Optional.empty());
        cir.setReturnValue(cir.getReturnValue().with(patch));
    }
}
