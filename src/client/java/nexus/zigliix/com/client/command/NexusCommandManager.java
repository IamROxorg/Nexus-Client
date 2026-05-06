package nexus.zigliix.com.client.command;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.module.Category;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.setting.Setting;
import nexus.zigliix.com.client.notify.NotificationManager;
import nexus.zigliix.com.client.util.KeybindUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class NexusCommandManager {
    private NexusCommandManager() {}

    private record ResolvedArgument<T>(T value, int nextIndex) {}

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(NexusCommandManager::handleChatMessage);
    }

    private static boolean handleChatMessage(String message) {
        String trimmed = message.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (!normalized.equals(".nexus") && !normalized.startsWith(".nexus ")) {
            return true;
        }

        execute(trimmed);
        return false;
    }

    private static void execute(String input) {
        List<String> args = tokenize(input);
        if (args.size() == 1) {
            showHelp();
            return;
        }

        String subcommand = args.get(1).toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "toggle" -> toggleModule(args);
            case "bind" -> bindModule(args);
            case "set" -> setSetting(args);
            case "reset" -> resetModule(args);
            case "list" -> listModules();
            case "status" -> showStatus();
            case "sound" -> configureSound(args);
            case "chat" -> configureChat(args);
            case "config" -> configurePersistence(args);
            case "help" -> showHelp();
            default -> NotificationManager.error("Unknown command: " + subcommand);
        }
    }

    private static void toggleModule(List<String> args) {
        ResolvedArgument<Module> resolved = resolveModule(args, 2, args.size());
        if (resolved == null || resolved.nextIndex() != args.size()) {
            NotificationManager.error("Usage: .nexus toggle <module>");
            return;
        }

        resolved.value().toggle();
    }

    private static void bindModule(List<String> args) {
        if (args.size() < 4) {
            NotificationManager.error("Usage: .nexus bind <module> <key>");
            return;
        }

        String keyName = args.get(args.size() - 1);
        ResolvedArgument<Module> resolved = resolveModule(args, 2, args.size() - 1);
        if (resolved == null || resolved.nextIndex() != args.size() - 1) {
            NotificationManager.error("Usage: .nexus bind <module> <key>");
            return;
        }

        Module module = resolved.value();
        int keyCode = KeybindUtil.parseKey(keyName);
        if (keyCode == KeybindUtil.NONE && !KeybindUtil.normalizeName(keyName).equals("none") && !KeybindUtil.normalizeName(keyName).equals("unbind")) {
            NotificationManager.error("Unknown key: " + keyName);
            return;
        }

        if (KeybindUtil.isReserved(keyCode)) {
            NotificationManager.error("That key is reserved for the GUI");
            return;
        }

        module.setKeybind(keyCode);
        NotificationManager.info("Bound " + module.getName() + " to " + module.getKeybindName());
    }

    private static void setSetting(List<String> args) {
        if (args.size() < 5) {
            NotificationManager.error("Usage: .nexus set <module> <setting> <value>");
            return;
        }

        ResolvedArgument<Module> moduleArgument = resolveModule(args, 2, args.size() - 2);
        if (moduleArgument == null) {
            NotificationManager.error("Unknown module");
            return;
        }

        ResolvedArgument<Setting<?>> settingArgument = resolveSetting(moduleArgument.value(), args, moduleArgument.nextIndex(), args.size() - 1);
        if (settingArgument == null) {
            NotificationManager.error("Unknown setting on " + moduleArgument.value().getName());
            return;
        }

        Module module = moduleArgument.value();
        Setting<?> setting = settingArgument.value();
        String value = join(args, settingArgument.nextIndex(), args.size());
        if (value.isBlank()) {
            NotificationManager.error("Usage: .nexus set <module> <setting> <value>");
            return;
        }

        if (!setting.applyCommandValue(value)) {
            NotificationManager.error("Invalid value for " + setting.getName());
            return;
        }

        NotificationManager.info(module.getName() + " " + setting.getName() + " -> " + setting.getDisplayValue());
    }

    private static void resetModule(List<String> args) {
        ResolvedArgument<Module> resolved = resolveModule(args, 2, args.size());
        if (resolved == null || resolved.nextIndex() != args.size()) {
            NotificationManager.error("Usage: .nexus reset <module>");
            return;
        }

        Module module = resolved.value();
        module.resetToDefaults(false);
        NexusConfigManager.save();
        NotificationManager.info("Reset " + module.getName() + " to defaults");
    }

    private static void listModules() {
        NotificationManager.info("Nexus module list sent to chat", true);
        for (Category category : Category.values()) {
            String modules = ModuleManager.getByCategory(category).stream()
                .map(module -> module.isEnabled() ? module.getName() + " [on]" : module.getName())
                .collect(Collectors.joining(", "));
            if (!modules.isBlank()) {
                NotificationManager.chat(category.getDisplayName() + ": " + modules);
            }
        }
    }

    private static void showStatus() {
        long enabledModules = ModuleManager.getAll().stream().filter(Module::isEnabled).count();
        NotificationManager.info("Nexus status sent to chat", true);
        NotificationManager.chat("Enabled modules: " + enabledModules + "/" + ModuleManager.getAll().size());
        NotificationManager.chat("Toggle sounds: " + (NexusConfigManager.isToggleSoundsEnabled() ? "on" : "off"));
        NotificationManager.chat("Chat feedback: " + (NexusConfigManager.isChatFeedbackEnabled() ? "on" : "off"));
    }

    private static void configureSound(List<String> args) {
        if (args.size() < 3) {
            NotificationManager.error("Usage: .nexus sound <on|off>");
            return;
        }

        String mode = args.get(2).toLowerCase(Locale.ROOT);
        if (!mode.equals("on") && !mode.equals("off")) {
            NotificationManager.error("Usage: .nexus sound <on|off>");
            return;
        }

        NexusConfigManager.setToggleSoundsEnabled(mode.equals("on"));
        NotificationManager.info("Toggle sound -> " + mode);
    }

    private static void configureChat(List<String> args) {
        if (args.size() < 3) {
            NotificationManager.error("Usage: .nexus chat <on|off>");
            return;
        }

        String mode = args.get(2).toLowerCase(Locale.ROOT);
        if (!mode.equals("on") && !mode.equals("off")) {
            NotificationManager.error("Usage: .nexus chat <on|off>");
            return;
        }

        NexusConfigManager.setChatFeedbackEnabled(mode.equals("on"));
        NotificationManager.info("Chat feedback -> " + mode, true);
    }

    private static void configurePersistence(List<String> args) {
        if (args.size() < 3) {
            NotificationManager.error("Usage: .nexus config <save|load>");
            return;
        }

        String action = args.get(2).toLowerCase(Locale.ROOT);
        switch (action) {
            case "save" -> {
                NexusConfigManager.save();
                NotificationManager.info("Config saved");
            }
            case "load" -> {
                NexusConfigManager.load();
                NotificationManager.info("Config loaded");
            }
            default -> NotificationManager.error("Usage: .nexus config <save|load>");
        }
    }

    private static void showHelp() {
        NotificationManager.info("Nexus command help sent to chat", true);
        NotificationManager.chat("Open the Click GUI with Right Shift to edit modules and settings visually.");
        NotificationManager.chat(".nexus toggle \"Full Bright\"");
        NotificationManager.chat(".nexus bind Zoom C");
        NotificationManager.chat(".nexus set Zoom FOV 25");
        NotificationManager.chat(".nexus reset Zoom");
        NotificationManager.chat(".nexus list");
        NotificationManager.chat(".nexus status");
        NotificationManager.chat(".nexus sound <on|off>");
        NotificationManager.chat(".nexus chat <on|off>");
        NotificationManager.chat(".nexus config <save|load>");
    }

    private static ResolvedArgument<Module> resolveModule(List<String> args, int startIndex, int endExclusive) {
        for (int endIndex = endExclusive; endIndex > startIndex; endIndex--) {
            Module module = ModuleManager.findByName(join(args, startIndex, endIndex));
            if (module != null) {
                return new ResolvedArgument<>(module, endIndex);
            }
        }
        return null;
    }

    private static ResolvedArgument<Setting<?>> resolveSetting(Module module, List<String> args, int startIndex, int endExclusive) {
        for (int endIndex = endExclusive; endIndex > startIndex; endIndex--) {
            Setting<?> setting = module.findSetting(join(args, startIndex, endIndex));
            if (setting != null) {
                return new ResolvedArgument<>(setting, endIndex);
            }
        }
        return null;
    }

    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (Character.isWhitespace(c) && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    private static String join(List<String> args, int startIndex, int endIndex) {
        if (startIndex >= endIndex || startIndex >= args.size()) {
            return "";
        }
        return args.subList(startIndex, Math.min(endIndex, args.size())).stream().collect(Collectors.joining(" "));
    }
}
