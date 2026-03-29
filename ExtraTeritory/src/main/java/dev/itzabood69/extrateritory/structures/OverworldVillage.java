package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Overworld Village — a small 4-house hamlet with actual villagers spawned inside.
 * Size: ~20x20 footprint, 4 houses + central well + lamp posts.
 * Materials: oak planks, cobblestone, glass pane, oak door, gravel path
 * Villagers: 3-5 spawned inside houses with random professions
 * Loot: food, tools, emeralds, seeds
 *
 * @author ItzAbood69
 */
public class OverworldVillage extends BaseStructure {

    public OverworldVillage(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 60 || y > 80) return;

        // ── Gravel path cross through the village ─────────────────────────
        for (int i = 0; i < 20; i++) {
            setBlock(world, x + i, y + 1, z + 10, Material.GRAVEL);
            setBlock(world, x + 10, y + 1, z + i, Material.GRAVEL);
        }

        // ── House 1: NW corner ────────────────────────────────────────────
        buildHouse(world, x + 1, y + 1, z + 1, random, true);

        // ── House 2: NE corner ────────────────────────────────────────────
        buildHouse(world, x + 13, y + 1, z + 1, random, false);

        // ── House 3: SW corner ────────────────────────────────────────────
        buildHouse(world, x + 1, y + 1, z + 13, random, false);

        // ── House 4: SE corner ────────────────────────────────────────────
        buildHouse(world, x + 13, y + 1, z + 13, random, true);

        // ── Central well ──────────────────────────────────────────────────
        buildWell(world, x + 9, y + 1, z + 9);

        // ── Lamp posts at path intersections ──────────────────────────────
        buildLampPost(world, x + 7, y + 1, z + 7);
        buildLampPost(world, x + 13, y + 1, z + 7);
        buildLampPost(world, x + 7, y + 1, z + 13);
        buildLampPost(world, x + 13, y + 1, z + 13);

