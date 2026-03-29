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

/** Soul Crypt — large sunken crypt with cross-dimension materials. Size: 12x6x12 */
public class SoulCrypt extends BaseStructure {

    public SoulCrypt(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Platform 12x12 ───────────────────────────────────────────────
        for (int px = 0; px < 12; px++)
            for (int pz = 0; pz < 12; pz++) {
                setBlock(world, x + px, y, z + pz, Material.SOUL_SOIL);
                setBlock(world, x + px, y - 1, z + pz, Material.SOUL_SAND);
            }

        // ── Walls (soul sand + bone blocks + purpur — cross-dim!) ─────────
        for (int wy = 1; wy <= 5; wy++) {
            for (int wx = 0; wx < 12; wx++) {
                Material mat = (wx % 3 == 0) ? Material.BONE_BLOCK :
                        (wx % 3 == 1) ? Material.SOUL_SAND : Material.PURPUR_BLOCK;
                setBlock(world, x + wx, y + wy, z,      mat);
                setBlock(world, x + wx, y + wy, z + 11, mat);
            }
            for (int wz = 1; wz < 11; wz++) {
                Material mat = (wz % 3 == 0) ? Material.BONE_BLOCK :
                        (wz % 3 == 1) ? Material.SOUL_SAND : Material.PURPUR_BLOCK;
                setBlock(world, x,      y + wy, z + wz, mat);
                setBlock(world, x + 11, y + wy, z + wz, mat);
            }
        }

        // ── Corner bone pillars 7 high ────────────────────────────────────
        for (int wy = 1; wy <= 7; wy++) {
            setBlock(world, x,      y + wy, z,      Material.BONE_BLOCK);
            setBlock(world, x + 11, y + wy, z,      Material.BONE_BLOCK);
            setBlock(world, x,      y + wy, z + 11, Material.BONE_BLOCK);
            setBlock(world, x + 11, y + wy, z + 11, Material.BONE_BLOCK);
        }

        // ── Roof ─────────────────────────────────────────────────────────
        for (int rx = 0; rx < 12; rx++)
            for (int rz = 0; rz < 12; rz++)
                setBlock(world, x + rx, y + 6, z + rz, Material.SOUL_SOIL);

        // ── Interior clear ─────────────────────────────────────────────────
        for (int iy = 1; iy <= 5; iy++)
            for (int ix = 1; ix <= 10; ix++)
                for (int iz = 1; iz <= 10; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Soul fire pillars ──────────────────────────────────────────────
        int[][] fires = {{2,2},{9,2},{2,9},{9,9},{5,5}};
        for (int[] f : fires) {
            setBlock(world, x + f[0], y + 1, z + f[1], Material.SOUL_SAND);
            setBlock(world, x + f[0], y + 2, z + f[1], Material.SOUL_FIRE);
        }

        // ── Hanging soul lanterns ──────────────────────────────────────────
        setBlock(world, x + 5, y + 5, z + 5,  Material.SOUL_LANTERN);
        setBlock(world, x + 2, y + 5, z + 9,  Material.SOUL_LANTERN);
        setBlock(world, x + 9, y + 5, z + 2,  Material.SOUL_LANTERN);

        // ── End rod accents ────────────────────────────────────────────────
        setBlock(world, x + 3, y + 4, z + 3,  Material.END_ROD);
        setBlock(world, x + 8, y + 4, z + 8,  Material.END_ROD);

        // ── Entrance ──────────────────────────────────────────────────────
        for (int gap = 5; gap <= 6; gap++) {
            setBlock(world, x + gap, y + 1, z + 11, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 11, Material.AIR);
        }

        // ── 3 Loot chests ─────────────────────────────────────────────────
        placeChest(world, x + 1, y + 1, z + 1, random);
        placeChest(world, x + 10, y + 1, z + 10, random);
        placeChest(world, x + 1, y + 1, z + 10, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(4);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.BONE,             10, 4, 10),
                    lootEntry(Material.BONE_BLOCK,        7, 2, 5),
                    lootEntry(Material.BONE_MEAL,         9, 4, 10),
                    lootEntry(Material.SOUL_SAND,         8, 3, 7),
                    lootEntry(Material.SOUL_SOIL,         7, 2, 5),
                    lootEntry(Material.COAL,              9, 4, 10),
                    lootEntry(Material.GOLD_INGOT,        5, 2, 4),
                    lootEntry(Material.IRON_INGOT,        6, 2, 5),
                    lootEntry(Material.DIAMOND,           2, 1, 1),
                    lootEntry(Material.GHAST_TEAR,        3, 1, 3),
                    lootEntry(Material.NETHERITE_SCRAP,   1, 1, 1),
                    lootEntry(Material.ENCHANTED_BOOK,    2, 1, 1)
            );
            placeInChest(chest, loot);

            ItemStack pot = new ItemStack(Material.POTION);
            PotionMeta m = (PotionMeta) pot.getItemMeta();
            if (m != null) { m.setBasePotionType(PotionType.REGENERATION); pot.setItemMeta(m); }
            for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
                if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
            }
            chest.update(true);
        }
    }
}