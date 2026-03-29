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
 * Crystal Sanctum — a mystical end-glass shrine radiating with end rods.
 * Size: 9x9 footprint, domed structure, 6 blocks tall.
 * Materials: end stone bricks, purpur pillar, glass, end rods, end crystals
 * Loot: crystals, rare potions, diamonds, end materials
 *
 * @author ItzAbood69
 */
public class CrystalSanctum extends BaseStructure {

    public CrystalSanctum(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 73) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Foundation ring ────────────────────────────────────────────────
        for (int px = 1; px < 8; px++) {
            setBlock(world, x + px, y, z,     Material.END_STONE_BRICKS);
            setBlock(world, x + px, y, z + 8, Material.END_STONE_BRICKS);
        }
        for (int pz = 0; pz <= 8; pz++) {
            setBlock(world, x,     y, z + pz, Material.END_STONE_BRICKS);
            setBlock(world, x + 8, y, z + pz, Material.END_STONE_BRICKS);
        }

        // ── Walls (glass panels + purpur pillars) ─────────────────────────
        // Level 1-2: solid purpur base
        for (int wy = 1; wy <= 2; wy++) {
            for (int wx = 1; wx < 8; wx++) {
                setBlock(world, x + wx, y + wy, z + 1, Material.PURPUR_BLOCK);
                setBlock(world, x + wx, y + wy, z + 7, Material.PURPUR_BLOCK);
            }
            for (int wz = 2; wz < 7; wz++) {
                setBlock(world, x + 1, y + wy, z + wz, Material.PURPUR_BLOCK);
                setBlock(world, x + 7, y + wy, z + wz, Material.PURPUR_BLOCK);
            }
        }
        // Level 3-5: glass panels
        for (int wy = 3; wy <= 5; wy++) {
            for (int wx = 1; wx < 8; wx++) {
                setBlock(world, x + wx, y + wy, z + 1, Material.GLASS);
                setBlock(world, x + wx, y + wy, z + 7, Material.GLASS);
            }
            for (int wz = 2; wz < 7; wz++) {
                setBlock(world, x + 1, y + wy, z + wz, Material.GLASS);
                setBlock(world, x + 7, y + wy, z + wz, Material.GLASS);
            }
        }

        // ── Purpur pillar columns at wall-corners ─────────────────────────
        for (int wy = 1; wy <= 6; wy++) {
            setBlock(world, x + 1, y + wy, z + 1, Material.PURPUR_PILLAR);
            setBlock(world, x + 7, y + wy, z + 1, Material.PURPUR_PILLAR);
            setBlock(world, x + 1, y + wy, z + 7, Material.PURPUR_PILLAR);
            setBlock(world, x + 7, y + wy, z + 7, Material.PURPUR_PILLAR);
        }

        // ── Dome top (purpur slabs over the glass level) ───────────────────
        for (int rx = 1; rx < 8; rx++) {
            for (int rz = 1; rz < 8; rz++) {
                setBlock(world, x + rx, y + 6, z + rz, Material.PURPUR_SLAB);
            }
        }
        // Flat solid center 3x3
        for (int rx = 3; rx <= 5; rx++) {
            for (int rz = 3; rz <= 5; rz++) {
                setBlock(world, x + rx, y + 6, z + rz, Material.PURPUR_BLOCK);
            }
        }
        setBlock(world, x + 4, y + 7, z + 4, Material.END_ROD); // spire

        // ── Interior clear ─────────────────────────────────────────────────
        for (int iy = 1; iy <= 5; iy++) {
            for (int ix = 2; ix <= 6; ix++) {
                for (int iz = 2; iz <= 6; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // ── Central altar ─────────────────────────────────────────────────
        setBlock(world, x + 4, y + 1, z + 4, Material.END_STONE_BRICKS);
        setBlock(world, x + 4, y + 2, z + 4, Material.PURPUR_BLOCK);
        setBlock(world, x + 4, y + 3, z + 4, Material.END_ROD);

        // End rods at inner corners
        setBlock(world, x + 2, y + 1, z + 2, Material.END_ROD);
        setBlock(world, x + 6, y + 1, z + 2, Material.END_ROD);
        setBlock(world, x + 2, y + 1, z + 6, Material.END_ROD);
        setBlock(world, x + 6, y + 1, z + 6, Material.END_ROD);

        // ── Entrance (south) ──────────────────────────────────────────────
        setBlock(world, x + 3, y + 1, z + 7, Material.AIR);
        setBlock(world, x + 3, y + 2, z + 7, Material.AIR);
        setBlock(world, x + 4, y + 1, z + 7, Material.AIR);
        setBlock(world, x + 4, y + 2, z + 7, Material.AIR);
        setBlock(world, x + 5, y + 1, z + 7, Material.AIR);
        setBlock(world, x + 5, y + 2, z + 7, Material.AIR);

        // ── Loot chest ────────────────────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 2, y + 1, z + 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillSanctumChest(chest, random);
        }
    }

    private void fillSanctumChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.ENDER_PEARL,       9, 2, 5),
            lootEntry(Material.ENDER_EYE,         6, 1, 3),
            lootEntry(Material.PURPUR_BLOCK,      8, 3, 6),
            lootEntry(Material.GLASS,             8, 3, 6),
            lootEntry(Material.END_ROD,           6, 2, 4),
            lootEntry(Material.DIAMOND,           3, 1, 2),
            lootEntry(Material.IRON_INGOT,        7, 2, 5),
            lootEntry(Material.SHULKER_SHELL,     3, 1, 2),
            lootEntry(Material.ELYTRA,            1, 1, 1),
            lootEntry(Material.ENCHANTED_BOOK,    2, 1, 1)
        );
        placeInChest(chest, loot);

        // Always a slow falling potion
        ItemStack pot = new ItemStack(Material.POTION);
        PotionMeta m = (PotionMeta) pot.getItemMeta();
        if (m != null) { m.setBasePotionType(PotionType.SLOW_FALLING); pot.setItemMeta(m); }
        for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
            if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
        }
        chest.update();
    }
}
