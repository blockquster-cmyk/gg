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
 * Void Beacon — a triangular floating-style structure with a beacon-like tip.
 * Size: 7x12x7, pyramid rising from end stone base, beacon at the apex.
 * Materials: end stone bricks, obsidian, purpur, end rods, iron blocks (beacon base)
 * Loot: high-value end + overworld mix, iron blocks, potions
 *
 * @author ItzAbood69
 */
public class VoidBeacon extends BaseStructure {

    public VoidBeacon(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 70) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Pyramid layers (shrinking each 2 blocks) ──────────────────────
        // Base: 7x7 layer (y+1, y+2)
        placeLayer(world, x, y + 1, z, 7, Material.END_STONE_BRICKS);
        placeLayer(world, x, y + 2, z, 7, Material.END_STONE_BRICKS);
        // Mid: 5x5 (y+3, y+4)
        placeLayer(world, x + 1, y + 3, z + 1, 5, Material.PURPUR_BLOCK);
        placeLayer(world, x + 1, y + 4, z + 1, 5, Material.PURPUR_BLOCK);
        // Upper: 3x3 (y+5, y+6)
        placeLayer(world, x + 2, y + 5, z + 2, 3, Material.OBSIDIAN);
        placeLayer(world, x + 2, y + 6, z + 2, 3, Material.OBSIDIAN);
        // Apex: 1x1 iron block + beacon
        setBlock(world, x + 3, y + 7, z + 3, Material.IRON_BLOCK);
        setBlock(world, x + 3, y + 8, z + 3, Material.BEACON);
        // Beacon cap end rod spire
        setBlock(world, x + 3, y + 9, z + 3, Material.END_ROD);
        setBlock(world, x + 3, y + 10, z + 3, Material.END_ROD);

        // ── Hollow out the pyramid interior ───────────────────────────────
        for (int iy = 2; iy <= 5; iy++) {
            int offset = (iy <= 2) ? 1 : (iy <= 4) ? 2 : 3;
            int size = 7 - (offset * 2);
            if (size <= 0) continue;
            for (int ix = offset + 1; ix < (offset + size - 1); ix++) {
                for (int iz = offset + 1; iz < (offset + size - 1); iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // ── Entrance at base ───────────────────────────────────────────────
        setBlock(world, x + 3, y + 1, z + 6, Material.AIR);
        setBlock(world, x + 3, y + 2, z + 6, Material.AIR);

        // ── Inner end rods lighting ────────────────────────────────────────
        setBlock(world, x + 1, y + 2, z + 1, Material.END_ROD);
        setBlock(world, x + 5, y + 2, z + 1, Material.END_ROD);
        setBlock(world, x + 1, y + 2, z + 5, Material.END_ROD);
        setBlock(world, x + 5, y + 2, z + 5, Material.END_ROD);

        // ── Loot chest at base interior ───────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 2, y + 2, z + 2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillBeaconChest(chest, random);
        }
    }

    /** Place a hollow square ring of given size at (x,y,z). */
    private void placeLayer(World world, int x, int y, int z, int size, Material mat) {
        for (int wx = 0; wx < size; wx++) {
            for (int wz = 0; wz < size; wz++) {
                boolean edge = (wx == 0 || wx == size - 1 || wz == 0 || wz == size - 1);
                if (edge) setBlock(world, x + wx, y, z + wz, mat);
            }
        }
    }

    private void fillBeaconChest(Chest chest, Random random) {
        int rolls = 4 + random.nextInt(3); // Best loot in the end
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.IRON_BLOCK,        5, 1, 2),
            lootEntry(Material.DIAMOND,           5, 1, 3),
            lootEntry(Material.ENDER_PEARL,       9, 3, 6),
            lootEntry(Material.ENDER_EYE,         7, 2, 5),
            lootEntry(Material.SHULKER_SHELL,     5, 1, 3),
            lootEntry(Material.OBSIDIAN,          8, 3, 6),
            lootEntry(Material.ENCHANTED_BOOK,    3, 1, 1),
            lootEntry(Material.ELYTRA,            2, 1, 1),
            lootEntry(Material.NETHERITE_SCRAP,   2, 1, 1),
            lootEntry(Material.DIAMOND_SWORD,     2, 1, 1),
            lootEntry(Material.GOLD_INGOT,        7, 2, 5)
        );
        placeInChest(chest, loot);

        // Always: slow falling potion
        ItemStack pot = new ItemStack(Material.POTION);
        PotionMeta m = (PotionMeta) pot.getItemMeta();
        if (m != null) { m.setBasePotionType(PotionType.SLOW_FALLING); pot.setItemMeta(m); }
        for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
            if (chest.getInventory().getItem(slot) == null) { chest.getInventory().setItem(slot, pot); break; }
        }
        chest.update();
    }
}
