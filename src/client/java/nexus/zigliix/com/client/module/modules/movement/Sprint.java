package nexus.zigliix.com.client.module.modules.movement;

import net.minecraft.client.Minecraft;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Auto-sprints while moving and fed", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        boolean shouldSprint = client.player.input.hasForwardImpulse()
            && !client.player.horizontalCollision
            && !client.player.isShiftKeyDown()
            && !client.player.isUsingItem()
            && client.player.getFoodData().getFoodLevel() > 6;

        if (shouldSprint) {
            client.player.setSprinting(true);
        }
    }
}
