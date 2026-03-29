package dev.itzabood69.extrateritory.managers;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Handles loading, saving, and in-memory caching of all player data.
 * Each player gets their own YAML file under /plugins/ExtraTeritory/players/.
 *
 * @author ItzAbood69
 */
public class DataManager {

    private final ExtraTeritory plugin;
    private final File playersFolder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    // Tracks how many players are in each dimension (for equal distribution)
    private final Map<Dimension, Integer> dimensionCounts = new EnumMap<>(Dimension.class);

    public DataManager(ExtraTeritory plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) playersFolder.mkdirs();

        for (Dimension d : Dimension.values()) dimensionCounts.put(d, 0);
        loadAllPlayers();
    }

    // ── Load ────────────────────────────────────────────────────────────────

    private void loadAllPlayers() {
        File[] files = playersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            try {
                UUID uuid = UUID.fromString(cfg.getString("uuid", ""));
                String name = cfg.getString("name", "Unknown");
                Dimension dim = Dimension.fromString(cfg.getString("dimension", "overworld"));
                if (dim == null) dim = Dimension.OVERWORLD;

                PlayerData data = new PlayerData(uuid, name, dim);
                data.setPvpDeaths(cfg.getInt("pvpDeaths", 0));

                List<String> immunities = cfg.getStringList("heartImmunities");
                for (String s : immunities) {
                    Dimension id = Dimension.fromString(s);
                    if (id != null) data.addHeartImmunity(id);
                }

                cache.put(uuid, data);
                dimensionCounts.merge(dim, 1, Integer::sum);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load player file: " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Loaded " + cache.size() + " player profiles.");
    }

    // ── Save ────────────────────────────────────────────────────────────────

    public void savePlayer(PlayerData data) {
        File file = new File(playersFolder, data.getUuid() + ".yml");
        FileConfiguration cfg = new YamlConfiguration();

        cfg.set("uuid", data.getUuid().toString());
        cfg.set("name", data.getName());
        cfg.set("dimension", data.getDimension().name().toLowerCase());
        cfg.set("pvpDeaths", data.getPvpDeaths());

        List<String> immunities = new ArrayList<>();
        for (Dimension d : data.getHeartImmunities()) immunities.add(d.name().toLowerCase());
        cfg.set("heartImmunities", immunities);

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for " + data.getName(), e);
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) savePlayer(data);
        plugin.getLogger().info("All player data saved.");
    }

    // ── Get / Create ────────────────────────────────────────────────────────

    /**
     * Returns the PlayerData for a UUID, or null if not registered.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Creates a new PlayerData for a first-time player, assigns a balanced dimension, and saves it.
     */
    public PlayerData createPlayerData(UUID uuid, String name) {
        Dimension assigned = getLeastPopulatedDimension();
        PlayerData data = new PlayerData(uuid, name, assigned);
        cache.put(uuid, data);
        dimensionCounts.merge(assigned, 1, Integer::sum);
        savePlayer(data);
        return data;
    }

    /**
     * Returns true if a player has been registered before.
     */
    public boolean hasPlayerData(UUID uuid) {
        return cache.containsKey(uuid);
    }

    // ── Dimension Management ────────────────────────────────────────────────

    /**
     * Changes a player's dimension, updates the count maps, and saves.
     */
    public void setPlayerDimension(PlayerData data, Dimension newDimension) {
        Dimension old = data.getDimension();
        dimensionCounts.merge(old, -1, Integer::sum);
        data.setDimension(newDimension);
        dimensionCounts.merge(newDimension, 1, Integer::sum);
        savePlayer(data);
    }

    /**
     * Returns the dimension with the fewest assigned players (for balanced distribution).
     */
    private Dimension getLeastPopulatedDimension() {
        return dimensionCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Dimension.OVERWORLD);
    }

    public Map<Dimension, Integer> getDimensionCounts() {
        return Collections.unmodifiableMap(dimensionCounts);
    }

    public Collection<PlayerData> getAllPlayerData() {
        return Collections.unmodifiableCollection(cache.values());
    }
}
