package dev.itzabood69.extrateritory.populators;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.structures.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decides when and where to place structures per chunk.
 * One structure-type attempt per chunk.  Rarity values come from config.yml.
 *
 * PLACED_LOCATIONS tracks every placed instance per structure name so that
 * /territory locate structure <name> can find the nearest one to the player.
 *
 * @author ItzAbood69
 */
public class StructurePopulator extends BlockPopulator {

    public enum StructureType {
        // Overworld
        SETTLER_CABIN, ANCIENT_WELL, RUINED_WATCHTOWER, DRUID_STONE_CIRCLE, BANDIT_CAMP,
        OVERWORLD_VILLAGE,
        // Nether
        ASH_SHRINE, BLAZE_ALTAR, WITHER_SHRINE, BASALT_FORTRESS, CRIMSON_HUT, SOUL_CRYPT,
        NETHER_VILLAGE,
        // End
        VOID_RUIN, CHORUS_SPIRE, SHULKER_VAULT, OBSIDIAN_OBELISK, CRYSTAL_SANCTUM,
        ENDER_TOMB, VOID_BEACON, END_VILLAGE
    }

    /**
     * All placed locations, keyed by structure name.
     * Value is a list of every placed instance — used by /locate for nearest search.
     * Thread-safe: chunk population can run on async threads.
     */
    public static final Map<String, List<Location>> PLACED_LOCATIONS = new ConcurrentHashMap<>();

    private final ExtraTeritory plugin;
    private final StructureType type;

    public StructurePopulator(ExtraTeritory plugin, StructureType type) {
        this.plugin = plugin;
        this.type   = type;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int rarity = getRarity();
        if (random.nextInt(rarity) != 0) return;

        int x = chunk.getX() * 16 + 4 + random.nextInt(8);
        int z = chunk.getZ() * 16 + 4 + random.nextInt(8);

        placeStructure(world, x, z, random);
    }

    private void placeStructure(World world, int x, int z, Random random) {
        switch (type) {
            // ── Overworld ───────────────────────────────────────────────────
            case SETTLER_CABIN      -> place(new SettlerCabin(plugin),      world, x, z, random, "settler-cabin");
            case ANCIENT_WELL       -> place(new AncientWell(plugin),       world, x, z, random, "ancient-well");
            case RUINED_WATCHTOWER  -> place(new RuinedWatchtower(plugin),  world, x, z, random, "ruined-watchtower");
            case DRUID_STONE_CIRCLE -> place(new DruidStoneCircle(plugin),  world, x, z, random, "druid-stone-circle");
            case BANDIT_CAMP        -> place(new BanditCamp(plugin),        world, x, z, random, "bandit-camp");
            case OVERWORLD_VILLAGE  -> place(new OverworldVillage(plugin),  world, x, z, random, "overworld-village");
            // ── Nether ─────────────────────────────────────────────────────
            case ASH_SHRINE         -> place(new AshShrine(plugin),         world, x, z, random, "ash-shrine");
            case BLAZE_ALTAR        -> place(new BlazeAltar(plugin),        world, x, z, random, "blaze-altar");
            case WITHER_SHRINE      -> place(new WitherShrine(plugin),      world, x, z, random, "wither-shrine");
            case BASALT_FORTRESS    -> place(new BasaltFortress(plugin),    world, x, z, random, "basalt-fortress");
            case CRIMSON_HUT        -> place(new CrimsonHut(plugin),        world, x, z, random, "crimson-hut");
            case SOUL_CRYPT         -> place(new SoulCrypt(plugin),         world, x, z, random, "soul-crypt");
            case NETHER_VILLAGE     -> place(new NetherVillage(plugin),     world, x, z, random, "nether-village");
            // ── End ────────────────────────────────────────────────────────
            case VOID_RUIN          -> place(new VoidRuin(plugin),          world, x, z, random, "void-ruin");
            case CHORUS_SPIRE       -> place(new ChorusSpire(plugin),       world, x, z, random, "chorus-spire");
            case SHULKER_VAULT      -> place(new ShulkerVault(plugin),      world, x, z, random, "shulker-vault");
            case OBSIDIAN_OBELISK   -> place(new ObsidianObelisk(plugin),   world, x, z, random, "obsidian-obelisk");
            case CRYSTAL_SANCTUM    -> place(new CrystalSanctum(plugin),    world, x, z, random, "crystal-sanctum");
            case ENDER_TOMB         -> place(new EnderTomb(plugin),         world, x, z, random, "ender-tomb");
            case VOID_BEACON        -> place(new VoidBeacon(plugin),        world, x, z, random, "void-beacon");
            case END_VILLAGE        -> place(new EndVillage(plugin),        world, x, z, random, "end-village");
        }
    }

