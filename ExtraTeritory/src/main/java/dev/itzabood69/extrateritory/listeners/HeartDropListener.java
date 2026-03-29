package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * When a player is killed by another player, drops a Heart item for the victim's dimension.
 * Hearts are the only cross-dimension immunity mechanic and cannot be obtained any other way.
 *
 * @author ItzAbood69
 */
public class HeartDropListener implements Listener {

    private final ExtraTeritory plugin;

    public HeartDropListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Must be killed by a player
        if (!(victim.getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent dmg)) return;
        if (!(dmg.getDamager() instanceof Player)) return;

        PlayerData data = plugin.getDataManager().getPlayerData(victim.getUniqueId());
        if (data == null) return;

        Dimension victimDimension = data.getDimension();
        ItemStack heart = plugin.getHeartManager().createHeart(victimDimension);

        // Drop at death location
        victim.getWorld().dropItemNaturally(victim.getLocation(), heart);
    }
}
