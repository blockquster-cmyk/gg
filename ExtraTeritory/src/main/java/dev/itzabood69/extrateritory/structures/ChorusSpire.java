package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Chorus Spire — a tall narrow end stone tower draped in chorus plants.
 * Size: 3x10x3 central tower, chorus plant vines on outside.
 * Materials: end stone bricks, purpur pillar, chorus plant, end rods
 * Loot: chorus fruit, ender pearls, purpur blocks
 *
 * @author ItzAbood69
 */
public class ChorusSpire extends BaseStructure {

    public ChorusSpire(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 75) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Tower (3x3, 9 tall) ───────────────────────────────────────────
        for (int wy = 1; wy <= 9; wy++) {
            for (int wx = 0; wx < 3; wx++) {
                for (int wz = 0; wz < 3; wz++) {
                    boolean isEdge = (wx == 0 || wx == 2 || wz == 0 || wz == 2);
                    if (isEdge) {
                        Material mat = (wy % 3 == 0) ? Material.PURPUR_PILLAR : Material.END_STONE_BRICKS;
                        setBlock(world, x + wx, y + wy, z + wz, mat);
                    } else {
                        setBlock(world, x + wx, y + wy, z + wz, Material.AIR);
                    }
                }
            }
        }

        // ── Tip: end rods ──────────────────────────────────────────────────
        setBlock(world, x + 1, y + 10, z + 1, Material.PURPUR_BLOCK);
        setBlock(world, x + 1, y + 11, z + 1, Material.END_ROD);

        // ── Chorus plants climbing up the sides ────────────────────────────
        int[] sidesX = {-1, 3, -1,  3};
        int[] sidesZ = { 1, 1,  2,  2};
        for (int i = 0; i < sidesX.length; i++) {
            int sx = x + sidesX[i];
            int sz = z + sidesZ[i];
            int sy = getSurfaceY(world, sx, sz);
            if (sy < 0) continue;
            int height = 2 + random.nextInt(5);
            for (int h = 1; h <= height; h++) {
                Block b = world.getBlockAt(sx, sy + h, sz);
                if (b.getType() == Material.AIR) {
                    b.setType(h == height ? Material.CHORUS_FLOWER : Material.CHORUS_PLANT);
                }
            }
        }

        // ── Base decoration: end stone bricks ring ─────────────────────────
        for (int bx = -1; bx <= 3; bx++) {
            setBlock(world, x + bx, y, z - 1, Material.END_STONE_BRICKS);
            setBlock(world, x + bx, y, z + 3, Material.END_STONE_BRICKS);
        }
        for (int bz = 0; bz < 3; bz++) {
            setBlock(world, x - 1, y, z + bz, Material.END_STONE_BRICKS);
            setBlock(world, x + 3, y, z + bz, Material.END_STONE_BRICKS);
        }

        // ── Entrance (south) ──────────────────────────────────────────────
        setBlock(world, x + 1, y + 1, z + 2, Material.AIR);
        setBlock(world, x + 1, y + 2, z + 2, Material.AIR);

        // ── Loot chest at top floor ────────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 1, y + 8, z + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillSpireChest(chest, random);
        }
    }

    private void fillSpireChest(Chest chest, Random random) {
        int rolls = 2 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.CHORUS_FRUIT,      10, 3, 7),
            lootEntry(Material.CHORUS_FLOWER,      7, 1, 2),
            lootEntry(Material.ENDER_PEARL,        9, 2, 5),
            lootEntry(Material.PURPUR_BLOCK,       8, 3, 6),
            lootEntry(Material.END_STONE_BRICKS,   7, 2, 5),
            lootEntry(Material.END_ROD,            5, 1, 3),
            lootEntry(Material.DIAMOND,            2, 1, 1),
            lootEntry(Material.IRON_INGOT,         7, 1, 4),
            lootEntry(Material.ENDER_EYE,          3, 1, 2),
            lootEntry(Material.SHULKER_SHELL,      2, 1, 1)
        );
        placeInChest(chest, loot);
    }
}
