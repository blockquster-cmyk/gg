package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Settler Cabin — small overworld wooden structure.
 * Size: 7x5x7 (W x H x D)
 * Materials: oak planks, oak logs, oak stairs, glass pane, oak door
 * Loot: balanced — max 1 diamond, basic tools and food
 *
 * @author ItzAbood69
 */
public class SettlerCabin extends BaseStructure {

    public SettlerCabin(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 60 || y > 80) return;

        // ── Floor ─────────────────────────────────────────────────────────────
        for (int fx = 0; fx < 7; fx++) {
            for (int fz = 0; fz < 7; fz++) {
                setBlock(world, x + fx, y, z + fz, Material.OAK_PLANKS);
            }
        }

        // ── Walls ─────────────────────────────────────────────────────────────
        for (int wy = 1; wy <= 3; wy++) {
            // North wall (z=0)
            for (int wx = 0; wx < 7; wx++) {
                setBlock(world, x + wx, y + wy, z, Material.OAK_PLANKS);
            }
            // South wall (z=6)
            for (int wx = 0; wx < 7; wx++) {
                setBlock(world, x + wx, y + wy, z + 6, Material.OAK_PLANKS);
            }
            // West wall (x=0)
            for (int wz = 0; wz < 7; wz++) {
                setBlock(world, x, y + wy, z + wz, Material.OAK_PLANKS);
            }
            // East wall (x=6)
            for (int wz = 0; wz < 7; wz++) {
                setBlock(world, x + 6, y + wy, z + wz, Material.OAK_PLANKS);
            }
        }

        // ── Corner logs ───────────────────────────────────────────────────────
        for (int wy = 1; wy <= 3; wy++) {
            setBlock(world, x,     y + wy, z,     Material.OAK_LOG);
            setBlock(world, x + 6, y + wy, z,     Material.OAK_LOG);
            setBlock(world, x,     y + wy, z + 6, Material.OAK_LOG);
            setBlock(world, x + 6, y + wy, z + 6, Material.OAK_LOG);
        }

        // ── Windows (glass panes on wall level 2) ────────────────────────────
        setBlock(world, x + 2, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 4, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 2, y + 2, z + 6, Material.GLASS_PANE);
        setBlock(world, x + 4, y + 2, z + 6, Material.GLASS_PANE);
        setBlock(world, x,     y + 2, z + 2, Material.GLASS_PANE);
        setBlock(world, x,     y + 2, z + 4, Material.GLASS_PANE);
        setBlock(world, x + 6, y + 2, z + 2, Material.GLASS_PANE);
        setBlock(world, x + 6, y + 2, z + 4, Material.GLASS_PANE);

        // ── Door (south wall center) ──────────────────────────────────────────
        setBlock(world, x + 3, y + 1, z + 6, Material.AIR);
        setBlock(world, x + 3, y + 2, z + 6, Material.AIR);

        // ── Roof (flat oak slab roof) ─────────────────────────────────────────
        for (int rx = 0; rx < 7; rx++) {
            for (int rz = 0; rz < 7; rz++) {
                setBlock(world, x + rx, y + 4, z + rz, Material.OAK_SLAB);
            }
        }

        // ── Interior ──────────────────────────────────────────────────────────
        // Clear interior air
        for (int iy = 1; iy <= 3; iy++) {
            for (int ix = 1; ix <= 5; ix++) {
                for (int iz = 1; iz <= 5; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // Crafting table
        setBlock(world, x + 1, y + 1, z + 1, Material.CRAFTING_TABLE);
        // Torch
        setBlock(world, x + 5, y + 2, z + 1, Material.WALL_TORCH);

        // ── Loot chest ────────────────────────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 3, y + 1, z + 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillCabinChest(chest, random);
        }
    }

    private void fillCabinChest(Chest chest, Random random) {
        int rolls = 2 + random.nextInt(3); // 2-4 items max

        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.BREAD,          12, 2, 4),
            lootEntry(Material.COOKED_BEEF,     8, 1, 3),
            lootEntry(Material.APPLE,          10, 2, 5),
            lootEntry(Material.WOODEN_PICKAXE,  6, 1, 1),
            lootEntry(Material.STONE_PICKAXE,   4, 1, 1),
            lootEntry(Material.IRON_PICKAXE,    1, 1, 1),   // rare
            lootEntry(Material.IRON_INGOT,      8, 1, 3),
            lootEntry(Material.GOLD_INGOT,      3, 1, 2),
            lootEntry(Material.DIAMOND,         1, 1, 1),   // max 1 — plugin rule
            lootEntry(Material.TORCH,          10, 4, 8),
            lootEntry(Material.OAK_LOG,         8, 3, 6),
            lootEntry(Material.CRAFTING_TABLE,  5, 1, 1)
        );

        placeInChest(chest, loot);
    }
}
