package nexus.zigliix.com.client.module.modules.cosmetic;

import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.BooleanSetting;
import nexus.zigliix.com.client.module.setting.ModeSetting;
import nexus.zigliix.com.client.module.setting.NumberSetting;

public final class CustomCrosshair extends Module {
    private final ModeSetting style = addSetting(new ModeSetting(
        "Style",
        "Crosshair shape",
        "Plus",
        "Plus",
        "Dot",
        "Box",
        "X"
    ));
    private final ModeSetting color = addSetting(new ModeSetting(
        "Color",
        "Crosshair tint",
        "Accent",
        "Accent",
        "White",
        "Red",
        "Mint"
    ));
    private final NumberSetting size = addSetting(new NumberSetting(
        "Size",
        "Crosshair arm length",
        7.0D,
        3.0D,
        14.0D,
        1.0D
    ));
    private final NumberSetting gap = addSetting(new NumberSetting(
        "Gap",
        "Space around the center",
        3.0D,
        0.0D,
        8.0D,
        1.0D
    ));
    private final NumberSetting thickness = addSetting(new NumberSetting(
        "Thickness",
        "Crosshair line thickness",
        1.0D,
        1.0D,
        4.0D,
        1.0D
    ));
    private final BooleanSetting outline = addSetting(new BooleanSetting(
        "Outline",
        "Draw a dark contrast outline",
        true
    ));

    public CustomCrosshair() {
        super("Custom Crosshair", "Renders a clean Nexus crosshair over the vanilla HUD", Category.COSMETIC);
    }

    public String getStyle() {
        return style.getValue();
    }

    public String getColorMode() {
        return color.getValue();
    }

    public int getSize() {
        return size.getValue().intValue();
    }

    public int getGap() {
        return gap.getValue().intValue();
    }

    public int getThickness() {
        return thickness.getValue().intValue();
    }

    public boolean hasOutline() {
        return outline.getValue();
    }
}
