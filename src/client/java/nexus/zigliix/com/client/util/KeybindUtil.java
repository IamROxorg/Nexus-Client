package nexus.zigliix.com.client.util;

import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class KeybindUtil {
    public static final int NONE = GLFW.GLFW_KEY_UNKNOWN;
    public static final int GUI_TOGGLE_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private KeybindUtil() {}

    public static String normalizeName(String value) {
        return value == null ? "" : value.replace("_", "").replace("-", "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    public static boolean isReserved(int keyCode) {
        return keyCode == GUI_TOGGLE_KEY;
    }

    public static String getKeyName(int keyCode) {
        if (keyCode == NONE) {
            return "NONE";
        }

        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }

        String glfwName = GLFW.glfwGetKeyName(keyCode, 0);
        if (glfwName != null && !glfwName.isBlank()) {
            return glfwName.toUpperCase(Locale.ROOT);
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGEUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGEDOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            default -> "KEY" + keyCode;
        };
    }

    public static int parseKey(String rawValue) {
        String value = normalizeName(rawValue);
        if (value.isEmpty() || value.equals("none") || value.equals("unbind")) {
            return NONE;
        }

        if (value.length() == 1) {
            char c = Character.toUpperCase(value.charAt(0));
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }

        if (value.startsWith("f")) {
            try {
                int number = Integer.parseInt(value.substring(1));
                if (number >= 1 && number <= 25) {
                    return GLFW.GLFW_KEY_F1 + number - 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return switch (value) {
            case "space" -> GLFW.GLFW_KEY_SPACE;
            case "tab" -> GLFW.GLFW_KEY_TAB;
            case "esc", "escape" -> GLFW.GLFW_KEY_ESCAPE;
            case "enter", "return" -> GLFW.GLFW_KEY_ENTER;
            case "backspace" -> GLFW.GLFW_KEY_BACKSPACE;
            case "lshift", "leftshift" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "rshift", "rightshift" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "lctrl", "leftctrl", "leftcontrol" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "rctrl", "rightctrl", "rightcontrol" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "lalt", "leftalt" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "ralt", "rightalt" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "up" -> GLFW.GLFW_KEY_UP;
            case "down" -> GLFW.GLFW_KEY_DOWN;
            case "left" -> GLFW.GLFW_KEY_LEFT;
            case "right" -> GLFW.GLFW_KEY_RIGHT;
            case "insert" -> GLFW.GLFW_KEY_INSERT;
            case "delete" -> GLFW.GLFW_KEY_DELETE;
            case "home" -> GLFW.GLFW_KEY_HOME;
            case "end" -> GLFW.GLFW_KEY_END;
            case "pageup" -> GLFW.GLFW_KEY_PAGE_UP;
            case "pagedown" -> GLFW.GLFW_KEY_PAGE_DOWN;
            default -> NONE;
        };
    }
}