        // ── Spawn villagers ───────────────────────────────────────────────
        spawnVillagers(world, x, y + 2, z, random);
    }

    // ── Small house: 6x5x6 ────────────────────────────────────────────────

    private void buildHouse(World world, int x, int y, int z, Random random, boolean hasChest) {
        // Floor
        for (int fx = 0; fx < 6; fx++)
            for (int fz = 0; fz < 6; fz++)
                setBlock(world, x + fx, y, z + fz, Material.OAK_PLANKS);

        // Walls
        for (int wy = 1; wy <= 3; wy++) {
            for (int wx = 0; wx < 6; wx++) {
                setBlock(world, x + wx, y + wy, z,     Material.OAK_PLANKS);
                setBlock(world, x + wx, y + wy, z + 5, Material.OAK_PLANKS);
            }
            for (int wz = 1; wz < 5; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.OAK_LOG);
                setBlock(world, x + 5, y + wy, z + wz, Material.OAK_LOG);
            }
        }

        // Windows
        setBlock(world, x + 2, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 3, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 2, y + 2, z + 5, Material.GLASS_PANE);
        setBlock(world, x + 3, y + 2, z + 5, Material.GLASS_PANE);
        setBlock(world, x + 5, y + 2, z + 2, Material.GLASS_PANE);
        setBlock(world, x + 5, y + 2, z + 3, Material.GLASS_PANE);

        // Door (south face)
        setBlock(world, x + 2, y + 1, z + 5, Material.AIR);
        setBlock(world, x + 2, y + 2, z + 5, Material.AIR);
        setBlock(world, x + 2, y + 1, z + 5, Material.OAK_DOOR);

        // Roof (pitched oak stairs)
        for (int rz = 0; rz < 6; rz++) {
            setBlock(world, x,     y + 4, z + rz, Material.OAK_STAIRS);
            setBlock(world, x + 1, y + 4, z + rz, Material.OAK_SLAB);
            setBlock(world, x + 2, y + 5, z + rz, Material.OAK_PLANKS);
            setBlock(world, x + 3, y + 5, z + rz, Material.OAK_PLANKS);
            setBlock(world, x + 4, y + 4, z + rz, Material.OAK_SLAB);
            setBlock(world, x + 5, y + 4, z + rz, Material.OAK_STAIRS);
        }

        // Interior clear
        for (int iy = 1; iy <= 3; iy++)
            for (int ix = 1; ix <= 4; ix++)
                for (int iz = 1; iz <= 4; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // Interior furnishings
        setBlock(world, x + 1, y + 1, z + 1, Material.CRAFTING_TABLE);
        setBlock(world, x + 4, y + 1, z + 1, Material.FURNACE);
        setBlock(world, x + 4, y + 2, z + 4, Material.WALL_TORCH);

        // Loot chest if flagged
        if (hasChest) {
            Block chestBlock = world.getBlockAt(x + 1, y + 1, z + 4);
            chestBlock.setType(Material.CHEST);
            if (chestBlock.getState() instanceof Chest chest) {
                fillVillageChest(chest, random);
            }
        }

        // Bed
        setBlock(world, x + 3, y + 1, z + 3, Material.RED_BED);
    }

    private void buildWell(World world, int x, int y, int z) {
        // 3x3 cobble base with water
        for (int wx = 0; wx < 3; wx++)
            for (int wz = 0; wz < 3; wz++)
                setBlock(world, x + wx, y, z + wz, Material.COBBLESTONE);
        setBlock(world, x + 1, y, z + 1, Material.WATER);

        // Walls 2 high
        for (int wy = 1; wy <= 2; wy++) {
            setBlock(world, x,     y + wy, z,     Material.COBBLESTONE);
            setBlock(world, x + 2, y + wy, z,     Material.COBBLESTONE);
            setBlock(world, x,     y + wy, z + 2, Material.COBBLESTONE);
            setBlock(world, x + 2, y + wy, z + 2, Material.COBBLESTONE);
        }

        // Crossbeam
        setBlock(world, x,     y + 3, z + 1, Material.OAK_FENCE);
        setBlock(world, x + 1, y + 3, z + 1, Material.OAK_FENCE);
        setBlock(world, x + 2, y + 3, z + 1, Material.OAK_FENCE);
        setBlock(world, x + 1, y + 4, z + 1, Material.OAK_LOG);
    }

    private void buildLampPost(World world, int x, int y, int z) {
        setBlock(world, x, y,     z, Material.OAK_FENCE);
        setBlock(world, x, y + 1, z, Material.OAK_FENCE);
        setBlock(world, x, y + 2, z, Material.OAK_FENCE);
        setBlock(world, x, y + 3, z, Material.LANTERN);
    }

    // ── Villager spawning ─────────────────────────────────────────────────────

    private void spawnVillagers(World world, int x, int y, int z, Random random) {
        int count = 3 + random.nextInt(3); // 3-5 villagers

        Villager.Profession[] professions = {
            Villager.Profession.FARMER,
            Villager.Profession.LIBRARIAN,
            Villager.Profession.WEAPONSMITH,
            Villager.Profession.TOOLSMITH,
            Villager.Profession.BUTCHER,
            Villager.Profession.SHEPHERD,
            Villager.Profession.FLETCHER,
            Villager.Profession.CLERIC
        };

        // Spawn positions inside the 4 houses
        int[][] spawnOffsets = {
            {3, 3}, {15, 3}, {3, 15}, {15, 15}, {3, 8}
        };

        for (int i = 0; i < count; i++) {
            int[] off = spawnOffsets[i % spawnOffsets.length];
            Location loc = new Location(world, x + off[0] + 0.5, y + 1, z + off[1] + 0.5);

            Villager villager = world.spawn(loc, Villager.class);
            villager.setProfession(professions[random.nextInt(professions.length)]);
            villager.setVillagerType(Villager.Type.PLAINS);

            // Give them a level so they have trades
            villager.setVillagerLevel(1 + random.nextInt(3));
        }
    }

    private void fillVillageChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.BREAD,            10, 3, 6),
            lootEntry(Material.WHEAT,             9, 4, 8),
            lootEntry(Material.CARROT,            9, 3, 7),
            lootEntry(Material.POTATO,            9, 3, 7),
            lootEntry(Material.EMERALD,           5, 1, 3),
            lootEntry(Material.IRON_INGOT,        7, 1, 3),
            lootEntry(Material.IRON_HOE,          5, 1, 1),
            lootEntry(Material.WOODEN_SWORD,      6, 1, 1),
            lootEntry(Material.OAK_LOG,           8, 3, 6),
            lootEntry(Material.APPLE,             8, 2, 5),
            lootEntry(Material.DIAMOND,           1, 1, 1),
            lootEntry(Material.BOOK,              6, 1, 2)
        );
        placeInChest(chest, loot);
    }
}
