package nexus.zigliix.com.client.module.modules.misc;

import net.minecraft.client.Minecraft;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.setting.BooleanSetting;
import nexus.zigliix.com.client.module.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    private final NumberSetting fovSetting = addSetting(new NumberSetting("FOV", "Field of view while zooming", 30.0, 10.0, 70.0, 1.0));
    private final BooleanSetting smoothCameraSetting = addSetting(new BooleanSetting("SmoothCamera", "Enable smooth camera while zooming", true));

    private Integer previousFov;
    private Boolean previousSmoothCamera;
    private Integer appliedFov;
    private Boolean appliedSmoothCamera;

    public Zoom() {
        super("Zoom", "Zooms the camera like a spyglass", Category.MISC);
        setKeybind(GLFW.GLFW_KEY_C, true);
    }

    public int getZoomFov() {
        return (int) Math.round(fovSetting.getValue());
    }

    public boolean shouldUseSmoothCamera() {
        return smoothCameraSetting.getValue();
    }

    @Override
    public void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousFov = client.options.fov().get();
        previousSmoothCamera = client.options.smoothCamera;
        applyZoom(client);
    }

    @Override
    public void onDisable() {
        Minecraft client = Minecraft.getInstance();
        if (previousFov != null) {
            client.options.fov().set(previousFov);
            previousFov = null;
        }
        if (previousSmoothCamera != null) {
            client.options.smoothCamera = previousSmoothCamera;
            previousSmoothCamera = null;
        }

        appliedFov = null;
        appliedSmoothCamera = null;
    }

    @Override
    public void onTick() {
        applyZoom(Minecraft.getInstance());
    }

    private void applyZoom(Minecraft client) {
        int targetFov = getZoomFov();
        int currentFov = client.options.fov().get();
        if (appliedFov != null && currentFov != appliedFov) {
            previousFov = currentFov;
        }
        client.options.fov().set(targetFov);
        appliedFov = targetFov;

        boolean targetSmoothCamera = shouldUseSmoothCamera();
        boolean currentSmoothCamera = client.options.smoothCamera;
        if (appliedSmoothCamera != null && currentSmoothCamera != appliedSmoothCamera) {
            previousSmoothCamera = currentSmoothCamera;
        }
        client.options.smoothCamera = targetSmoothCamera;
        appliedSmoothCamera = targetSmoothCamera;
    }
}
