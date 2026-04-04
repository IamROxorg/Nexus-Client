package nexus.zigliix.com.client.module;

import nexus.zigliix.com.client.module.modules.combat.AutoRespawn;
import nexus.zigliix.com.client.module.modules.combat.AutoSoup;
import nexus.zigliix.com.client.module.modules.combat.HitCircles;
import nexus.zigliix.com.client.module.modules.misc.AntiAfk;
import nexus.zigliix.com.client.module.modules.misc.Zoom;
import nexus.zigliix.com.client.module.modules.movement.NoFall;
import nexus.zigliix.com.client.module.modules.movement.Speed;
import nexus.zigliix.com.client.module.modules.movement.Sprint;
import nexus.zigliix.com.client.module.modules.visual.ESP;
import nexus.zigliix.com.client.module.modules.visual.FullBright;
import nexus.zigliix.com.client.module.modules.visual.Tracers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private ModuleManager() {}

    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        register(new AutoRespawn());
        register(new AutoSoup());
        register(new HitCircles());
        register(new FullBright());
        register(new ESP());
        register(new Tracers());
        register(new Sprint());
        register(new Speed());
        register(new NoFall());
        register(new Zoom());
        register(new AntiAfk());
    }

    private static void register(Module m) { MODULES.add(m); }

    public static List<Module> getAll() { return Collections.unmodifiableList(MODULES); }

    public static List<Module> getByCategory(Category category) {
        return MODULES.stream().filter(m -> m.getCategory() == category).toList();
    }

    public static List<Module> getEnabled() {
        return MODULES.stream()
                .filter(Module::isEnabled)
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }
}
