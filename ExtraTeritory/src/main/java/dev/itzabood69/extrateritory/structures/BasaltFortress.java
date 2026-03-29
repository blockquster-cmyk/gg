package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Basalt Fortress — massive nether fortress with 3 floors + overworld/end accents.
 * Size: 16x12x16
 */
public class BasaltFortress extends BaseStructure {

    public BasaltFortress(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Foundation 16x16 ──────────────────────────────────────────────
        for (int px = 0; px < 16; px++)
            for (int pz = 0; pz < 16; pz++)
                setBlock(world, x + px, y, z + pz, Material.SMOOTH_BASALT);

        // ── Outer walls 2 blocks thick, 8 high ────────────────────────────
        for (int wy = 1; wy <= 8; wy++) {
            for (int wx = 0; wx < 16; wx++) {
                setBlock(world, x + wx, y + wy, z,      Material.BASALT);
                setBlock(world, x + wx, y + wy, z + 1,  wy <= 3 ? Material.BASALT : Material.AIR);
                setBlock(world, x + wx, y + wy, z + 15, Material.BASALT);
                setBlock(world, x + wx, y + wy, z + 14, wy <= 3 ? Material.BASALT : Material.AIR);
            }
            for (int wz = 2; wz < 14; wz++) {
                setBlock(world, x,      y + wy, z + wz, Material.BASALT);
                setBlock(world, x + 1,  y + wy, z + wz, wy <= 3 ? Material.BASALT : Material.AIR);
                setBlock(world, x + 15, y + wy, z + wz, Material.BASALT);
                setBlock(world, x + 14, y + wy, z + wz, wy <= 3 ? Material.BASALT : Material.AIR);
            }
        }

        // ── Corner towers (smooth basalt) 10 high ─────────────────────────
        for (int wy = 1; wy <= 10; wy++) {
            for (int[] c : new int[][]{{0,0},{15,0},{0,15},{15,15}}) {
                setBlock(world, x + c[0], y + wy, z + c[1], Material.SMOOTH_BASALT);
            }
        }

        // ── Merlons on top ─────────────────────────────────────────────────
        for (int wx = 0; wx < 16; wx += 2) {
            setBlock(world, x + wx, y + 9, z,      Material.POLISHED_BLACKSTONE);
            setBlock(world, x + wx, y + 9, z + 15, Material.POLISHED_BLACKSTONE);
        }
        for (int wz = 0; wz < 16; wz += 2) {
            setBlock(world, x,      y + 9, z + wz, Material.POLISHED_BLACKSTONE);
            setBlock(world, x + 15, y + 9, z + wz, Material.POLISHED_BLACKSTONE);
        }

        // ── Floor 1 interior clear ────────────────────────────────────────
        for (int iy = 1; iy <= 3; iy++)
            for (int ix = 2; ix <= 13; ix++)
                for (int iz = 2; iz <= 13; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Floor 2 (smooth stone slab — overworld block!) ────────────────
        for (int fx = 2; fx <= 13; fx++)
            for (int fz = 2; fz <= 13; fz++)
                setBlock(world, x + fx, y + 4, z + fz, Material.SMOOTH_STONE_SLAB);
        // Holes for access
        setBlock(world, x + 7, y + 4, z + 7, Material.AIR);
        setBlock(world, x + 8, y + 4, z + 7, Material.AIR);
        setBlock(world, x + 7, y + 4, z + 8, Material.AIR);

        // ── Floor 2 clear ─────────────────────────────────────────────────
        for (int iy = 5; iy <= 7; iy++)
            for (int ix = 2; ix <= 13; ix++)
                for (int iz = 2; iz <= 13; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Floor 3 (end stone bricks — end block in nether!) ─────────────
        for (int fx = 3; fx <= 12; fx++)
            for (int fz = 3; fz <= 12; fz++)
                setBlock(world, x + fx, y + 8, z + fz, Material.END_STONE_BRICKS);
        setBlock(world, x + 7, y + 8, z + 7, Material.AIR);
        setBlock(world, x + 8, y + 8, z + 7, Material.AIR);

        // ── Interior lighting ─────────────────────────────────────────────
        setBlock(world, x + 3,  y + 3, z + 3,  Material.SOUL_LANTERN);
        setBlock(world, x + 12, y + 3, z + 3,  Material.SOUL_LANTERN);
        setBlock(world, x + 3,  y + 3, z + 12, Material.SOUL_LANTERN);
        setBlock(world, x + 12, y + 3, z + 12, Material.SOUL_LANTERN);

        // End rods on floor 3 — cross-dimension
        setBlock(world, x + 5,  y + 9, z + 5,  Material.END_ROD);
        setBlock(world, x + 10, y + 9, z + 5,  Material.END_ROD);
        setBlock(world, x + 5,  y + 9, z + 10, Material.END_ROD);
        setBlock(world, x + 10, y + 9, z + 10, Material.END_ROD);

        // ── Wide entrance ─────────────────────────────────────────────────
        for (int gap = 6; gap <= 9; gap++) {
            setBlock(world, x + gap, y + 1, z + 15, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 15, Material.AIR);
            setBlock(world, x + gap, y + 3, z + 15, Material.AIR);
            setBlock(world, x + gap, y + 1, z + 14, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 14, Material.AIR);
        }

        // ── 4 Loot chests (1 per floor + bonus) ───────────────────────────
        placeChest(world, x + 3,  y + 1, z + 3,  random, false);
        placeChest(world, x + 12, y + 1, z + 12, random, false);
        placeChest(world, x + 3,  y + 5, z + 3,  random, true);
        placeChest(world, x + 12, y + 9, z + 12, random, true);
    }

    private void placeChest(World world, int x, int y, int z, Random random, boolean upper) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = upper ? (5 + random.nextInt(4)) : (3 + random.nextInt(3));
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.IRON_INGOT,             9, 3, 7),
                    lootEntry(Material.GOLD_INGOT,             7, 2, 5),
                    lootEntry(Material.OBSIDIAN,               7, 3, 6),
                    lootEntry(Material.NETHER_WART,            8, 3, 7),
                    lootEntry(Material.IRON_SWORD,             5, 1, 1),
                    lootEntry(Material.CHAINMAIL_HELMET,       upper ? 5 : 2, 1, 1),
                    lootEntry(Material.CHAINMAIL_CHESTPLATE,   upper ? 3 : 1, 1, 1),
                    lootEntry(Material.GOLDEN_SWORD,           4, 1, 1),
                    lootEntry(Material.COOKED_PORKCHOP,       10, 3, 7),
                    lootEntry(Material.DIAMOND,                upper ? 3 : 1, 1, 2),
                    lootEntry(Material.NETHERITE_SCRAP,        upper ? 2 : 0, 1, 1),
                    lootEntry(Material.ENCHANTED_BOOK,         upper ? 3 : 1, 1, 1),
                    lootEntry(Material.IRON_CHESTPLATE,        upper ? 2 : 1, 1, 1)
            );
            placeInChest(chest, loot);
            chest.update(true);
        }
    }
}