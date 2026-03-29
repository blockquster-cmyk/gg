package dev.itzabood69.extrateritory.managers;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Manages fixed spawn locations for each dimension.
 * Spawns are stored in config.yml and hot-reloadable via /territory setspawn.
 *
 * @author ItzAbood69
 */
public class SpawnManager {

    private final ExtraTeritory plugin;

    public SpawnManager(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the configured spawn Location for the given dimension, or null if the world is not loaded.
     */
    public Location getSpawn(Dimension dimension) {
        String base = "spawns." + dimension.name().toLowerCase() + ".";
        String worldName = plugin.getConfig().getString(base + "world", dimension.getDefaultWorld());
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x   = plugin.getConfig().getDouble(base + "x",   0.5);
        double y   = plugin.getConfig().getDouble(base + "y",   64.0);
        double z   = plugin.getConfig().getDouble(base + "z",   0.5);
        float  yaw = (float) plugin.getConfig().getDouble(base + "yaw",   0.0);
        float  pit = (float) plugin.getConfig().getDouble(base + "pitch", 0.0);

        return new Location(world, x, y, z, yaw, pit);
    }

    /**
     * Saves a new spawn location for the given dimension to config.yml.
     */
    public void setSpawn(Dimension dimension, Location loc) {
        String base = "spawns." + dimension.name().toLowerCase() + ".";
        plugin.getConfig().set(base + "world", loc.getWorld().getName());
        plugin.getConfig().set(base + "x",     loc.getX());
        plugin.getConfig().set(base + "y",     loc.getY());
        plugin.getConfig().set(base + "z",     loc.getZ());
        plugin.getConfig().set(base + "yaw",   (double) loc.getYaw());
        plugin.getConfig().set(base + "pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    /**
     * Returns true if a world is associated with the given dimension.
     */
    public boolean isWorldForDimension(World world, Dimension dimension) {
        String cfgWorld = plugin.getConfig().getString(
                "spawns." + dimension.name().toLowerCase() + ".world",
                dimension.getDefaultWorld()
        );
        return world.getName().equalsIgnoreCase(cfgWorld);
    }

    /**
     * Detects which Dimension a world belongs to based on config.
     * Returns null if the world is unrecognised.
     */
    public Dimension getDimensionForWorld(World world) {
        for (Dimension d : Dimension.values()) {
            if (isWorldForDimension(world, d)) return d;
        }
        return null;
    }
}
