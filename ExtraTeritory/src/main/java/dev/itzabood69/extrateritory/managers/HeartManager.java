package dev.itzabood69.extrateritory.managers;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

/**
 * Manages the creation, identification, and dimension extraction of Heart items.
 * Hearts are custom ItemStacks with PersistentDataContainer tags so they survive
 * server restarts and cannot be confused with vanilla items.
 *
 * @author ItzAbood69
 */
public class HeartManager {

    private static final String PDC_KEY = "heart_dimension";

    private final ExtraTeritory plugin;
    private final NamespacedKey heartKey;

    public HeartManager(ExtraTeritory plugin) {
        this.plugin = plugin;
        this.heartKey = new NamespacedKey(plugin, PDC_KEY);
    }

    // ── Item Creation ────────────────────────────────────────────────────────

    /**
     * Creates a Heart item for the given dimension.
     * The dimension is stored in the PDC so it can be read back reliably.
     */
    public ItemStack createHeart(Dimension dimension) {
        Material mat = getMaterialForDimension(dimension);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(colorize(dimension) + dimension.getDisplayName() + " Heart");
            meta.setLore(buildLore(dimension));
            meta.getPersistentDataContainer().set(heartKey, PersistentDataType.STRING, dimension.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    // ── Item Identification ──────────────────────────────────────────────────

    /**
     * Returns true if the given ItemStack is a valid ExtraTeritory Heart.
     */
    public boolean isHeart(ItemStack item) {
        return getDimensionFromHeart(item) != null;
    }

    /**
     * Reads the Dimension encoded in the heart's PersistentDataContainer.
     * Returns null if the item is not a heart.
     */
    public Dimension getDimensionFromHeart(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String raw = meta.getPersistentDataContainer().get(heartKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return Dimension.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Material getMaterialForDimension(Dimension dimension) {
        String cfgKey = "heart-items." + dimension.name().toLowerCase();
        String matName = plugin.getConfig().getString(cfgKey);
        if (matName != null) {
            try {
                return Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        // Fallback defaults
        switch (dimension) {
            case NETHER:    return Material.RED_DYE;
            case END:       return Material.PURPLE_DYE;
            case OVERWORLD: return Material.LIME_DYE;
            default:        return Material.GLOWSTONE_DUST;
        }
    }

    private String colorize(Dimension dimension) {
        switch (dimension) {
            case NETHER:    return "§c";
            case END:       return "§5";
            case OVERWORLD: return "§a";
            default:        return "§f";
        }
    }

    private List<String> buildLore(Dimension dimension) {
        return Arrays.asList(
                "§7Carry this in your inventory to gain",
                "§7immunity to §e" + dimension.getDisplayName() + "§7 dimension debuffs.",
                "",
                "§8Obtained through PvP combat only."
        );
    }
}
