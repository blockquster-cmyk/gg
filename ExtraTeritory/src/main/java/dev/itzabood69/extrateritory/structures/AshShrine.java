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
 * Ash Shrine — large nether shrine with overworld/end accent blocks.
 * Size: 11x9x11  — much bigger, cross-dimension materials.
 */
public class AshShrine extends BaseStructure {

    public AshShrine(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Wide platform 11x11 ───────────────────────────────────────────
        for (int px = 0; px < 11; px++)
            for (int pz = 0; pz < 11; pz++) {
                Material m = ((px + pz) % 2 == 0) ? Material.NETHER_BRICKS : Material.BLACKSTONE;
                setBlock(world, x + px, y, z + pz, m);
            }

        // ── Outer walls 5 high ────────────────────────────────────────────
        for (int wy = 1; wy <= 5; wy++) {
            for (int wx = 0; wx < 11; wx++) {
                setBlock(world, x + wx, y + wy, z,      Material.NETHER_BRICKS);
                setBlock(world, x + wx, y + wy, z + 10, Material.NETHER_BRICKS);
            }
            for (int wz = 1; wz < 10; wz++) {
                setBlock(world, x,      y + wy, z + wz, Material.NETHER_BRICKS);
                setBlock(world, x + 10, y + wy, z + wz, Material.NETHER_BRICKS);
            }
        }

        // ── Corner pillars (chiseled + polished blackstone — cross-dimension) ──
        for (int wy = 1; wy <= 7; wy++) {
            Material mat = (wy % 2 == 0) ? Material.CHISELED_NETHER_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS;
            setBlock(world, x,      y + wy, z,      mat);
            setBlock(world, x + 10, y + wy, z,      mat);
            setBlock(world, x,      y + wy, z + 10, mat);
            setBlock(world, x + 10, y + wy, z + 10, mat);
        }

        // ── Inner wall ring (stone bricks — overworld block in nether!) ───
        for (int wy = 1; wy <= 3; wy++) {
            for (int wx = 2; wx < 9; wx++) {
                setBlock(world, x + wx, y + wy, z + 2,  Material.STONE_BRICKS);
                setBlock(world, x + wx, y + wy, z + 8,  Material.STONE_BRICKS);
            }
            for (int wz = 3; wz < 8; wz++) {
                setBlock(world, x + 2, y + wy, z + wz, Material.STONE_BRICKS);
                setBlock(world, x + 8, y + wy, z + wz, Material.STONE_BRICKS);
            }
        }

        // ── Roof: nether brick slab + soul lantern corners ────────────────
        for (int rx = 0; rx < 11; rx++)
            for (int rz = 0; rz < 11; rz++)
                setBlock(world, x + rx, y + 6, z + rz, Material.NETHER_BRICK_SLAB);

        setBlock(world, x,      y + 7, z,      Material.SOUL_LANTERN);
        setBlock(world, x + 10, y + 7, z,      Material.SOUL_LANTERN);
        setBlock(world, x,      y + 7, z + 10, Material.SOUL_LANTERN);
        setBlock(world, x + 10, y + 7, z + 10, Material.SOUL_LANTERN);
        // End rods on top — cross-dimension accent
        setBlock(world, x + 5, y + 7, z + 5, Material.END_ROD);
        setBlock(world, x + 3, y + 7, z + 3, Material.END_ROD);
        setBlock(world, x + 7, y + 7, z + 7, Material.END_ROD);

        // ── Entrance (south wall, wide) ───────────────────────────────────
        for (int gap = 4; gap <= 6; gap++) {
            setBlock(world, x + gap, y + 1, z + 10, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 10, Material.AIR);
            setBlock(world, x + gap, y + 3, z + 10, Material.AIR);
        }
        setBlock(world, x + 3, y + 1, z + 10, Material.NETHER_BRICK_FENCE);
        setBlock(world, x + 7, y + 1, z + 10, Material.NETHER_BRICK_FENCE);

        // ── Interior ──────────────────────────────────────────────────────
        for (int iy = 1; iy <= 5; iy++)
            for (int ix = 1; ix <= 9; ix++)
                for (int iz = 1; iz <= 9; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // Soul fire altar
        setBlock(world, x + 5, y + 1, z + 5, Material.SOUL_SAND);
        setBlock(world, x + 5, y + 2, z + 5, Material.SOUL_FIRE);
        setBlock(world, x + 4, y + 1, z + 5, Material.SOUL_SAND);
        setBlock(world, x + 6, y + 1, z + 5, Material.SOUL_SAND);
        setBlock(world, x + 5, y + 1, z + 4, Material.SOUL_SAND);
        setBlock(world, x + 5, y + 1, z + 6, Material.SOUL_SAND);

        // Purpur pillar decoration — End block in Nether!
        setBlock(world, x + 2, y + 1, z + 2, Material.PURPUR_PILLAR);
        setBlock(world, x + 8, y + 1, z + 2, Material.PURPUR_PILLAR);
        setBlock(world, x + 2, y + 1, z + 8, Material.PURPUR_PILLAR);
        setBlock(world, x + 8, y + 1, z + 8, Material.PURPUR_PILLAR);

        // ── 2 Loot chests ─────────────────────────────────────────────────
        placeChest(world, x + 1, y + 1, z + 1, random);
        placeChest(world, x + 9, y + 1, z + 9, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(4);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.COOKED_PORKCHOP,    10, 3, 6),
                    lootEntry(Material.GOLDEN_APPLE,         3, 1, 2),
                    lootEntry(Material.QUARTZ,              10, 4, 8),
                    lootEntry(Material.GOLD_INGOT,           9, 2, 5),
                    lootEntry(Material.GOLD_BLOCK,           2, 1, 1),
                    lootEntry(Material.BLAZE_ROD,            6, 1, 3),
                    lootEntry(Material.MAGMA_CREAM,          7, 2, 4),
                    lootEntry(Material.GOLDEN_SWORD,         5, 1, 1),
                    lootEntry(Material.GOLDEN_CHESTPLATE,    3, 1, 1),
                    lootEntry(Material.FLINT_AND_STEEL,      6, 1, 1),
                    lootEntry(Material.OBSIDIAN,             5, 2, 5),
                    lootEntry(Material.DIAMOND,              2, 1, 1),
                    lootEntry(Material.IRON_INGOT,           8, 2, 5),
                    lootEntry(Material.NETHERITE_SCRAP,      1, 1, 1)
            );
            placeInChest(chest, loot);

            ItemStack pot = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) pot.getItemMeta();
            if (meta != null) { meta.setBasePotionType(PotionType.FIRE_RESISTANCE); pot.setItemMeta(meta); }
            for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
                if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
            }
            chest.update(true);
        }
    }
}