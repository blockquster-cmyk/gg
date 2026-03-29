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

/** Void Ruin — large crumbled End tower with nether/overworld accents. Size: 12x10x12 */
public class VoidRuin extends BaseStructure {

    public VoidRuin(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 50 || y > 80) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Foundation 12x12 ─────────────────────────────────────────────
        for (int px = 0; px < 12; px++)
            for (int pz = 0; pz < 12; pz++)
                setBlock(world, x + px, y, z + pz, Material.END_STONE_BRICKS);

        // ── Tower walls — crumbled (random gaps) ──────────────────────────
        for (int wy = 1; wy <= 8; wy++) {
            for (int wx = 0; wx < 12; wx++) {
                if (random.nextInt(5) != 0) {
                    setBlock(world, x + wx, y + wy, z,      Material.PURPUR_BLOCK);
                    setBlock(world, x + wx, y + wy, z + 11, Material.PURPUR_BLOCK);
                }
            }
            for (int wz = 1; wz < 11; wz++) {
                if (random.nextInt(5) != 0) {
                    setBlock(world, x,      y + wy, z + wz, Material.PURPUR_BLOCK);
                    setBlock(world, x + 11, y + wy, z + wz, Material.PURPUR_BLOCK);
                }
            }
        }

        // ── Corner purpur pillars — always solid 10 high ──────────────────
        for (int wy = 1; wy <= 10; wy++) {
            setBlock(world, x,      y + wy, z,      Material.PURPUR_PILLAR);
            setBlock(world, x + 11, y + wy, z,      Material.PURPUR_PILLAR);
            setBlock(world, x,      y + wy, z + 11, Material.PURPUR_PILLAR);
            setBlock(world, x + 11, y + wy, z + 11, Material.PURPUR_PILLAR);
        }

        // ── Inner obsidian columns — nether block in End! ─────────────────
        int[][] innerCol = {{3,3},{8,3},{3,8},{8,8}};
        for (int[] c : innerCol)
            for (int h = 1; h <= 6; h++)
                setBlock(world, x + c[0], y + h, z + c[1], Material.OBSIDIAN);

        // ── Partial roof ──────────────────────────────────────────────────
        for (int rx = 1; rx < 11; rx++)
            for (int rz = 1; rz < 11; rz++)
                if (random.nextInt(3) != 0)
                    setBlock(world, x + rx, y + 9, z + rz, Material.PURPUR_SLAB);

        // End rods on corner tips
        for (int[] c : new int[][]{{0,0},{11,0},{0,11},{11,11}}) {
            setBlock(world, x + c[0], y + 11, z + c[1], Material.END_ROD);
            setBlock(world, x + c[0], y + 10, z + c[1], Material.END_ROD);
        }

        // ── Interior clear ────────────────────────────────────────────────
        for (int iy = 1; iy <= 8; iy++)
            for (int ix = 1; ix <= 10; ix++)
                for (int iz = 1; iz <= 10; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Pedestal center ────────────────────────────────────────────────
        setBlock(world, x + 5, y + 1, z + 5, Material.END_STONE_BRICKS);
        setBlock(world, x + 5, y + 2, z + 5, Material.END_ROD);
        setBlock(world, x + 6, y + 1, z + 6, Material.END_STONE_BRICKS);
        setBlock(world, x + 6, y + 2, z + 6, Material.END_ROD);

        // ── Entrance gap ──────────────────────────────────────────────────
        for (int gap = 4; gap <= 7; gap++) {
            setBlock(world, x + gap, y + 1, z + 11, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 11, Material.AIR);
            setBlock(world, x + gap, y + 3, z + 11, Material.AIR);
        }

        // ── 3 Loot chests ─────────────────────────────────────────────────
        placeChest(world, x + 1,  y + 1, z + 1,  random);
        placeChest(world, x + 10, y + 1, z + 1,  random);
        placeChest(world, x + 5,  y + 1, z + 10, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(4);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.ENDER_PEARL,      10, 2, 5),
                    lootEntry(Material.CHORUS_FRUIT,      8, 3, 7),
                    lootEntry(Material.PURPUR_BLOCK,      7, 4, 8),
                    lootEntry(Material.END_STONE_BRICKS,  6, 4, 8),
                    lootEntry(Material.DIAMOND,           3, 1, 2),
                    lootEntry(Material.IRON_INGOT,        8, 2, 6),
                    lootEntry(Material.GOLD_INGOT,        5, 2, 4),
                    lootEntry(Material.SHULKER_SHELL,     4, 1, 2),
                    lootEntry(Material.ENDER_EYE,         4, 1, 3),
                    lootEntry(Material.TORCH,             6, 5, 10),
                    lootEntry(Material.ELYTRA,            1, 1, 1),
                    lootEntry(Material.NETHERITE_SCRAP,   1, 1, 1)
            );
            placeInChest(chest, loot);

            ItemStack pot = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) pot.getItemMeta();
            if (meta != null) { meta.setBasePotionType(PotionType.SLOW_FALLING); pot.setItemMeta(meta); }
            for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
                if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
            }
            chest.update(true);
        }
    }
}