package nexus.zigliix.com.client.module;

public enum Category {
    HUD("HUD"),
    VISUAL("Visual"),
    COSMETIC("Cosmetic"),
    MOVEMENT("Movement"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
