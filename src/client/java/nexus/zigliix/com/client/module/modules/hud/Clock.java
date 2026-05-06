package nexus.zigliix.com.client.module.modules.hud;

import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.ModeSetting;

public class Clock extends Module {
    private final ModeSetting formatSetting = addSetting(new ModeSetting("Format", "Display time in 24H or 12H", "24H", "24H", "12H"));

    public Clock() {
        super("Clock", "Displays the current time", Category.HUD);
    }

    public boolean usesTwelveHourClock() {
        return formatSetting.getValue().equalsIgnoreCase("12H");
    }
}
