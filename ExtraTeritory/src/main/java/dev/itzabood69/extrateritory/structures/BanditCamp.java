package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Bandit Camp — a rough campsite with a tent, campfire, and loot.
 * Size: 9x9 footprint, tent is 5x3, rough wooden construction.
 * Materials: spruce planks, spruce fence, cobblestone, campfire
 * Loot: stolen goods — iron, gold, food, weapons
 *
 * @author ItzAbood69
 */
public class BanditCamp extends BaseStructure {

    public BanditCamp(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 60 || y > 82) return;

        // ── Campfire in the center ─────────────────────────────────────────
        int cx = x + 4;
        int cz = z + 4;
        setBlock(world, cx, y + 1, cz, Material.CAMPFIRE);

        // ── Ring of logs around fire ───────────────────────────────────────
        int[][] logPositions = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
        for (int[] pos : logPositions) {
            if (random.nextInt(3) != 0) { // some logs missing — rough camp
                setBlock(world, cx + pos[0], y + 1, cz + pos[1], Material.OAK_LOG);
            }
        }

        // ── Tent structure (north side, 5x3 lean-to) ──────────────────────
        int tx = x + 1;
        int tz = z + 1;
        // Back wall
        for (int wx = 0; wx < 5; wx++) {
            setBlock(world, tx + wx, y + 1, tz,     Material.SPRUCE_PLANKS);
            setBlock(world, tx + wx, y + 2, tz,     Material.SPRUCE_PLANKS);
        }
        // Side walls
        for (int wz = 0; wz < 3; wz++) {
            setBlock(world, tx,     y + 1, tz + wz, Material.SPRUCE_PLANKS);
            setBlock(world, tx + 4, y + 1, tz + wz, Material.SPRUCE_PLANKS);
        }
        // Roof (sloping — slabs)
        for (int rx = 0; rx < 5; rx++) {
            setBlock(world, tx + rx, y + 3, tz,     Material.SPRUCE_SLAB);
            setBlock(world, tx + rx, y + 2, tz + 1, Material.SPRUCE_SLAB);
            setBlock(world, tx + rx, y + 1, tz + 2, Material.SPRUCE_SLAB);
        }

        // ── Tent interior — clear air ──────────────────────────────────────
        for (int iy = 1; iy <= 2; iy++) {
            for (int ix = 1; ix < 4; ix++) {
                setBlock(world, tx + ix, y + iy, tz + 1, Material.AIR);
                setBlock(world, tx + ix, y + iy, tz + 2, Material.AIR);
            }
        }

        // ── Fence perimeter posts (rough boundary) ─────────────────────────
        int[][] fenceCorners = {{0,0},{8,0},{0,8},{8,8},{4,0},{0,4},{8,4},{4,8}};
        for (int[] fc : fenceCorners) {
            setBlock(world, x + fc[0], y + 1, z + fc[1], Material.SPRUCE_FENCE);
            if (random.nextBoolean()) {
                setBlock(world, x + fc[0], y + 2, z + fc[1], Material.SPRUCE_FENCE);
            }
        }

        // ── Torch on tent corner ──────────────────────────────────────────
        setBlock(world, tx + 4, y + 2, tz, Material.WALL_TORCH);

        // ── Loot chest inside the tent ────────────────────────────────────
        Block chestBlock = world.getBlockAt(tx + 1, y + 1, tz + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillCampChest(chest, random);
        }

        // ── Barrel outside with extra loot ───────────────────────────────
        Block barrel = world.getBlockAt(cx + 2, y + 1, cz + 1);
        barrel.setType(Material.BARREL);
    }

    private void fillCampChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.IRON_INGOT,        9, 2, 5),
            lootEntry(Material.GOLD_INGOT,        6, 1, 3),
            lootEntry(Material.COOKED_PORKCHOP,  10, 3, 6),
            lootEntry(Material.IRON_SWORD,         5, 1, 1),
            lootEntry(Material.IRON_AXE,           4, 1, 1),
            lootEntry(Material.LEATHER_CHESTPLATE, 5, 1, 1),
            lootEntry(Material.ARROW,              9, 6, 12),
            lootEntry(Material.BOW,                4, 1, 1),
            lootEntry(Material.DIAMOND,            1, 1, 1),
            lootEntry(Material.EMERALD,            3, 1, 2),
            lootEntry(Material.TNT,                2, 1, 2),
            lootEntry(Material.FLINT,              8, 2, 4)
        );
        placeInChest(chest, loot);
    }
}
