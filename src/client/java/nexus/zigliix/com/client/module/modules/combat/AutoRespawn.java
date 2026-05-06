package nexus.zigliix.com.client.module.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("Auto Respawn", "Automatically respawns on death screen", Category.MISC);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.screen instanceof DeathScreen) {
            client.player.respawn();
            client.setScreen(null);
        }
    }
}
