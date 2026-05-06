package nexus.zigliix.com.client.discord;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import nexus.zigliix.com.NexusClient;

public final class DiscordRichPresenceManager {
    private static final DiscordRichPresenceManager INSTANCE = new DiscordRichPresenceManager();
    private static final long APPLICATION_ID = 1501355442989957242L;
    private static final long RECONNECT_DELAY_MS = 15_000L;

    private final OffsetDateTime sessionStart = OffsetDateTime.now();
    private final ExecutorService rpcExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Nexus Discord RPC");
        thread.setDaemon(true);
        return thread;
    });

    private volatile NativeDiscordRpcClient client;
    private volatile boolean started;
    private volatile boolean connecting;
    private volatile boolean connected;
    private volatile boolean discordMissingLogged;
    private volatile long nextConnectAttemptAt;
    private volatile String lastQueuedSignature = "";
    private volatile String lastSentSignature = "";
    private volatile PresenceSnapshot latestSnapshot;

    private DiscordRichPresenceManager() {}

    public static DiscordRichPresenceManager getInstance() {
        return INSTANCE;
    }

    public void start() {
        if (started) {
            return;
        }

        started = true;
        connecting = false;
        connected = false;
        nextConnectAttemptAt = 0L;
        lastQueuedSignature = "";
        lastSentSignature = "";
        scheduleConnectIfReady();
    }

    public void tick(Minecraft minecraft) {
        if (!started || minecraft == null) {
            return;
        }

        PresenceSnapshot snapshot = PresenceSnapshot.capture(minecraft);
        if (snapshot != null) {
            latestSnapshot = snapshot;
        }

        if (!isConnected(client)) {
            connected = false;
            scheduleConnectIfReady();
            return;
        }

        if (snapshot == null || snapshot.signature().equals(lastQueuedSignature)) {
            return;
        }

        lastQueuedSignature = snapshot.signature();
        queuePresenceUpdate(snapshot);
    }

    public void stop() {
        started = false;
        connecting = false;
        connected = false;
        lastQueuedSignature = "";
        lastSentSignature = "";
        latestSnapshot = null;

        executeRpcTask("stop", () -> {
            closeClient(client);
            client = null;
        });
    }

    private void scheduleConnectIfReady() {
        long now = System.currentTimeMillis();
        if (connecting || now < nextConnectAttemptAt) {
            return;
        }

        connecting = true;
        executeRpcTask("connect", this::connect);
    }

    private void connect() {
        if (!started) {
            connecting = false;
            return;
        }

        closeClient(client);
        client = null;

        try {
            NativeDiscordRpcClient rpcClient = NativeDiscordRpcClient.connect(APPLICATION_ID);
            client = rpcClient;
            connecting = false;
            connected = true;
            discordMissingLogged = false;
            lastQueuedSignature = "";
            lastSentSignature = "";
            NexusClient.LOGGER.info("Discord RPC connected.");

            PresenceSnapshot snapshot = latestSnapshot;
            if (snapshot != null) {
                sendPresence(rpcClient, snapshot);
            }
        } catch (IOException exception) {
            handleConnectFailure(exception);
        }
    }

    private void queuePresenceUpdate(PresenceSnapshot snapshot) {
        executeRpcTask("update presence", () -> {
            NativeDiscordRpcClient rpcClient = client;
            if (!started || !isConnected(rpcClient)) {
                markDisconnected();
                return;
            }

            sendPresence(rpcClient, snapshot);
        });
    }

    private void sendPresence(NativeDiscordRpcClient rpcClient, PresenceSnapshot snapshot) {
        if (snapshot.signature().equals(lastSentSignature)) {
            return;
        }

        try {
            rpcClient.sendActivity(buildActivity(snapshot));
            lastSentSignature = snapshot.signature();
        } catch (IOException exception) {
            NexusClient.LOGGER.warn("Discord RPC presence update failed; reconnecting later.", exception);
            markDisconnected();
            closeClient(rpcClient);
            if (client == rpcClient) {
                client = null;
            }
        }
    }

    private JsonObject buildActivity(PresenceSnapshot snapshot) {
        JsonObject activity = new JsonObject();
        activity.addProperty("type", 0);
        activity.addProperty("status_display_type", 2);
        activity.addProperty("details", snapshot.detailsLine());
        activity.addProperty("state", snapshot.stateLine());
        activity.addProperty("instance", false);

        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", sessionStart.toEpochSecond());
        activity.add("timestamps", timestamps);
        return activity;
    }

    private void handleConnectFailure(IOException exception) {
        closeClient(client);
        client = null;
        connecting = false;
        connected = false;
        lastQueuedSignature = "";
        lastSentSignature = "";
        nextConnectAttemptAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;

        if (!discordMissingLogged) {
            discordMissingLogged = true;
            NexusClient.LOGGER.info("Discord RPC is waiting for a running Discord client.");
        } else {
            NexusClient.LOGGER.debug("Discord RPC reconnect attempt failed.", exception);
        }
    }

    private void markDisconnected() {
        connecting = false;
        connected = false;
        lastQueuedSignature = "";
        lastSentSignature = "";
        nextConnectAttemptAt = System.currentTimeMillis() + RECONNECT_DELAY_MS;
    }

    private boolean isConnected(NativeDiscordRpcClient rpcClient) {
        return connected && rpcClient != null && rpcClient.isOpen();
    }

    private void closeClient(NativeDiscordRpcClient rpcClient) {
        if (rpcClient == null) {
            return;
        }

        try {
            if (rpcClient.isOpen()) {
                rpcClient.sendActivity(null);
            }
        } catch (IOException ignored) {
        }

        try {
            rpcClient.close();
        } catch (IOException ignored) {
        }
    }

    private void executeRpcTask(String action, Runnable runnable) {
        try {
            rpcExecutor.execute(() -> {
                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    markDisconnected();
                    NexusClient.LOGGER.warn("Discord RPC {} task failed.", action, throwable);
                }
            });
        } catch (RejectedExecutionException exception) {
            started = false;
            markDisconnected();
            NexusClient.LOGGER.warn("Discord RPC executor rejected {} task; disabling integration.", action, exception);
        }
    }

    private record PresenceSnapshot(String playerName, String detailsLine, String stateLine, String signature) {
        private static PresenceSnapshot capture(Minecraft client) {
            if (client.getGameProfile() == null) {
                return null;
            }

            String playerName = client.getGameProfile().name();
            if (playerName == null || playerName.isBlank()) {
                return null;
            }

            String location = resolveLocation(client);
            String details = switch (location) {
                case "Main Menu" -> "In the main menu";
                case "Singleplayer" -> "Playing singleplayer";
                default -> "Playing multiplayer";
            };
            String state = location + " | " + playerName;
            String signature = details + "|" + state;
            return new PresenceSnapshot(playerName, details, state, signature);
        }

        private static String resolveLocation(Minecraft client) {
            ServerData currentServer = client.getCurrentServer();
            if (currentServer != null && currentServer.ip != null && !currentServer.ip.isBlank()) {
                return "Multiplayer";
            }

            if (client.hasSingleplayerServer()) {
                return "Singleplayer";
            }

            return "Main Menu";
        }
    }

    private static final class NativeDiscordRpcClient implements Closeable {
        private static final int OP_HANDSHAKE = 0;
        private static final int OP_FRAME = 1;
        private static final int OP_CLOSE = 2;
        private static final int PIPE_COUNT = 10;

        private final RandomAccessFile pipe;
        private final long applicationId;
        private boolean open = true;

        private NativeDiscordRpcClient(RandomAccessFile pipe, long applicationId) {
            this.pipe = pipe;
            this.applicationId = applicationId;
        }

        private static NativeDiscordRpcClient connect(long applicationId) throws IOException {
            if (!isWindows()) {
                throw new IOException("Discord RPC without native dependencies is currently supported on Windows only");
            }

            IOException lastException = null;
            for (int i = 0; i < PIPE_COUNT; i++) {
                String path = "\\\\.\\pipe\\discord-ipc-" + i;
                try {
                    RandomAccessFile pipe = new RandomAccessFile(path, "rw");
                    NativeDiscordRpcClient client = new NativeDiscordRpcClient(pipe, applicationId);
                    client.handshake();
                    return client;
                } catch (IOException exception) {
                    lastException = exception;
                }
            }

            throw lastException != null ? lastException : new IOException("No Discord IPC pipe found");
        }

        private static boolean isWindows() {
            return System.getProperty("os.name", "").toLowerCase().contains("win");
        }

        private boolean isOpen() {
            return open;
        }

        private void handshake() throws IOException {
            JsonObject payload = new JsonObject();
            payload.addProperty("v", 1);
            payload.addProperty("client_id", Long.toString(applicationId));
            writePacket(OP_HANDSHAKE, payload.toString());
        }

        private void sendActivity(JsonObject activity) throws IOException {
            JsonObject args = new JsonObject();
            args.addProperty("pid", ProcessHandle.current().pid());
            if (activity == null) {
                args.add("activity", JsonNull.INSTANCE);
            } else {
                args.add("activity", activity);
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("cmd", "SET_ACTIVITY");
            payload.add("args", args);
            payload.addProperty("nonce", UUID.randomUUID().toString());
            writePacket(OP_FRAME, payload.toString());
        }

        private synchronized void writePacket(int opCode, String payload) throws IOException {
            if (!open) {
                throw new IOException("Discord IPC pipe is closed");
            }

            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            writeLittleEndianInt(opCode);
            writeLittleEndianInt(data.length);
            pipe.write(data);
        }

        private void writeLittleEndianInt(int value) throws IOException {
            pipe.write(value & 0xFF);
            pipe.write((value >>> 8) & 0xFF);
            pipe.write((value >>> 16) & 0xFF);
            pipe.write((value >>> 24) & 0xFF);
        }

        @Override
        public synchronized void close() throws IOException {
            if (!open) {
                return;
            }

            try {
                writePacket(OP_CLOSE, "{}");
            } catch (IOException ignored) {
            } finally {
                open = false;
                pipe.close();
            }
        }
    }
}
