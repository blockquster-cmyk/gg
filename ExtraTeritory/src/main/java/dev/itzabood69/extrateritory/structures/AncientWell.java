package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Ancient Well — a mossy cobblestone well with a bucket of water inside.
 * Size: 5x5 base, 4 tall, open top with water bucket chest.
 * Materials: mossy cobblestone, cobblestone, stone bricks, water
 * Loot: water buckets, fishing rods, raw fish, old tools
 *
 * @author ItzAbood69
 */
public class AncientWell extends BaseStructure {

    public AncientWell(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 58 || y > 82) return;

        // ── Base ring (5x5 hollow square) ─────────────────────────────────
        for (int px = 0; px < 5; px++) {
            for (int pz = 0; pz < 5; pz++) {
                boolean edge = (px == 0 || px == 4 || pz == 0 || pz == 4);
                if (edge) {
                    setBlock(world, x + px, y, z + pz, Material.MOSSY_COBBLESTONE);
                }
            }
        }

        // ── Walls (3 high) ─────────────────────────────────────────────────
        for (int wy = 1; wy <= 3; wy++) {
            for (int wx = 0; wx < 5; wx++) {
                setBlock(world, x + wx, y + wy, z,     Material.MOSSY_COBBLESTONE);
                setBlock(world, x + wx, y + wy, z + 4, Material.MOSSY_COBBLESTONE);
            }
            for (int wz = 1; wz < 4; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.MOSSY_COBBLESTONE);
                setBlock(world, x + 4, y + wy, z + wz, Material.MOSSY_COBBLESTONE);
            }
        }

        // ── Corner pillars: stone bricks ─────────────────────────────────
        for (int wy = 1; wy <= 4; wy++) {
            setBlock(world, x,     y + wy, z,     Material.STONE_BRICKS);
            setBlock(world, x + 4, y + wy, z,     Material.STONE_BRICKS);
            setBlock(world, x,     y + wy, z + 4, Material.STONE_BRICKS);
            setBlock(world, x + 4, y + wy, z + 4, Material.STONE_BRICKS);
        }

        // ── Interior: clear + water at bottom ─────────────────────────────
        for (int iy = 1; iy <= 3; iy++) {
            for (int ix = 1; ix <= 3; ix++) {
                for (int iz = 1; iz <= 3; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }
        // Water source blocks at floor of well
        for (int ix = 1; ix <= 3; ix++) {
            for (int iz = 1; iz <= 3; iz++) {
                setBlock(world, x + ix, y, z + iz, Material.WATER);
            }
        }

        // ── Crossbeam on top (sticks over the well) ───────────────────────
        setBlock(world, x + 2, y + 4, z,     Material.OAK_FENCE);
        setBlock(world, x + 2, y + 4, z + 1, Material.OAK_FENCE);
        setBlock(world, x + 2, y + 4, z + 2, Material.OAK_FENCE);
        setBlock(world, x + 2, y + 4, z + 3, Material.OAK_FENCE);
        setBlock(world, x + 2, y + 4, z + 4, Material.OAK_FENCE);
        setBlock(world, x + 2, y + 5, z + 2, Material.OAK_LOG); // center post

        // ── Loot chest on the edge ─────────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 1, y + 1, z + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillWellChest(chest, random);
        }
    }

    private void fillWellChest(Chest chest, Random random) {
        int rolls = 2 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.WATER_BUCKET,     8, 1, 1),
            lootEntry(Material.FISHING_ROD,      7, 1, 1),
                lootEntry(Material.COD,    10, 2, 4),   // كان RAW_COD
                lootEntry(Material.SALMON,  8, 1, 3),   // كان RAW_SALMON      8, 1, 3),
            lootEntry(Material.LILY_PAD,         9, 1, 3),
            lootEntry(Material.IRON_INGOT,       5, 1, 2),
            lootEntry(Material.MOSSY_COBBLESTONE,6, 2, 4),
            lootEntry(Material.STONE_BRICKS,     6, 2, 4),
            lootEntry(Material.DIAMOND,          1, 1, 1),
            lootEntry(Material.BREAD,            8, 1, 3)
        );
        placeInChest(chest, loot);
    }
}
