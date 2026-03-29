package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Wither Shrine — large dark shrine with overworld/end accents.
 * Size: 12x8x12
 */
public class WitherShrine extends BaseStructure {

    public WitherShrine(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Platform 12x12 (alternating polished blackstone + end stone bricks) ─
        for (int px = 0; px < 12; px++)
            for (int pz = 0; pz < 12; pz++) {
                Material m = ((px + pz) % 2 == 0)
                        ? Material.POLISHED_BLACKSTONE
                        : Material.END_STONE_BRICKS; // End block in Nether!
                setBlock(world, x + px, y, z + pz, m);
            }

        // ── Outer walls 5 high ────────────────────────────────────────────
        for (int wy = 1; wy <= 5; wy++) {
            for (int wx = 0; wx < 12; wx++) {
                setBlock(world, x + wx, y + wy, z,      Material.BLACKSTONE);
                setBlock(world, x + wx, y + wy, z + 11, Material.BLACKSTONE);
            }
            for (int wz = 1; wz < 11; wz++) {
                setBlock(world, x,      y + wy, z + wz, Material.BLACKSTONE);
                setBlock(world, x + 11, y + wy, z + wz, Material.BLACKSTONE);
            }
        }

        // ── Corner polished blackstone pillars 7 high ─────────────────────
        for (int wy = 1; wy <= 7; wy++) {
            setBlock(world, x,      y + wy, z,      Material.POLISHED_BLACKSTONE_BRICKS);
            setBlock(world, x + 11, y + wy, z,      Material.POLISHED_BLACKSTONE_BRICKS);
            setBlock(world, x,      y + wy, z + 11, Material.POLISHED_BLACKSTONE_BRICKS);
            setBlock(world, x + 11, y + wy, z + 11, Material.POLISHED_BLACKSTONE_BRICKS);
        }

        // ── 4 inner obsidian pillars (overworld block!) ───────────────────
        int[][] innerPillars = {{3,3},{8,3},{3,8},{8,8}};
        for (int[] p : innerPillars)
            for (int h = 1; h <= 5; h++)
                setBlock(world, x + p[0], y + h, z + p[1], Material.OBSIDIAN);

        // ── Interior clear ────────────────────────────────────────────────
        for (int iy = 1; iy <= 5; iy++)
            for (int ix = 1; ix <= 10; ix++)
                for (int iz = 1; iz <= 10; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Central wither altar ──────────────────────────────────────────
        int[][] soulSand = {{5,5},{6,5},{5,6},{6,6}};
        for (int[] p : soulSand) {
            setBlock(world, x + p[0], y + 1, z + p[1], Material.SOUL_SAND);
            setBlock(world, x + p[0], y + 2, z + p[1], Material.SOUL_LANTERN);
        }

        // Wither roses at pillar bases
        for (int[] p : innerPillars)
            setBlock(world, x + p[0] + 1, y + 1, z + p[1], Material.WITHER_ROSE);

        // ── Roof open — top lanterns ──────────────────────────────────────
        setBlock(world, x + 5, y + 6, z + 5, Material.SOUL_LANTERN);
        setBlock(world, x + 6, y + 6, z + 6, Material.SOUL_LANTERN);
        // Crying obsidian glow accents — End-adjacent
        setBlock(world, x + 1, y + 6, z + 1, Material.CRYING_OBSIDIAN);
        setBlock(world, x + 10, y + 6, z + 1, Material.CRYING_OBSIDIAN);
        setBlock(world, x + 1, y + 6, z + 10, Material.CRYING_OBSIDIAN);
        setBlock(world, x + 10, y + 6, z + 10, Material.CRYING_OBSIDIAN);

        // ── Wide entrance ─────────────────────────────────────────────────
        for (int gap = 4; gap <= 7; gap++) {
            setBlock(world, x + gap, y + 1, z + 11, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 11, Material.AIR);
            setBlock(world, x + gap, y + 3, z + 11, Material.AIR);
        }

        // ── 3 Loot chests ─────────────────────────────────────────────────
        placeChest(world, x + 1, y + 1, z + 1, random);
        placeChest(world, x + 10, y + 1, z + 1, random);
        placeChest(world, x + 10, y + 1, z + 10, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(4);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.COAL_BLOCK,          8, 2, 4),
                    lootEntry(Material.OBSIDIAN,            9, 3, 7),
                    lootEntry(Material.BLACKSTONE,          9, 4, 10),
                    lootEntry(Material.SOUL_SAND,           8, 2, 6),
                    lootEntry(Material.WITHER_ROSE,         5, 1, 3),
                    lootEntry(Material.IRON_INGOT,          7, 2, 5),
                    lootEntry(Material.NETHER_BRICK,        8, 4, 10),
                    lootEntry(Material.DIAMOND,             2, 1, 2),
                    lootEntry(Material.GOLD_INGOT,          6, 2, 5),
                    lootEntry(Material.NETHERITE_SCRAP,     2, 1, 1),
                    lootEntry(Material.IRON_SWORD,          4, 1, 1),
                    lootEntry(Material.ENCHANTED_BOOK,      2, 1, 1)
            );
            placeInChest(chest, loot);
            chest.update(true);
        }
    }
}