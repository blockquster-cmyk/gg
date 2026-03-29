package dev.itzabood69.extrateritory.structures;

import dev.itzabood69.extrateritory.ExtraTeritory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Random;

/**
 * End Village — a mysterious purpur settlement in the End.
 * Size: ~18x18 footprint, 3 towers + central beacon structure.
 * Materials: purpur block, purpur pillar, end stone bricks, end rods
 * Residents: 2-4 Villagers (End-type/Taiga look) with high-level trades
 * Loot: top-tier end resources, enchanted books, rare elytra
 *
 * @author ItzAbood69
 */
public class EndVillage extends BaseStructure {

    public EndVillage(ExtraTeritory plugin) {
        super(plugin);
    }

    @Override
    public void place(World world, int x, int z, Random random) {
        int y = getSurfaceY(world, x, z);
        if (y < 55 || y > 73) return;
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) return;

        // ── End stone brick paths in a cross ─────────────────────────────
        for (int i = 0; i < 18; i++) {
            setBlock(world, x + i, y + 1, z + 9, Material.END_STONE_BRICKS);
            setBlock(world, x + 9, y + 1, z + i, Material.END_STONE_BRICKS);
        }

        // ── 3 tower-houses ────────────────────────────────────────────────
        buildEndTower(world, x + 1, y + 1, z + 1, random, true);
        buildEndTower(world, x + 11, y + 1, z + 1, random, false);
        buildEndTower(world, x + 1, y + 1, z + 11, random, false);

        // ── Central meeting platform ──────────────────────────────────────
        buildCentralPlatform(world, x + 7, y + 1, z + 7);

        // ── Spawn End villagers ───────────────────────────────────────────
        spawnEndVillagers(world, x, y + 2, z, random);
    }

    private void buildEndTower(World world, int x, int y, int z, Random random, boolean hasChest) {
        // Floor — end stone bricks
        for (int fx = 0; fx < 6; fx++)
            for (int fz = 0; fz < 6; fz++)
                setBlock(world, x + fx, y, z + fz, Material.END_STONE_BRICKS);

        // Walls — purpur block
        for (int wy = 1; wy <= 4; wy++) {
            for (int wx = 0; wx < 6; wx++) {
                setBlock(world, x + wx, y + wy, z,     Material.PURPUR_BLOCK);
                setBlock(world, x + wx, y + wy, z + 5, Material.PURPUR_BLOCK);
            }
            for (int wz = 1; wz < 5; wz++) {
                setBlock(world, x,     y + wy, z + wz, Material.PURPUR_BLOCK);
                setBlock(world, x + 5, y + wy, z + wz, Material.PURPUR_BLOCK);
            }
        }

        // Corner pillars — purpur pillar
        for (int wy = 1; wy <= 6; wy++) {
            setBlock(world, x,     y + wy, z,     Material.PURPUR_PILLAR);
            setBlock(world, x + 5, y + wy, z,     Material.PURPUR_PILLAR);
            setBlock(world, x,     y + wy, z + 5, Material.PURPUR_PILLAR);
            setBlock(world, x + 5, y + wy, z + 5, Material.PURPUR_PILLAR);
        }

        // Roof — purpur slabs
        for (int rx = 0; rx < 6; rx++)
            for (int rz = 0; rz < 6; rz++)
                setBlock(world, x + rx, y + 5, z + rz, Material.PURPUR_SLAB);

        // Top end rods
        setBlock(world, x + 2, y + 6, z + 2, Material.END_ROD);
        setBlock(world, x + 3, y + 6, z + 3, Material.END_ROD);

        // Entrance (south)
        setBlock(world, x + 2, y + 1, z + 5, Material.AIR);
        setBlock(world, x + 2, y + 2, z + 5, Material.AIR);
        setBlock(world, x + 3, y + 1, z + 5, Material.AIR);
        setBlock(world, x + 3, y + 2, z + 5, Material.AIR);

        // Interior clear
        for (int iy = 1; iy <= 4; iy++)
            for (int ix = 1; ix <= 4; ix++)
                for (int iz = 1; iz <= 4; iz++)
                    setBlock(world, x + ix, y + iy, z + iz, Material.AIR);

        // Interior end rods
        setBlock(world, x + 1, y + 1, z + 1, Material.END_ROD);
        setBlock(world, x + 4, y + 1, z + 4, Material.END_ROD);

        if (hasChest) {
            Block chestBlock = world.getBlockAt(x + 4, y + 1, z + 1);
            chestBlock.setType(Material.CHEST);
            if (chestBlock.getState() instanceof Chest chest) {
                fillEndVillageChest(chest, random);
            }
        }
    }

    private void buildCentralPlatform(World world, int x, int y, int z) {
        // 5x5 raised platform
        for (int px = 0; px < 5; px++)
            for (int pz = 0; pz < 5; pz++)
                setBlock(world, x + px, y, z + pz, Material.PURPUR_BLOCK);

        // Center pillar + end rod
        setBlock(world, x + 2, y + 1, z + 2, Material.END_STONE_BRICKS);
        setBlock(world, x + 2, y + 2, z + 2, Material.PURPUR_PILLAR);
        setBlock(world, x + 2, y + 3, z + 2, Material.END_ROD);

        // Corner end rods
        setBlock(world, x,     y + 1, z,     Material.END_ROD);
        setBlock(world, x + 4, y + 1, z,     Material.END_ROD);
        setBlock(world, x,     y + 1, z + 4, Material.END_ROD);
        setBlock(world, x + 4, y + 1, z + 4, Material.END_ROD);
    }

    private void spawnEndVillagers(World world, int x, int y, int z, Random random) {
        Villager.Profession[] profs = {
            Villager.Profession.LIBRARIAN,    // "lore keeper"
            Villager.Profession.CLERIC,       // "void shaman"
            Villager.Profession.WEAPONSMITH,  // "end forger"
            Villager.Profession.ARMORER,      // "purpur armorer"
            Villager.Profession.CARTOGRAPHER  // "void mapper"
        };

        int count = 2 + random.nextInt(3); // 2-4

        int[][] spots = {{3,3},{13,3},{3,13},{9,9},{13,13}};
        for (int i = 0; i < count; i++) {
            int[] spot = spots[i % spots.length];
            Location loc = new Location(world, x + spot[0] + 0.5, y + 1, z + spot[1] + 0.5);
            Villager v = world.spawn(loc, Villager.class);
            v.setProfession(profs[i % profs.length]);
            v.setVillagerType(Villager.Type.TAIGA); // unusual look for End
            v.setVillagerLevel(3 + random.nextInt(2)); // high level trades
        }
    }

    private void fillEndVillageChest(Chest chest, Random random) {
        int rolls = 4 + random.nextInt(3); // best loot
        ItemStack[] loot = buildLootTable(random, rolls,
            lootEntry(Material.ENDER_PEARL,        9, 3, 6),
            lootEntry(Material.ENDER_EYE,          7, 2, 5),
            lootEntry(Material.SHULKER_SHELL,      5, 1, 3),
            lootEntry(Material.PURPUR_BLOCK,       8, 4, 8),
            lootEntry(Material.DIAMOND,            5, 1, 3),
            lootEntry(Material.ELYTRA,             2, 1, 1),
            lootEntry(Material.ENCHANTED_BOOK,     4, 1, 2),
            lootEntry(Material.END_ROD,            6, 2, 5),
            lootEntry(Material.IRON_INGOT,         7, 2, 5),
            lootEntry(Material.NETHERITE_SCRAP,    2, 1, 1),
            lootEntry(Material.DIAMOND_CHESTPLATE, 1, 1, 1)
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
