package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Ender Tomb — a flat sarcophagus-style sealed tomb in the end.
 * Size: 8x3x5 main chamber, flat slab top.
 * Materials: end stone bricks, purpur block, crying obsidian, end rods
 * Loot: high-tier end loot, rare netherite, enchanted gear
 *
 * @author ItzAbood69
 */
public class EnderTomb extends BaseStructure {

    public EnderTomb(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 73) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Main chamber: 8x5 floor ────────────────────────────────────────
        for (int px = 0; px < 8; px++) {
            for (int pz = 0; pz < 5; pz++) {
                setBlock(world, x + px, y, z + pz, Material.END_STONE_BRICKS);
            }
        }

        // ── Walls 2 high (low ceiling tomb) ───────────────────────────────
        for (int wy = 1; wy <= 2; wy++) {
            for (int wx = 0; wx < 8; wx++) {
                setBlock(world, x + wx, y + wy, z,     Material.PURPUR_BLOCK);
                setBlock(world, x + wx, y + wy, z + 4, Material.PURPUR_BLOCK);
            }
            for (int wz = 1; wz < 4; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.PURPUR_BLOCK);
                setBlock(world, x + 7, y + wy, z + wz, Material.PURPUR_BLOCK);
            }
        }

        // ── Crying obsidian accents at corners ─────────────────────────────
        for (int wy = 1; wy <= 3; wy++) {
            setBlock(world, x,     y + wy, z,     Material.CRYING_OBSIDIAN);
            setBlock(world, x + 7, y + wy, z,     Material.CRYING_OBSIDIAN);
            setBlock(world, x,     y + wy, z + 4, Material.CRYING_OBSIDIAN);
            setBlock(world, x + 7, y + wy, z + 4, Material.CRYING_OBSIDIAN);
        }

        // ── Sealed flat roof ───────────────────────────────────────────────
        for (int rx = 0; rx < 8; rx++) {
            for (int rz = 0; rz < 5; rz++) {
                setBlock(world, x + rx, y + 3, z + rz, Material.PURPUR_BLOCK);
            }
        }

        // ── End rods along the top ─────────────────────────────────────────
        for (int rx = 1; rx < 7; rx += 2) {
            setBlock(world, x + rx, y + 4, z + 2, Material.END_ROD);
        }

        // ── Interior clear ─────────────────────────────────────────────────
        for (int iy = 1; iy <= 2; iy++) {
            for (int ix = 1; ix <= 6; ix++) {
                for (int iz = 1; iz <= 3; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // ── Sarcophagus in center (end stone bricks + purpur top) ─────────
        setBlock(world, x + 3, y + 1, z + 2, Material.END_STONE_BRICKS);
        setBlock(world, x + 4, y + 1, z + 2, Material.END_STONE_BRICKS);
        setBlock(world, x + 3, y + 2, z + 2, Material.PURPUR_PILLAR);
        setBlock(world, x + 4, y + 2, z + 2, Material.PURPUR_PILLAR);

        // ── Sealed entrance (break in to enter) ───────────────────────────
        // No entrance — it's a sealed tomb. Players must mine in.

        // ── Loot chest inside behind sarcophagus ──────────────────────────
        Block chestBlock = world.getBlockAt(x + 5, y + 1, z + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillTombChest(chest, random);
        }
    }

    private void fillTombChest(Chest chest, Random random) {
        int rolls = 4 + random.nextInt(3); // Best loot of all End structures
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.DIAMOND,           5, 1, 3),
            lootEntry(Material.ENDER_PEARL,       9, 3, 6),
            lootEntry(Material.SHULKER_SHELL,     5, 1, 3),
            lootEntry(Material.ENDER_EYE,         7, 2, 4),
            lootEntry(Material.PURPUR_BLOCK,      8, 4, 8),
            lootEntry(Material.ELYTRA,            2, 1, 1),  // higher chance than others
            lootEntry(Material.ENCHANTED_BOOK,    3, 1, 1),
            lootEntry(Material.NETHERITE_SCRAP,   2, 1, 1),
            lootEntry(Material.IRON_INGOT,        7, 2, 5),
            lootEntry(Material.GOLD_INGOT,        6, 2, 4),
            lootEntry(Material.DIAMOND_SWORD,     2, 1, 1),
            lootEntry(Material.DIAMOND_CHESTPLATE,1, 1, 1)
        );
        placeInChest(chest, loot);
    }
}
