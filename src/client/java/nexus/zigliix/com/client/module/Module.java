package nexus.zigliix.com.client.module;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable()  {}
    public void onDisable() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public String   getName()       { return name; }
    public String   getDescription(){ return description; }
    public Category getCategory()   { return category; }
    public boolean  isEnabled()     { return enabled; }
}
