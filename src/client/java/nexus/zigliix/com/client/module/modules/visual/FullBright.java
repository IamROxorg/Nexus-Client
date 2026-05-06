package nexus.zigliix.com.client.module.modules.visual;

import net.minecraft.client.Minecraft;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;

public class FullBright extends Module {
    private static final double FULL_BRIGHT_GAMMA = 16.0D;

    private Double previousGamma;
    private Double appliedGamma;

    public FullBright() {
        super("Full Bright", "Sets gamma to maximum brightness", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousGamma = client.options.gamma().get();
        client.options.gamma().set(FULL_BRIGHT_GAMMA);
    }

    @Override
    public void onDisable() {
        if (previousGamma != null) {
            Minecraft.getInstance().options.gamma().set(previousGamma);
            previousGamma = null;
        }

        appliedGamma = null;
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        double currentGamma = client.options.gamma().get();
        if (appliedGamma != null && Double.compare(currentGamma, appliedGamma) != 0) {
            previousGamma = currentGamma;
        }

        client.options.gamma().set(FULL_BRIGHT_GAMMA);
        appliedGamma = FULL_BRIGHT_GAMMA;
    }
}
