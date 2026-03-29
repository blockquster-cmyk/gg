package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Applies dimension-specific debuffs and health restrictions when a player
 * enters a world that does not belong to their assigned dimension.
 *
 * Changes in this version:
 *  - Dimension title/subtitle shown on world change (replaces plain chat message)
 *  - Poison is NEVER applied in the Nether if the player is at ≤ half hearts (10 hp)
 *    regardless of what config says — prevents death-loop on reduced-health cap
 *  - Action-bar warning instead of intrusive chat spam for debuffs
 *
 * @author ItzAbood69
 */
public class DimensionDebuffListener implements Listener {

    private static final int REFRESH_TICKS  = 80;   // 4 seconds
    private static final double HALF_HEALTH = 10.0; // 5 hearts

    private final ExtraTeritory plugin;

    public DimensionDebuffListener(ExtraTeritory plugin) {
        this.plugin = plugin;
        startDebuffTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        // Small delay so the world change settles before applying effects
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyOrRemoveDebuffs(player);
            showDimensionTitle(player);   // ← Display on-screen dimension name
        }, 10L);
    }

    // ── Core logic ───────────────────────────────────────────────────────────

    public void applyOrRemoveDebuffs(Player player) {
        if (!player.isOnline()) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;
        if (player.hasPermission("extrateritory.bypass")) {
            restoreHealth(player);
            return;
        }

        Dimension worldDim = plugin.getSpawnManager().getDimensionForWorld(player.getWorld());
        if (worldDim == null) {
            restoreHealth(player);
            return;
        }

        boolean isForeign  = worldDim != data.getDimension();
        boolean hasImmunity = data.hasHeartImmunity(worldDim);

        if (isForeign && !hasImmunity) {
            applyDebuffs(player, worldDim);
            capHealth(player);
        } else {
            removeDebuffs(player, worldDim);
            restoreHealth(player);
        }
    }

    // ── Dimension title overlay ──────────────────────────────────────────────

    private void showDimensionTitle(Player player) {
        if (!player.isOnline()) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        Dimension worldDim = plugin.getSpawnManager().getDimensionForWorld(player.getWorld());
        if (worldDim == null) return;

        boolean isForeign   = worldDim != data.getDimension();
        boolean hasImmunity = data.hasHeartImmunity(worldDim);

        // Play the 10-second slot-roll animation then show the dimension title
        MessageUtil.playDimensionSlotRoll(plugin, player, worldDim, isForeign && !hasImmunity);
    }

    // ── Potion effects ───────────────────────────────────────────────────────

    private void applyDebuffs(Player player, Dimension dimension) {
        List<String> effects = plugin.getConfig().getStringList(
                "debuffs." + dimension.name().toLowerCase());

        // POISON SAFETY: In the Nether (or any dimension with a health cap),
        // if the player is already at or below half hearts, NEVER apply Poison.
        // This prevents an inescapable death-loop: capped health + poison = instant kill.
        double healthCap = plugin.getConfig().getDouble("foreign-dimension-max-health", HALF_HEALTH);
        boolean atHalfHearts = player.getHealth() <= healthCap;

        for (String entry : effects) {
            String[] parts = entry.split(":");
            if (parts.length < 1) continue;

            PotionEffectType type = parsePotionType(parts[0]);
            if (type == null) continue;

            // Skip poison entirely when health is already at/below the cap — always, for ALL dimensions
            if (atHalfHearts && type.equals(PotionEffectType.POISON)) continue;

            int amplifier = (parts.length >= 2) ? parseInt(parts[1], 0) : 0;
            // Duration: 200 ticks (10 s) — refreshed every 80 ticks so it never expires naturally
            player.addPotionEffect(new PotionEffect(type, 200, amplifier, false, false, false));
        }

        // Show persistent action-bar warning instead of flooding chat
        MessageUtil.sendActionBar(player,
                "§c⚠ Foreign territory — §e" + dimension.getDisplayName() + " §c— health capped");
    }

    private void removeDebuffs(Player player, Dimension dimension) {
        List<String> effects = plugin.getConfig().getStringList(
                "debuffs." + dimension.name().toLowerCase());

        for (String entry : effects) {
            String[] parts = entry.split(":");
            if (parts.length < 1) continue;
            PotionEffectType type = parsePotionType(parts[0]);
            if (type != null) player.removePotionEffect(type);
        }
    }

    // ── Health cap ───────────────────────────────────────────────────────────

    private void capHealth(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        double cap = plugin.getConfig().getDouble("foreign-dimension-max-health", HALF_HEALTH);
        if (attr.getValue() > cap) {
            attr.setBaseValue(cap);
            if (player.getHealth() > cap) {
                player.setHealth(cap);
            }
        }
    }

    private void restoreHealth(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        double base = attr.getDefaultValue(); // 20.0 by default
        if (attr.getValue() < base) {
            attr.setBaseValue(base);
        }
    }

    // ── Background refresh task ──────────────────────────────────────────────

    private void startDebuffTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    applyOrRemoveDebuffs(player);
                }
            }
        }.runTaskTimer(plugin, REFRESH_TICKS, REFRESH_TICKS);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PotionEffectType parsePotionType(String name) {
        try {
            PotionEffectType type = PotionEffectType.getByName(name.toUpperCase());
            if (type != null) return type;
            for (PotionEffectType t : PotionEffectType.values()) {
                if (t.getName().equalsIgnoreCase(name)) return t;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }
}