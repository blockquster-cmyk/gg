package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Grants dimension immunity when a Heart enters the player's inventory.
 * Removes immunity when the Heart is dropped or the player dies (items leave inventory).
 *
 * @author ItzAbood69
 */
public class HeartPickupListener implements Listener {

    private final ExtraTeritory plugin;

    public HeartPickupListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    // ── Pickup ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        Dimension heartDim = plugin.getHeartManager().getDimensionFromHeart(item);
        if (heartDim == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Don't grant immunity to your own dimension (you already live there)
        if (heartDim == data.getDimension()) return;

        data.addHeartImmunity(heartDim);
        plugin.getDataManager().savePlayer(data);
        MessageUtil.send(player, plugin, "heart-immunity", "dimension", heartDim.getDisplayName());

        // Re-evaluate debuffs immediately
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(
                        new org.bukkit.event.player.PlayerChangedWorldEvent(
                                player, player.getWorld()
                        )
                ), 2L
        );
    }

    // ── Drop ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        Dimension heartDim = plugin.getHeartManager().getDimensionFromHeart(item);
        if (heartDim == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        data.removeHeartImmunity(heartDim);
        plugin.getDataManager().savePlayer(data);
        player.sendMessage(MessageUtil.colorize(
                plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                + "&cYou dropped your &e" + heartDim.getDisplayName() + " Heart&c — immunity lost."
        ));
    }

    // ── Death (inventory lost) ───────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(victim.getUniqueId());
        if (data == null) return;

        // Check which hearts were in their inventory and remove those immunities
        for (ItemStack item : victim.getInventory().getContents()) {
            if (item == null) continue;
            Dimension heartDim = plugin.getHeartManager().getDimensionFromHeart(item);
            if (heartDim != null) {
                data.removeHeartImmunity(heartDim);
            }
        }
        plugin.getDataManager().savePlayer(data);
    }
}
