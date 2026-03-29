package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Nether Village — a piglin/zombie-villager settlement in the nether.
 * Size: ~18x18 footprint, 3 huts + central fire pit + bartering chest.
 * Materials: nether bricks, basalt, blackstone, crimson planks
 * Residents: 3-4 Piglins + 1-2 Zombified Villagers (with actual Villager professions)
 * Loot: nether resources, gold, rare gear
 *
 * @author ItzAbood69
 */
public class NetherVillage extends BaseStructure {

    public NetherVillage(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 20 || y > 100) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Basalt/nether brick path ──────────────────────────────────────
        for (int i = 0; i < 18; i++) {
            setBlock(world, x + i, y + 1, z + 9, Material.BASALT);
            setBlock(world, x + 9, y + 1, z + i, Material.BASALT);
        }

        // ── 3 huts ────────────────────────────────────────────────────────
        buildNetherHut(world, x + 1, y + 1, z + 1, random, true);
        buildNetherHut(world, x + 11, y + 1, z + 1, random, false);
        buildNetherHut(world, x + 1, y + 1, z + 11, random, false);

        // ── Central fire pit ──────────────────────────────────────────────
        buildFirePit(world, x + 8, y + 1, z + 8);

        // ── Spawn residents ───────────────────────────────────────────────
        spawnResidents(world, x, y + 2, z, random);
    }

    private void buildNetherHut(World world, int x, int y, int z, Random random, boolean hasChest) {
        // Floor — nether bricks
        for (int fx = 0; fx < 6; fx++)
            for (int fz = 0; fz < 6; fz++)
                setBlock(world, x + fx, y, z + fz, Material.NETHER_BRICKS);

        // Walls — mix of nether brick and blackstone
        for (int wy = 1; wy <= 3; wy++) {
            for (int wx = 0; wx < 6; wx++) {
                Material mat = (wx % 2 == 0) ? Material.NETHER_BRICKS : Material.BLACKSTONE;
                setBlock(world, x + wx, y + wy, z,     mat);
                setBlock(world, x + wx, y + wy, z + 5, mat);
            }
            for (int wz = 1; wz < 5; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.NETHER_BRICKS);
                setBlock(world, x + 5, y + wy, z + wz, Material.NETHER_BRICKS);
            }
        }

        // Roof — nether brick slabs
        for (int rx = 0; rx < 6; rx++)
            for (int rz = 0; rz < 6; rz++)
                setBlock(world, x + rx, y + 4, z + rz, Material.NETHER_BRICK_SLAB);

        // Corner pillars
        for (int wy = 1; wy <= 4; wy++) {
            setBlock(world, x,     y + wy, z,     Material.CHISELED_NETHER_BRICKS);
            setBlock(world, x + 5, y + wy, z,     Material.CHISELED_NETHER_BRICKS);
            setBlock(world, x,     y + wy, z + 5, Material.CHISELED_NETHER_BRICKS);
            setBlock(world, x + 5, y + wy, z + 5, Material.CHISELED_NETHER_BRICKS);
        }

        // Entrance
        setBlock(world, x + 2, y + 1, z + 5, Material.AIR);
        setBlock(world, x + 2, y + 2, z + 5, Material.AIR);

        // Soul lantern light
        setBlock(world, x + 4, y + 3, z + 1, Material.SOUL_LANTERN);

        // Interior clear
        for (int iy = 1; iy <= 3; iy++)
            for (int ix = 1; ix <= 4; ix++)
                for (int iz = 1; iz <= 4; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // Small fire
        setBlock(world, x + 1, y + 1, z + 1, Material.SOUL_SAND);
        setBlock(world, x + 1, y + 2, z + 1, Material.SOUL_FIRE);

        if (hasChest) {
            Block chestBlock = world.getBlockAt(x + 4, y + 1, z + 4);
            chestBlock.setType(Material.CHEST);
            if (chestBlock.getState() instanceof Chest chest) {
                fillNetherHutChest(chest, random);
            }
        }
    }

    private void buildFirePit(World world, int x, int y, int z) {
        // 3x3 magma/soul sand ring with fire
        for (int fx = 0; fx < 3; fx++)
            for (int fz = 0; fz < 3; fz++)
                setBlock(world, x + fx, y, z + fz,
                    ((fx + fz) % 2 == 0) ? Material.MAGMA_BLOCK : Material.SOUL_SAND);
        setBlock(world, x + 1, y + 1, z + 1, Material.FIRE);

        // Ring of bone blocks
        setBlock(world, x,     y + 1, z,     Material.BONE_BLOCK);
        setBlock(world, x + 2, y + 1, z,     Material.BONE_BLOCK);
        setBlock(world, x,     y + 1, z + 2, Material.BONE_BLOCK);
        setBlock(world, x + 2, y + 1, z + 2, Material.BONE_BLOCK);
    }

    private void spawnResidents(World world, int x, int y, int z, Random random) {
        // Piglins
        int piglinCount = 2 + random.nextInt(3); // 2-4
        int[][] piglinSpots = {{3,3},{13,3},{3,13},{9,9},{13,13}};
        for (int i = 0; i < piglinCount; i++) {
            int[] spot = piglinSpots[i % piglinSpots.length];
            Location loc = new Location(world, x + spot[0] + 0.5, y + 1, z + spot[1] + 0.5);
            Piglin piglin = world.spawn(loc, Piglin.class);
            piglin.setImmuneToZombification(true); // keep them as Piglins
            piglin.setAdult();
        }

        // Zombified Villagers with professions — these are the "village traders"
        // We spawn actual Villagers since ZombieVillager can't be traded with easily
        Villager.Profession[] profs = {
            Villager.Profession.CLERIC,
            Villager.Profession.WEAPONSMITH,
            Villager.Profession.ARMORER
        };
        int vilCount = 1 + random.nextInt(2);
        int[][] vilSpots = {{4, 4}, {12, 4}};
        for (int i = 0; i < vilCount; i++) {
            int[] spot = vilSpots[i % vilSpots.length];
            Location loc = new Location(world, x + spot[0] + 0.5, y + 1, z + spot[1] + 0.5);
            Villager v = world.spawn(loc, Villager.class);
            v.setProfession(profs[random.nextInt(profs.length)]);
            v.setVillagerType(Villager.Type.DESERT); // desert look fits nether aesthetic
            v.setVillagerLevel(2 + random.nextInt(2));
        }
    }

    private void fillNetherHutChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.GOLD_INGOT,         10, 3, 6),
            lootEntry(Material.GOLD_BLOCK,          2, 1, 1),
            lootEntry(Material.QUARTZ,       9, 3, 7),
            lootEntry(Material.BLAZE_ROD,           6, 1, 3),
            lootEntry(Material.MAGMA_CREAM,         7, 1, 4),
            lootEntry(Material.GOLDEN_SWORD,        5, 1, 1),
            lootEntry(Material.GOLDEN_CHESTPLATE,   3, 1, 1),
            lootEntry(Material.COOKED_PORKCHOP,     8, 3, 6),
            lootEntry(Material.OBSIDIAN,            6, 2, 4),
            lootEntry(Material.DIAMOND,             1, 1, 1),
            lootEntry(Material.NETHERITE_SCRAP,     1, 1, 1)
        );
        placeInChest(chest, loot);
    }
}
