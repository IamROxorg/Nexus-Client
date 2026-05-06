package nexus.zigliix.com.client.module.modules.cosmetic;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.BooleanSetting;
import nexus.zigliix.com.client.module.setting.ModeSetting;

import java.util.UUID;

public final class NexusCape extends Module {
    private static final Identifier NEXUS_CAPE = Identifier.fromNamespaceAndPath("nexus-client", "textures/cosmetics/nexus_cape.png");
    private static final Identifier CRYSTAL_CAPE = Identifier.fromNamespaceAndPath("nexus-client", "textures/cosmetics/crystal_cape.png");

    private final ModeSetting style = addSetting(new ModeSetting(
        "Style",
        "Cape texture style",
        "Nexus",
        "Nexus",
        "Crystal"
    ));
    private final BooleanSetting showOnOthers = addSetting(new BooleanSetting(
        "Show On Others",
        "Also applies the cape to other players on your client",
        false
    ));

    public NexusCape() {
        super("Nexus Cape", "Adds a client-side Nexus cape cosmetic", Category.COSMETIC);
    }

    public Identifier getCapeTexture() {
        return "Crystal".equalsIgnoreCase(style.getValue()) ? CRYSTAL_CAPE : NEXUS_CAPE;
    }

    public boolean appliesTo(GameProfile profile) {
        if (showOnOthers.getValue()) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        GameProfile localProfile = client != null ? client.getGameProfile() : null;
        UUID localId = localProfile != null ? localProfile.id() : null;
        UUID targetId = profile != null ? profile.id() : null;
        return localId != null && localId.equals(targetId);
    }
}
