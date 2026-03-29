package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

/**
 * Intercepts common TPA commands from popular plugins (EssentialsX, CMI, etc.)
 * and cancels any cross-dimension teleport request.
 *
 * @author ItzAbood69
 */
public class TpaListener implements Listener {

    // Commands considered TPA-style — lowercase, no leading slash
    private static final Set<String> TPA_COMMANDS = Set.of(
            "tpa", "tpask", "tpahere", "tpyes", "tpaccept",
            "etpa", "etpahere"                               // EssentialsX aliases
    );

    private final ExtraTeritory plugin;

    public TpaListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().toLowerCase().trim();
        if (!raw.startsWith("/")) return;

        // Strip leading slash and split into parts
        String[] parts = raw.substring(1).split("\\s+");
        String cmd = parts[0];

        if (!TPA_COMMANDS.contains(cmd)) return;
        if (parts.length < 2) return; // no target specified — let the TPA plugin handle usage errors

        Player sender = event.getPlayer();
        Player target = plugin.getServer().getPlayer(parts[1]);
        if (target == null) return; // player not found — let normal handling show the error

        PlayerData senderData = plugin.getDataManager().getPlayerData(sender.getUniqueId());
        PlayerData targetData = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (senderData == null || targetData == null) return;

        if (senderData.getDimension() != targetData.getDimension()) {
            event.setCancelled(true);
            MessageUtil.send(sender, plugin, "tpa-blocked");
        }
    }
}
