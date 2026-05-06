package nexus.zigliix.com.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import nexus.zigliix.com.NexusClient;
import nexus.zigliix.com.client.command.NexusCommandManager;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.discord.DiscordRichPresenceManager;
import nexus.zigliix.com.client.hud.HudRenderer;
import nexus.zigliix.com.client.runtime.ClientFeatureController;
import nexus.zigliix.com.client.module.ModuleManager;

public class NexusClientClient implements ClientModInitializer {
	private static final Identifier HUD_RENDERER_ID = Identifier.fromNamespaceAndPath("nexus-client", "hud_renderer");
	private static boolean discordAvailable = true;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();
		NexusConfigManager.load();
		NexusCommandManager.register();
		startDiscordRpc();
		ClientTickEvents.END_CLIENT_TICK.register(ClientFeatureController::tick);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> stopDiscordRpc());
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, HUD_RENDERER_ID, (graphics, deltaTracker) -> {
			Minecraft client = Minecraft.getInstance();
			HudRenderer.renderAll(graphics, client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		});
	}

	public static void tickDiscordRpc(Minecraft client) {
		if (!discordAvailable) {
			return;
		}

		try {
			DiscordRichPresenceManager.getInstance().tick(client);
		} catch (Throwable throwable) {
			disableDiscordRpc("Discord RPC tick failed; disabling integration.", throwable);
		}
	}

	private static void startDiscordRpc() {
		if (!discordAvailable) {
			return;
		}

		try {
			DiscordRichPresenceManager.getInstance().start();
		} catch (Throwable throwable) {
			disableDiscordRpc("Discord RPC unavailable; continuing without Discord integration.", throwable);
		}
	}

	private static void stopDiscordRpc() {
		if (!discordAvailable) {
			return;
		}

		try {
			DiscordRichPresenceManager.getInstance().stop();
		} catch (Throwable throwable) {
			disableDiscordRpc("Discord RPC shutdown failed.", throwable);
		}
	}

	private static void disableDiscordRpc(String message, Throwable throwable) {
		discordAvailable = false;
		NexusClient.LOGGER.warn(message, throwable);
	}
}
