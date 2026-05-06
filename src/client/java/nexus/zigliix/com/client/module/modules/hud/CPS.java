package nexus.zigliix.com.client.module.modules.hud;

import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;

import java.util.concurrent.ConcurrentLinkedDeque;

public class CPS extends Module {
    private static final ConcurrentLinkedDeque<Long> clicks = new ConcurrentLinkedDeque<>();
    private static final long WINDOW_MS = 1000L;
    
    public CPS() {
        super("CPS", "Displays clicks per second", Category.HUD);
    }
    
    public static void registerClick() {
        long now = System.currentTimeMillis();
        prune(now);
        clicks.addLast(now);
    }
    
    public static int getCPS() {
        long now = System.currentTimeMillis();
        prune(now);
        return clicks.size();
    }

    private static void prune(long now) {
        Long oldest;
        while ((oldest = clicks.peekFirst()) != null && now - oldest > WINDOW_MS) {
            clicks.pollFirst();
        }
    }
}
