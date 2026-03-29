package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Refreshes a player's tab-list display name whenever they change worlds,
 * ensuring the dimension prefix is always up-to-date.
 *
 * @author ItzAbood69
 */
public class TabListListener implements Listener {

    private final ExtraTeritory plugin;

    public TabListListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        refreshTab(event.getPlayer());
    }

    public void refreshTab(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        String prefix = plugin.getConfig().getString(
                "tab-colors." + data.getDimension().name().toLowerCase(), "");
        player.setPlayerListName(MessageUtil.colorize(prefix + player.getName()));
    }
}
