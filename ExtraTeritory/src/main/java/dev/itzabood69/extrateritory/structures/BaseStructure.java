package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared utilities for all ExtraTeritory structure builders.
 * Provides block placement, surface detection, and loot table helpers.
 *
 * @author ItzAbood69
 */
public abstract class BaseStructure {

    protected final ExtraTeritory plugin;

    protected BaseStructure(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    /**
     * Place the structure at world coordinates (x, z).
     * y is determined internally via getSurfaceY().
     */
    public abstract void place(World world, int x, int z, Random random);

    // ── Block placement ───────────────────────────────────────────────────────

    protected void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }

    /**
     * Finds the highest non-air block Y at the given X,Z.
     * Returns -1 if no solid surface found in range.
     */
    protected int getSurfaceY(World world, int x, int z) {
        for (int y = 150; y > 40; y--) {
            Material m = world.getBlockAt(x, y, z).getType();
            if (m != Material.AIR && m != Material.WATER && m != Material.LAVA) {
                return y;
            }
        }
        return -1;
    }

    // ── Loot table helpers ────────────────────────────────────────────────────

    /**
     * Defines a loot entry: material, weight (higher = more likely), min/max count.
     */
    protected int[] lootEntry(Material material, int weight, int minCount, int maxCount) {
        // Encode as int array: [material ordinal, weight, minCount, maxCount]
        return new int[]{ material.ordinal(), weight, minCount, maxCount };
    }

    /**
     * Rolls the loot table `rolls` times and returns ItemStacks to put in chest.
     * Weighted random selection — higher weight = more likely.
     */
    protected ItemStack[] buildLootTable(Random random, int rolls, int[]... entries) {
        // Build weighted pool
        List<int[]> pool = new ArrayList<>();
        for (int[] entry : entries) {
            for (int w = 0; w < entry[1]; w++) {
                pool.add(entry);
            }
        }
        Collections.shuffle(pool, random);

        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < rolls && i < pool.size(); i++) {
            int[] entry = pool.get(random.nextInt(pool.size()));
            Material mat = Material.values()[entry[0]];
            int count = entry[2] + (entry[3] > entry[2] ? random.nextInt(entry[3] - entry[2] + 1) : 0);
            result.add(new ItemStack(mat, count));
        }

        return result.toArray(new ItemStack[0]);
    }

    /**
     * Places ItemStacks into a chest at random slots.
     */
    protected void placeInChest(Chest chest, ItemStack[] items) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < chest.getInventory().getSize(); i++) slots.add(i);
        Collections.shuffle(slots);

        for (int i = 0; i < items.length && i < slots.size(); i++) {
            chest.getInventory().setItem(slots.get(i), items[i]);
        }
        chest.update();
    }
}
