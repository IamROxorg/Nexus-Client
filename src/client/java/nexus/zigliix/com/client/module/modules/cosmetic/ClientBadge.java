package nexus.zigliix.com.client.module.modules.cosmetic;

import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.ModeSetting;

public final class ClientBadge extends Module {
    private final ModeSetting position = addSetting(new ModeSetting(
        "Position",
        "Where the Nexus badge is rendered",
        "Top Right",
        "Top Left",
        "Top Right",
        "Bottom Left",
        "Bottom Right"
    ));

    public ClientBadge() {
        super("Client Badge", "Shows a compact Nexus cosmetic badge on your HUD", Category.COSMETIC);
    }

    public String getPosition() {
        return position.getValue();
    }
}
