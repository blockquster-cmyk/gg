package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Random;

/**
 * Druid Stone Circle — a ring of mossy stone pillars around a central altar.
 * Size: 11x11 footprint, pillars up to 4 blocks tall.
 * Materials: mossy stone bricks, stone, flowering azalea
 * Loot: potions, seeds, bones, rare enchanted book
 *
 * @author ItzAbood69
 */
public class DruidStoneCircle extends BaseStructure {

    public DruidStoneCircle(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 60 || y > 80) return;

        // ── Ring of pillars (8 pillars on a circle of radius 4) ────────────
        int[][] pillarOffsets = {
            {4, 0}, {-4, 0}, {0, 4}, {0, -4},
            {3, 3}, {-3, 3}, {3, -3}, {-3, -3}
        };

        int cx = x + 5; // center
        int cz = z + 5;

        for (int[] off : pillarOffsets) {
            int px = cx + off[0];
            int pz = cz + off[1];
            int py = getSurfaceY(world, px, pz);
            if (py < 0) continue;
            int height = 2 + random.nextInt(3); // 2-4 blocks
            for (int h = 1; h <= height; h++) {
                Material mat = (h == height)
                    ? Material.MOSSY_STONE_BRICKS
                    : (random.nextBoolean() ? Material.STONE_BRICKS : Material.MOSSY_STONE_BRICKS);
                setBlock(world, px, py + h, pz, mat);
            }
            // Flowering azalea on some tops
            if (random.nextInt(3) == 0) {
                setBlock(world, px, py + (2 + random.nextInt(3)) + 1, pz, Material.FLOWERING_AZALEA);
            }
        }

        // ── Central altar: 3x3 platform + pillar ──────────────────────────
        int ay = getSurfaceY(world, cx, cz);
        if (ay < 0) return;

        for (int ax = -1; ax <= 1; ax++) {
            for (int az = -1; az <= 1; az++) {
                setBlock(world, cx + ax, ay + 1, cz + az, Material.MOSSY_STONE_BRICKS);
            }
        }
        setBlock(world, cx, ay + 2, cz, Material.CHISELED_STONE_BRICKS);
        setBlock(world, cx, ay + 3, cz, Material.STONE_BRICKS);

        // Flowers around base
        Material[] flowers = { Material.DANDELION, Material.POPPY, Material.ALLIUM, Material.OXEYE_DAISY };
        for (int ax = -2; ax <= 2; ax++) {
            for (int az = -2; az <= 2; az++) {
                if (ax == 0 && az == 0) continue;
                int sy = getSurfaceY(world, cx + ax, cz + az);
                if (sy < 0) continue;
                Block above = world.getBlockAt(cx + ax, sy + 1, cz + az);
                if (above.getType() == Material.AIR && random.nextInt(3) == 0) {
                    above.setType(flowers[random.nextInt(flowers.length)]);
                }
            }
        }

        // ── Loot chest buried at altar base ───────────────────────────────
        Block chestBlock = world.getBlockAt(cx + 1, ay + 2, cz + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillAltarChest(chest, random);
        }
    }

    private void fillAltarChest(Chest chest, Random random) {
        int rolls = 2 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.BONE,             10, 2, 5),
            lootEntry(Material.WHEAT_SEEDS,       9, 3, 8),
            lootEntry(Material.BEETROOT_SEEDS,    7, 2, 6),
            lootEntry(Material.PUMPKIN_SEEDS,     7, 2, 5),
            lootEntry(Material.MOSS_BLOCK,        8, 2, 5),
            lootEntry(Material.FLOWERING_AZALEA,  6, 1, 2),
            lootEntry(Material.ENCHANTED_BOOK,    1, 1, 1),
            lootEntry(Material.IRON_HOE,          5, 1, 1),
            lootEntry(Material.DIAMOND,           1, 1, 1),
            lootEntry(Material.EMERALD,           3, 1, 2)
        );
        placeInChest(chest, loot);

        // Healing potion — always add one
        ItemStack healPotion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) healPotion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.HEALING);
            healPotion.setItemMeta(meta);
        }
        for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
            if (chest.getInventory().getItem(slot) == null) {
                chest.getInventory().setItem(slot, healPotion);
                break;
            }
        }
        chest.update();
    }
}
