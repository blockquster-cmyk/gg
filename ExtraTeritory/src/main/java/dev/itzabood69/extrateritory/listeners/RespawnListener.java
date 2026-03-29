package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Overrides respawn location so players without a bed/anchor always return
 * to their dimension's fixed spawn instead of the world spawn.
 *
 * @author ItzAbood69
 */
public class RespawnListener implements Listener {

    private final ExtraTeritory plugin;

    public RespawnListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        // isBedSpawn() covers beds in Overworld; isAnchorSpawn() covers respawn anchors
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;

        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        Dimension dim = data.getDimension();
        Location spawn = plugin.getSpawnManager().getSpawn(dim);
        if (spawn == null) return;

        event.setRespawnLocation(spawn);
    }
}
