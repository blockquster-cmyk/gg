package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Ruined Watchtower — a crumbled stone guard tower.
 * Size: 5x10x5, deliberately broken with random wall gaps.
 * Materials: cobblestone, mossy cobblestone, stone brick stairs, oak fence
 * Loot: weapons, armor scraps, arrows, gold
 *
 * @author ItzAbood69
 */
public class RuinedWatchtower extends BaseStructure {

    public RuinedWatchtower(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 60 || y > 80) return;

        // ── Foundation ─────────────────────────────────────────────────────
        for (int fx = 0; fx < 5; fx++) {
            for (int fz = 0; fz < 5; fz++) {
                setBlock(world, x + fx, y, z + fz, Material.COBBLESTONE);
            }
        }

        // ── Walls — 8 blocks tall, crumbled ───────────────────────────────
        int maxHeight = 6 + random.nextInt(3); // 6-8 blocks
        for (int wy = 1; wy <= maxHeight; wy++) {
            // Crumble probability increases with height
            int crumbleChance = (wy > 4) ? 4 : 8; // 1-in-N chance of missing block
            for (int wx = 0; wx < 5; wx++) {
                if (random.nextInt(crumbleChance) != 0) {
                    Material mat = random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE;
                    setBlock(world, x + wx, y + wy, z,     mat);
                    setBlock(world, x + wx, y + wy, z + 4, mat);
                }
            }
            for (int wz = 1; wz < 4; wz++) {
                if (random.nextInt(crumbleChance) != 0) {
                    Material mat = random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE;
                    setBlock(world, x,     y + wy, z + wz, mat);
                    setBlock(world, x + 4, y + wy, z + wz, mat);
                }
            }
        }

        // ── Corner pillars — always intact ────────────────────────────────
        for (int wy = 1; wy <= maxHeight; wy++) {
            setBlock(world, x,     y + wy, z,     Material.STONE_BRICKS);
            setBlock(world, x + 4, y + wy, z,     Material.STONE_BRICKS);
            setBlock(world, x,     y + wy, z + 4, Material.STONE_BRICKS);
            setBlock(world, x + 4, y + wy, z + 4, Material.STONE_BRICKS);
        }

        // ── Interior — clear air + floor at midpoint ───────────────────────
        for (int iy = 1; iy <= maxHeight; iy++) {
            for (int ix = 1; ix <= 3; ix++) {
                for (int iz = 1; iz <= 3; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // Mid-floor at ~half height
        int midFloor = maxHeight / 2;
        for (int ix = 1; ix <= 3; ix++) {
            for (int iz = 1; iz <= 3; iz++) {
                setBlock(world, x + ix, y + midFloor, z + iz, Material.OAK_PLANKS);
            }
        }

        // ── Entrance (south wall, bottom) ──────────────────────────────────
        setBlock(world, x + 2, y + 1, z + 4, Material.AIR);
        setBlock(world, x + 2, y + 2, z + 4, Material.AIR);

        // ── Torch inside ──────────────────────────────────────────────────
        setBlock(world, x + 1, y + 2, z + 1, Material.WALL_TORCH);

        // ── Loot chest on mid-floor ───────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 2, y + midFloor + 1, z + 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillTowerChest(chest, random);
        }
    }

    private void fillTowerChest(Chest chest, Random random) {
        int rolls = 2 + random.nextInt(4);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.ARROW,             10, 4, 12),
            lootEntry(Material.BOW,                6, 1, 1),
            lootEntry(Material.IRON_SWORD,         5, 1, 1),
            lootEntry(Material.IRON_HELMET,        3, 1, 1),
            lootEntry(Material.IRON_CHESTPLATE,    2, 1, 1),
            lootEntry(Material.GOLD_INGOT,         7, 1, 3),
            lootEntry(Material.IRON_INGOT,         8, 1, 3),
            lootEntry(Material.COOKED_BEEF,        9, 2, 4),
            lootEntry(Material.DIAMOND,            1, 1, 1),
            lootEntry(Material.COBBLESTONE,        7, 4, 8),
            lootEntry(Material.TORCH,              8, 3, 6)
        );
        placeInChest(chest, loot);
    }
}
