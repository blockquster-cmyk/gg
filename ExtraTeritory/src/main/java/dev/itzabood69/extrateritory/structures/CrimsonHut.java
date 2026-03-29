package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/** Crimson Hut — large organic crimson settlement. Size: 10x6x10 */
public class CrimsonHut extends BaseStructure {

    public CrimsonHut(ExtraTeritory plugin) { super(plugin); }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 15 || y > 110) return;
        if (world.getBlockAt(x, y, z).getType() == Material.LAVA) return;

        // ── Floor 10x10 (crimson nylium + mossy cobblestone — overworld!) ─
        for (int px = 0; px < 10; px++)
            for (int pz = 0; pz < 10; pz++) {
                Material m = ((px + pz) % 3 == 0) ? Material.MOSSY_COBBLESTONE : Material.CRIMSON_NYLIUM;
                setBlock(world, x + px, y, z + pz, m);
            }

        // ── Walls ─────────────────────────────────────────────────────────
        for (int wy = 1; wy <= 4; wy++) {
            for (int wx = 0; wx < 10; wx++) {
                Material mat = (wx == 0 || wx == 9) ? Material.CRIMSON_STEM : Material.CRIMSON_PLANKS;
                setBlock(world, x + wx, y + wy, z,     mat);
                setBlock(world, x + wx, y + wy, z + 9, mat);
            }
            for (int wz = 1; wz < 9; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.CRIMSON_STEM);
                setBlock(world, x + 9, y + wy, z + wz, Material.CRIMSON_STEM);
            }
        }

        // ── Windows (glass pane — overworld!) ────────────────────────────
        setBlock(world, x + 3, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 6, y + 2, z,     Material.GLASS_PANE);
        setBlock(world, x + 3, y + 2, z + 9, Material.GLASS_PANE);
        setBlock(world, x + 6, y + 2, z + 9, Material.GLASS_PANE);

        // ── Roof (shroomlight center + crimson slabs) ──────────────────────
        for (int rx = 0; rx < 10; rx++)
            for (int rz = 0; rz < 10; rz++) {
                boolean isCenter = (rx >= 3 && rx <= 6 && rz >= 3 && rz <= 6);
                setBlock(world, x + rx, y + 5, z + rz, isCenter ? Material.SHROOMLIGHT : Material.CRIMSON_SLAB);
            }

        // ── Interior clear ─────────────────────────────────────────────────
        for (int iy = 1; iy <= 4; iy++)
            for (int ix = 1; ix <= 8; ix++)
                for (int iz = 1; iz <= 8; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // ── Decorations ───────────────────────────────────────────────────
        setBlock(world, x + 2, y + 1, z + 2, Material.CRIMSON_ROOTS);
        setBlock(world, x + 7, y + 1, z + 2, Material.CRIMSON_FUNGUS);
        setBlock(world, x + 2, y + 1, z + 7, Material.WARPED_FUNGUS);
        setBlock(world, x + 7, y + 1, z + 7, Material.CRIMSON_ROOTS);
        // Crafting table — cross-dimension
        setBlock(world, x + 4, y + 1, z + 4, Material.CRAFTING_TABLE);
        setBlock(world, x + 5, y + 1, z + 4, Material.FURNACE);

        // ── Entrance ──────────────────────────────────────────────────────
        for (int gap = 4; gap <= 5; gap++) {
            setBlock(world, x + gap, y + 1, z + 9, Material.AIR);
            setBlock(world, x + gap, y + 2, z + 9, Material.AIR);
        }

        // ── 2 Loot chests ─────────────────────────────────────────────────
        placeChest(world, x + 1, y + 1, z + 1, random);
        placeChest(world, x + 8, y + 1, z + 8, random);
    }

    private void placeChest(World world, int x, int y, int z, Random random) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            int rolls = 4 + random.nextInt(3);
            ItemStack[] loot = buildLootTable(random, rolls,
                    lootEntry(Material.CRIMSON_STEM,        8, 4, 8),
                    lootEntry(Material.CRIMSON_PLANKS,      8, 4, 8),
                    lootEntry(Material.SHROOMLIGHT,         5, 1, 3),
                    lootEntry(Material.NETHER_WART,        10, 3, 8),
                    lootEntry(Material.CRIMSON_FUNGUS,      7, 1, 4),
                    lootEntry(Material.WARPED_FUNGUS,       5, 1, 3),
                    lootEntry(Material.COOKED_PORKCHOP,     9, 3, 7),
                    lootEntry(Material.GOLD_INGOT,          5, 1, 3),
                    lootEntry(Material.DIAMOND,             2, 1, 1),
                    lootEntry(Material.MAGMA_CREAM,         6, 2, 4),
                    lootEntry(Material.IRON_INGOT,          7, 2, 5),
                    lootEntry(Material.BLAZE_ROD,           4, 1, 2)
            );
            placeInChest(chest, loot);
            chest.update(true);
        }
    }
}