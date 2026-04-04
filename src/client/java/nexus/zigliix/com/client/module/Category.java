package nexus.zigliix.com.client.module;

public enum Category {
    COMBAT("Combat"),
    VISUAL("Visual"),
    MOVEMENT("Movement"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
