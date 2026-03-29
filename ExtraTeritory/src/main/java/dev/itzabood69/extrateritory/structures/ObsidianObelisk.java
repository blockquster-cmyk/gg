package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Obsidian Obelisk — a tall imposing obsidian needle rising from the end.
 * Size: tapers from 5x5 base to 1x1 tip, 14 blocks tall total.
 * Materials: obsidian, crying obsidian, end rods, end crystals
 * Loot: obsidian, rare enchanted books, diamonds, end crystals
 *
 * @author ItzAbood69
 */
public class ObsidianObelisk extends BaseStructure {

    public ObsidianObelisk(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 72) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Tapering layers ────────────────────────────────────────────────
        // Layer 1-2: 5x5 base
        for (int wy = 1; wy <= 2; wy++) {
            for (int wx = 0; wx < 5; wx++) {
                for (int wz = 0; wz < 5; wz++) {
                    boolean isEdge = (wx == 0 || wx == 4 || wz == 0 || wz == 4);
                    Material mat = isEdge ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN;
                    setBlock(world, x + wx, y + wy, z + wz, mat);
                }
            }
        }

        // Layer 3-5: 3x3
        for (int wy = 3; wy <= 5; wy++) {
            for (int wx = 1; wx < 4; wx++) {
                for (int wz = 1; wz < 4; wz++) {
                    Material mat = (wx == 1 || wx == 3 || wz == 1 || wz == 3)
                        ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN;
                    setBlock(world, x + wx, y + wy, z + wz, mat);
                }
            }
        }

        // Layer 6-12: 1x1 needle
        for (int wy = 6; wy <= 12; wy++) {
            setBlock(world, x + 2, y + wy, z + 2, Material.OBSIDIAN);
        }

        // Tip: end rod + crying obsidian glow
        setBlock(world, x + 2, y + 13, z + 2, Material.CRYING_OBSIDIAN);
        setBlock(world, x + 2, y + 14, z + 2, Material.END_ROD);

        // ── Crying obsidian accent blocks on base corners ──────────────────
        for (int[] corner : new int[][]{{0,0},{4,0},{0,4},{4,4}}) {
            setBlock(world, x + corner[0], y, z + corner[1], Material.CRYING_OBSIDIAN);
            setBlock(world, x + corner[0], y + 1, z + corner[1], Material.CRYING_OBSIDIAN);
        }

        // ── Base platform ring ──────────────────────────────────────────────
        for (int px = -1; px <= 5; px++) {
            setBlock(world, x + px, y, z - 1, Material.END_STONE_BRICKS);
            setBlock(world, x + px, y, z + 5, Material.END_STONE_BRICKS);
        }
        for (int pz = 0; pz < 5; pz++) {
            setBlock(world, x - 1, y, z + pz, Material.END_STONE_BRICKS);
            setBlock(world, x + 5, y, z + pz, Material.END_STONE_BRICKS);
        }

        // ── Buried loot chest (dig under base) ────────────────────────────
        Block chestBlock = world.getBlockAt(x + 2, y - 1, z + 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillObeliskChest(chest, random);
        }
    }

    private void fillObeliskChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.OBSIDIAN,          9, 4, 8),
            lootEntry(Material.CRYING_OBSIDIAN,   7, 2, 5),
            lootEntry(Material.DIAMOND,           3, 1, 2),
            lootEntry(Material.ENDER_PEARL,       8, 2, 4),
            lootEntry(Material.ENDER_EYE,         5, 1, 3),
            lootEntry(Material.ENCHANTED_BOOK,    2, 1, 1),
            lootEntry(Material.IRON_INGOT,        7, 2, 5),
            lootEntry(Material.GOLD_INGOT,        5, 1, 3),
            lootEntry(Material.END_STONE_BRICKS,  8, 3, 6),
            lootEntry(Material.NETHERITE_SCRAP,   1, 1, 1)
        );
        placeInChest(chest, loot);
    }
}
