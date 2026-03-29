package dev.itzabood69.extrateritory.models;

/**
 * Represents the three playable dimensions in ExtraTeritory.
 * Each dimension has a display name, tab color, and associated Minecraft world environment.
 *
 * @author ItzAbood69
 */
public enum Dimension {

    OVERWORLD("Overworld", "&a[Overworld]&r ", "world"),
    NETHER("Nether", "&c[Nether]&r ", "world_nether"),
    END("End", "&5[End]&r ", "world_the_end");

    private final String displayName;
    private final String tabPrefix;
    private final String defaultWorld;

    Dimension(String displayName, String tabPrefix, String defaultWorld) {
        this.displayName = displayName;
        this.tabPrefix = tabPrefix;
        this.defaultWorld = defaultWorld;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTabPrefix() {
        return tabPrefix;
    }

    public String getDefaultWorld() {
        return defaultWorld;
    }

    /**
     * Parse a dimension from a string (case-insensitive).
     *
     * @param input the raw string input
     * @return the matching Dimension or null
     */
    public static Dimension fromString(String input) {
        if (input == null) return null;
        switch (input.toLowerCase()) {
            case "overworld": return OVERWORLD;
            case "nether":    return NETHER;
            case "end":       return END;
            default:          return null;
        }
    }

    /**
     * Returns the Dimension that corresponds to a Bukkit world name.
     *
     * @param worldName the Bukkit world name
     * @return the matching Dimension or null
     */
    public static Dimension fromWorldName(String worldName) {
        if (worldName == null) return null;
        for (Dimension d : values()) {
            if (d.defaultWorld.equalsIgnoreCase(worldName)) return d;
        }
        return null;
    }
}
