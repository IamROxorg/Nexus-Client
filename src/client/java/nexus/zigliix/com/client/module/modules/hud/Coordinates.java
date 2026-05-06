package nexus.zigliix.com.client.module.modules.hud;

import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.BooleanSetting;

public class Coordinates extends Module {
    private final BooleanSetting showYSetting = addSetting(new BooleanSetting("ShowY", "Display the Y coordinate", true));

    public Coordinates() {
        super("Coordinates", "Displays your current position (X, Y, Z)", Category.HUD);
    }

    public boolean shouldShowY() {
        return showYSetting.getValue();
    }
}
