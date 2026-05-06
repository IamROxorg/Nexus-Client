package nexus.zigliix.com.client.runtime;

import net.minecraft.client.Minecraft;
import nexus.zigliix.com.client.NexusClientClient;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.notify.NotificationManager;

public final class ClientFeatureController {
    private ClientFeatureController() {}

    public static void tick(Minecraft client) {
        if (client == null) {
            return;
        }

        ModuleManager.tickModules();
        NotificationManager.tick();
        NexusClientClient.tickDiscordRpc(client);
    }
}
