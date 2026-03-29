package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Date;

/**
 * Enforces same-dimension combat immunity, tracks PvP deaths, and bans players
 * who reach the configured death threshold.
 *
 * Fixed: now also blocks projectile attacks (arrows, tridents, etc.)
 * between players in the same dimension.
 *
 * @author ItzAbood69
 */
public class PvPListener implements Listener {

    private final ExtraTeritory plugin;

    public PvPListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event);
        if (attacker == null) return;

        if (attacker.hasPermission("extrateritory.bypass")) return;

        PlayerData victimData   = plugin.getDataManager().getPlayerData(victim.getUniqueId());
        PlayerData attackerData = plugin.getDataManager().getPlayerData(attacker.getUniqueId());
        if (victimData == null || attackerData == null) return;

        if (victimData.getDimension() == attackerData.getDimension()) {
            event.setCancelled(true);
            MessageUtil.send(attacker, plugin, "same-dimension-pvp");
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player p) return p;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!(victim.getLastDamageCause() instanceof EntityDamageByEntityEvent dmg)) return;

        Player killer = resolveAttacker(dmg);
        if (killer == null) return;

        PlayerData data = plugin.getDataManager().getPlayerData(victim.getUniqueId());
        if (data == null) return;

        data.incrementPvpDeaths();
        plugin.getDataManager().savePlayer(data);

        int maxDeaths = plugin.getConfig().getInt("pvp-deaths-until-ban", 10);
        int current   = data.getPvpDeaths();

        if (current >= maxDeaths) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> banPlayer(victim), 1L);
        } else {
            victim.sendMessage(MessageUtil.format(plugin, "pvp-deaths-warning",
                    "deaths", String.valueOf(current)));
        }
    }

    private void banPlayer(Player player) {
        String reason = "You have been banned after reaching 10 PvP deaths in ExtraTeritory.";
        ProfileBanList banList = Bukkit.getBanList(org.bukkit.BanList.Type.PROFILE);
        banList.addBan(player.getPlayerProfile(), reason, (Date) null, "ExtraTeritory");
        player.kickPlayer(MessageUtil.colorize("&c" + reason));
        Bukkit.broadcastMessage(MessageUtil.format(plugin, "player-banned", "player", player.getName()));
    }
}