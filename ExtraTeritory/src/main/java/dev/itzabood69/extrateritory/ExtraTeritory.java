package dev.itzabood69.extrateritory;

import dev.itzabood69.extrateritory.commands.TerritoryCommand;
import dev.itzabood69.extrateritory.listeners.*;
import dev.itzabood69.extrateritory.managers.DataManager;
import dev.itzabood69.extrateritory.managers.HeartManager;
import dev.itzabood69.extrateritory.managers.SpawnManager;
import dev.itzabood69.extrateritory.populators.StructurePopulator;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * ExtraTeritory — Competitive three-dimension PvP survival plugin.
 *
 * World generation: fully vanilla — only custom structures are injected
 * via BlockPopulators registered in getDefaultWorldGenerator.
 *
 * @author ItzAbood69
 * @version 1.0.0
 * @mc-version 1.21.1
 */
public final class ExtraTeritory extends JavaPlugin {

    private DataManager  dataManager;
    private HeartManager heartManager;
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.dataManager  = new DataManager(this);
        this.heartManager = new HeartManager(this);
        this.spawnManager = new SpawnManager(this);

        // ── Commands ─────────────────────────────────────────────────────────
        TerritoryCommand cmd = new TerritoryCommand(this);
        getCommand("territory").setExecutor(cmd);
        getCommand("territory").setTabCompleter(cmd);

        // ── Listeners ─────────────────────────────────────────────────────────
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this),      this);
        getServer().getPluginManager().registerEvents(new PvPListener(this),             this);
        getServer().getPluginManager().registerEvents(new DimensionDebuffListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(this),         this);
        getServer().getPluginManager().registerEvents(new HeartDropListener(this),       this);
        getServer().getPluginManager().registerEvents(new HeartPickupListener(this),     this);
        getServer().getPluginManager().registerEvents(new TpaListener(this),             this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this),         this);
        getServer().getPluginManager().registerEvents(new TabListListener(this),         this);

        printStartBanner();
    }

    private void printStartBanner() {
        getLogger().info(MessageUtil.SEPARATOR);
        getLogger().info("  ✦  ExtraTeritory  v" + getDescription().getVersion() + "  ✦");
        getLogger().info("  Author : ItzAbood69  |  MC: 1.21.1");
        getLogger().info("  World  : 100% Vanilla generation");
        getLogger().info("  Structures injected via BlockPopulators in all 3 dimensions.");
        getLogger().info("  Add to bukkit.yml to inject structures:");
        getLogger().info("    worlds:");
        getLogger().info("      world:         generator: ExtraTeritory");
        getLogger().info("      world_nether:  generator: ExtraTeritory:nether");
        getLogger().info("      world_the_end: generator: ExtraTeritory:end");
        getLogger().info("  Then DELETE existing world folders and restart.");
        getLogger().info(MessageUtil.SEPARATOR);
    }

    /**
     * Returns a pass-through ChunkGenerator that uses 100% vanilla terrain
     * but injects our custom structure populators on top.
     */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        String dim = (id != null) ? id.toLowerCase() : inferDim(worldName);
        return new VanillaPassthroughGenerator(this, dim);
    }

    private String inferDim(String worldName) {
        if (worldName.equalsIgnoreCase("world_nether"))  return "nether";
        if (worldName.equalsIgnoreCase("world_the_end")) return "end";
        return "overworld";
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveAll();
        getLogger().info(MessageUtil.SEPARATOR);
        getLogger().info("  ExtraTeritory disabled — all player data saved.");
        getLogger().info(MessageUtil.SEPARATOR);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public DataManager  getDataManager()  { return dataManager;  }
    public HeartManager getHeartManager() { return heartManager; }
    public SpawnManager getSpawnManager() { return spawnManager; }

    // ═══════════════════════════════════════════════════════════════════════
    //  VanillaPassthroughGenerator
    //  Lets vanilla terrain run untouched; only adds structure populators.
    // ═══════════════════════════════════════════════════════════════════════

    private static class VanillaPassthroughGenerator extends ChunkGenerator {

        private final ExtraTeritory plugin;
        private final String dim;

        VanillaPassthroughGenerator(ExtraTeritory plugin, String dim) {
            this.plugin = plugin;
            this.dim    = dim;
        }

        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {
            List<BlockPopulator> pops = new ArrayList<>();

            switch (dim) {
                case "nether" -> {
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.ASH_SHRINE));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.BLAZE_ALTAR));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.WITHER_SHRINE));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.BASALT_FORTRESS));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.CRIMSON_HUT));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.SOUL_CRYPT));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.NETHER_VILLAGE));
                }
                case "end" -> {
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.VOID_RUIN));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.CHORUS_SPIRE));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.SHULKER_VAULT));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.OBSIDIAN_OBELISK));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.CRYSTAL_SANCTUM));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.ENDER_TOMB));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.VOID_BEACON));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.END_VILLAGE));
                }
                default -> { // overworld
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.SETTLER_CABIN));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.ANCIENT_WELL));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.RUINED_WATCHTOWER));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.DRUID_STONE_CIRCLE));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.BANDIT_CAMP));
                    pops.add(new StructurePopulator(plugin, StructurePopulator.StructureType.OVERWORLD_VILLAGE));
                }
            }
            return pops;
        }

        // All vanilla flags ON — let the server generate everything normally
        @Override public boolean shouldGenerateNoise()       { return true; }
        @Override public boolean shouldGenerateSurface()     { return true; }
        @Override public boolean shouldGenerateBedrock()     { return true; }
        @Override public boolean shouldGenerateCaves()       { return true; }
        @Override public boolean shouldGenerateDecorations() { return true; }
        @Override public boolean shouldGenerateMobs()        { return true; }
        @Override public boolean shouldGenerateStructures()  { return true; }
    }
}