    /**
     * Places a structure and registers its location for /locate.
     */
    private void place(BaseStructure structure, World world, int x, int z, Random random, String key) {
        structure.place(world, x, z, random);
        int surfaceY = world.getHighestBlockYAt(x, z);
        Location loc = new Location(world, x, surfaceY, z);

        PLACED_LOCATIONS.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(loc);
    }

    // ── Rarity lookup ─────────────────────────────────────────────────────────

    private int getRarity() {
        return switch (type) {
            case SETTLER_CABIN      -> plugin.getConfig().getInt("structures.settler-cabin-rarity",      180);
            case ANCIENT_WELL       -> plugin.getConfig().getInt("structures.ancient-well-rarity",       200);
            case RUINED_WATCHTOWER  -> plugin.getConfig().getInt("structures.ruined-watchtower-rarity",  190);
            case DRUID_STONE_CIRCLE -> plugin.getConfig().getInt("structures.druid-stone-circle-rarity", 220);
            case BANDIT_CAMP        -> plugin.getConfig().getInt("structures.bandit-camp-rarity",        170);
            case ASH_SHRINE         -> plugin.getConfig().getInt("structures.ash-shrine-rarity",         160);
            case BLAZE_ALTAR        -> plugin.getConfig().getInt("structures.blaze-altar-rarity",        190);
            case WITHER_SHRINE      -> plugin.getConfig().getInt("structures.wither-shrine-rarity",      200);
            case BASALT_FORTRESS    -> plugin.getConfig().getInt("structures.basalt-fortress-rarity",    210);
            case CRIMSON_HUT        -> plugin.getConfig().getInt("structures.crimson-hut-rarity",        175);
            case SOUL_CRYPT         -> plugin.getConfig().getInt("structures.soul-crypt-rarity",         185);
            case VOID_RUIN          -> plugin.getConfig().getInt("structures.void-ruin-rarity",          170);
            case CHORUS_SPIRE       -> plugin.getConfig().getInt("structures.chorus-spire-rarity",       180);
            case SHULKER_VAULT      -> plugin.getConfig().getInt("structures.shulker-vault-rarity",      200);
            case OBSIDIAN_OBELISK   -> plugin.getConfig().getInt("structures.obsidian-obelisk-rarity",   190);
            case CRYSTAL_SANCTUM    -> plugin.getConfig().getInt("structures.crystal-sanctum-rarity",    210);
            case ENDER_TOMB         -> plugin.getConfig().getInt("structures.ender-tomb-rarity",         220);
            case VOID_BEACON        -> plugin.getConfig().getInt("structures.void-beacon-rarity",        230);
            case OVERWORLD_VILLAGE  -> plugin.getConfig().getInt("structures.overworld-village-rarity",  400);
            case NETHER_VILLAGE     -> plugin.getConfig().getInt("structures.nether-village-rarity",     420);
            case END_VILLAGE        -> plugin.getConfig().getInt("structures.end-village-rarity",         440);
        };
    }

    // ── Static helper — find nearest placed location to a player ─────────────

    /**
     * Returns the nearest recorded location for the given structure name,
     * searching only within the same world as {@code origin}.
     * Returns null if none has been generated yet.
     */
    public static Location findNearest(String structureName, Location origin) {
        List<Location> locs = PLACED_LOCATIONS.get(structureName.toLowerCase());
        if (locs == null || locs.isEmpty()) return null;

        Location nearest = null;
        double   bestDist = Double.MAX_VALUE;

        synchronized (locs) {
            for (Location loc : locs) {
                if (!loc.getWorld().equals(origin.getWorld())) continue;
                double dist = origin.distanceSquared(loc);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest  = loc;
                }
            }
        }
        return nearest;
    }

    /** All known structure names (for tab-complete). */
    public static List<String> allStructureNames() {
        return List.of(
            // Overworld
            "settler-cabin", "ancient-well", "ruined-watchtower",
            "druid-stone-circle", "bandit-camp", "overworld-village",
            // Nether
            "ash-shrine", "blaze-altar", "wither-shrine",
            "basalt-fortress", "crimson-hut", "soul-crypt", "nether-village",
            // End
            "void-ruin", "chorus-spire", "shulker-vault", "obsidian-obelisk",
            "crystal-sanctum", "ender-tomb", "void-beacon", "end-village"
        );
    }
}
