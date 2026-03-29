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
 * Shulker Vault — a bunker-like structure built into the end stone.
 * Size: 7x5x7, reinforced purpur walls, shulker box decoration.
 * Materials: purpur block, purpur slab, end stone bricks, shulker box
 * Loot: shulker shells, levitation potions, diamonds, rare elytra
 *
 * @author ItzAbood69
 */
public class ShulkerVault extends BaseStructure {

    public ShulkerVault(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 75) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── Foundation ─────────────────────────────────────────────────────
        for (int px = 0; px < 7; px++) {
            for (int pz = 0; pz < 7; pz++) {
                setBlock(world, x + px, y, z + pz, Material.END_STONE_BRICKS);
            }
        }

        // ── Walls 4 high ──────────────────────────────────────────────────
        for (int wy = 1; wy <= 4; wy++) {
            for (int wx = 0; wx < 7; wx++) {
                setBlock(world, x + wx, y + wy, z,     Material.PURPUR_BLOCK);
                setBlock(world, x + wx, y + wy, z + 6, Material.PURPUR_BLOCK);
            }
            for (int wz = 1; wz < 6; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.PURPUR_BLOCK);
                setBlock(world, x + 6, y + wy, z + wz, Material.PURPUR_BLOCK);
            }
        }

        // ── Corner pillars ─────────────────────────────────────────────────
        for (int wy = 1; wy <= 5; wy++) {
            setBlock(world, x,     y + wy, z,     Material.PURPUR_PILLAR);
            setBlock(world, x + 6, y + wy, z,     Material.PURPUR_PILLAR);
            setBlock(world, x,     y + wy, z + 6, Material.PURPUR_PILLAR);
            setBlock(world, x + 6, y + wy, z + 6, Material.PURPUR_PILLAR);
        }

        // ── Roof (purpur slabs) ────────────────────────────────────────────
        for (int rx = 0; rx < 7; rx++) {
            for (int rz = 0; rz < 7; rz++) {
                setBlock(world, x + rx, y + 5, z + rz, Material.PURPUR_SLAB);
            }
        }

        // ── Interior ─────────────────────────────────────────────────────
        for (int iy = 1; iy <= 4; iy++) {
            for (int ix = 1; ix <= 5; ix++) {
                for (int iz = 1; iz <= 5; iz++) {
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);
                }
            }
        }

        // ── Shulker box decoration on walls ───────────────────────────────
        setBlock(world, x + 1, y + 2, z + 1, Material.SHULKER_BOX);
        setBlock(world, x + 5, y + 2, z + 5, Material.SHULKER_BOX);
        setBlock(world, x + 5, y + 2, z + 1, Material.CYAN_SHULKER_BOX);

        // ── End rods for lighting ──────────────────────────────────────────
        setBlock(world, x + 3, y + 4, z + 1, Material.END_ROD);
        setBlock(world, x + 3, y + 4, z + 5, Material.END_ROD);

        // ── Entrance (south wall) ──────────────────────────────────────────
        setBlock(world, x + 3, y + 1, z + 6, Material.AIR);
        setBlock(world, x + 3, y + 2, z + 6, Material.AIR);

        // ── Loot chest in center ───────────────────────────────────────────
        Block chestBlock = world.getBlockAt(x + 3, y + 1, z + 3);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillVaultChest(chest, random);
        }
    }

    private void fillVaultChest(Chest chest, Random random) {
        int rolls = 3 + random.nextInt(3);
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.SHULKER_SHELL,     6, 1, 2),
            lootEntry(Material.ENDER_PEARL,       9, 2, 5),
            lootEntry(Material.ENDER_EYE,         5, 1, 3),
            lootEntry(Material.PURPUR_BLOCK,      8, 3, 7),
            lootEntry(Material.DIAMOND,           3, 1, 2),
            lootEntry(Material.IRON_INGOT,        7, 2, 5),
            lootEntry(Material.GOLD_INGOT,        5, 1, 3),
            lootEntry(Material.CHORUS_FRUIT,      8, 2, 5),
            lootEntry(Material.ELYTRA,            1, 1, 1)   // very rare
        );
        placeInChest(chest, loot);

        // Slow falling potion always present
        ItemStack pot = new ItemStack(Material.POTION);
        PotionMeta m = (PotionMeta) pot.getItemMeta();
        if (m != null) { m.setBasePotionType(PotionType.SLOW_FALLING); pot.setItemMeta(m); }
        for (int slot = 0; slot < chest.getInventory().getSize(); slot++) {
            if (chest.getInventory().getItem(slot) == null) {
                chest.getInventory().setItem(slot, pot); break;
            }
        }
        chest.update();
    }
}
