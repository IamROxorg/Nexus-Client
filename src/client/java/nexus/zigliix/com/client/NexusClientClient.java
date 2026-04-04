package nexus.zigliix.com.client;

import net.fabricmc.api.ClientModInitializer;

public class NexusClientClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		nexus.zigliix.com.client.module.ModuleManager.init();
	}
}