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
 * Blaze Altar — large multi-tier nether altar with overworld/end accents.
 * Size: 13x10x13
 */
public class BlazeAltar extends BaseStructure {

    public BlazeAltar(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Base platform 13x13 ───────────────────────────────────────────
        for (int px = 0; px < 13; px++)
            for (int pz = 0; pz < 13; pz++)
                setBlock(world, x + px, y, z + pz, Material.NETHER_BRICKS);

        // ── Outer walls 6 high ────────────────────────────────────────────
        for (int wy = 1; wy <= 6; wy++) {
            for (int wx = 0; wx < 13; wx++) {
                setBlock(world, x + wx, y + wy, z,      Material.NETHER_BRICKS);
                setBlock(world, x + wx, y + wy, z + 12, Material.NETHER_BRICKS);
            }
            for (int wz = 1; wz < 12; wz++) {
                setBlock(world, x,      y + wy, z + wz, Material.NETHER_BRICKS);
                setBlock(world, x + 12, y + wy, z + wz, Material.NETHER_BRICKS);
            }
        }

        // ── Corner towers 8 high (obsidian — overworld block!) ────────────
        for (int wy = 1; wy <= 8; wy++) {
            for (int[] c : new int[][]{{0,0},{12,0},{0,12},{12,12}}) {
                setBlock(world, x + c[0], y + wy, z + c[1], Material.OBSIDIAN);
            }
        }

        // ── Inner platform raised (step up) ───────────────────────────────
        for (int px = 3; px < 10; px++)
            for (int pz = 3; pz < 10; pz++)
                setBlock(world, x + px, y + 1, z + pz, Material.SMOOTH_STONE); // overworld!

        // ── Central magma column ──────────────────────────────────────────
        for (int h = 2; h <= 5; h++)
            setBlock(world, x + 6, y + h, z + 6, Material.MAGMA_BLOCK);
        setBlock(world, x + 6, y + 6, z + 6, Material.FIRE);
        // End rod spire on top — cross-dimension
        setBlock(world, x + 6, y + 7, z + 6, Material.END_ROD);
        setBlock(world, x + 6, y + 8, z + 6, Material.END_ROD);

        // ── Purpur pillar ring — End block in Nether! ─────────────────────
        int[][] pillarPos = {{3,3},{9,3},{3,9},{9,9}};
        for (int[] p : pillarPos) {
            for (int h = 2; h <= 5; h++)
                setBlock(world, x + p[0], y + h, z + p[1], Material.PURPUR_PILLAR);
            setBlock(world, x + p[0], y + 6, z + p[1], Material.SOUL_LANTERN);
        }

        // ── Interior clear ────────────────────────────────────────────────
        for (int iy = 2; iy <= 6; iy++)
            for (int ix = 1; ix <= 11; ix++)
                for (int iz = 1; iz <= 11; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Entrance 3 wide ───────────────────────────────────────────────
        for (int gap = 5; gap <= 7; gap++) {
            setBlock(world, x + gap, y + 1, z + 12, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 12, Material.AIR);
            setBlock(world, x + gap, y + 3, z + 12, Material.AIR);
        }

        // ── Wall lanterns ─────────────────────────────────────────────────
        setBlock(world, x + 2, y + 5, z + 2,   Material.SOUL_LANTERN);
        setBlock(world, x + 10, y + 5, z + 2,  Material.SOUL_LANTERN);
        setBlock(world, x + 2, y + 5, z + 10,  Material.SOUL_LANTERN);
        setBlock(world, x + 10, y + 5, z + 10, Material.SOUL_LANTERN);

        // ── Loot chests ───────────────────────────────────────────────────
        placeChest(world, x + 1, y + 1, z + 1, random);
        placeChest(world, x + 11, y + 1, z + 11, random);
        placeChest(world, x + 1, y + 1, z + 11, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(4);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.BLAZE_ROD,          9, 3, 6),
                    lootEntry(Material.BLAZE_POWDER,        8, 3, 7),
                    lootEntry(Material.NETHER_WART,        10, 4, 10),
                    lootEntry(Material.MAGMA_CREAM,         7, 2, 5),
                    lootEntry(Material.GOLD_INGOT,          8, 3, 6),
                    lootEntry(Material.GOLD_BLOCK,          2, 1, 1),
                    lootEntry(Material.QUARTZ,              9, 4, 8),
                    lootEntry(Material.FIRE_CHARGE,         5, 2, 4),
                    lootEntry(Material.FLINT_AND_STEEL,     5, 1, 1),
                    lootEntry(Material.DIAMOND,             2, 1, 2),
                    lootEntry(Material.IRON_INGOT,          8, 2, 5),
                    lootEntry(Material.NETHERITE_SCRAP,     1, 1, 1),
                    lootEntry(Material.OBSIDIAN,            6, 3, 6)
            );
            placeInChest(chest, loot);

            ItemStack pot = new ItemStack(Material.POTION);
            PotionMeta m = (PotionMeta) pot.getItemMeta();
            if (m != null) { m.setBasePotionType(PotionType.STRENGTH); pot.setItemMeta(m); }
            for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
                if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
            }
            chest.update(true);
        }
    }
}