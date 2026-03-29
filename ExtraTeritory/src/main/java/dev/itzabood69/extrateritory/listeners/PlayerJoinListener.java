package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles first-time join dimension assignment.
 *
 * On first join:
 *  1. Player marked dimensionPending → fully invincible.
 *  2. 15-second slot machine runs AS A TITLE in the centre of the screen.
 *     Title   = the currently spinning dimension name (big, coloured).
 *     Subtitle = "[ PREV  |  >>> CURRENT <<<  |  NEXT ]" indicator row.
 *  3. Lands on assigned dimension → reveal title + teleport + pending cleared.
 *
 * @author ItzAbood69
 */
public class PlayerJoinListener implements Listener {

    // [displayName, boldColorCode, plain subtitle color]
    private static final String[][] SLOTS = {
        { "OVERWORLD", "§a§l", "§a" },
        { "THE NETHER", "§c§l", "§c" },
        { "THE END",    "§5§l", "§5" }
    };

    // 30 frames × 10 ticks (0.5 s each) = 300 ticks = 15 seconds
    private static final int TOTAL_FRAMES = 30;
    private static final int SLOW_START   = 20; // fast phase ends here

    private final ExtraTeritory plugin;

    public PlayerJoinListener(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasPlayerData(player.getUniqueId())) {
            // First join — create data, mark invincible
            PlayerData data = plugin.getDataManager().createPlayerData(
                    player.getUniqueId(), player.getName());
            data.setDimensionPending(true);

            // Wait 40 ticks (2 s) for the world to load, then start roll
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                playSlotRoll(player, data);
            }, 40L);

        } else {
            updateTabName(player);
        }
    }

    // ── Slot machine (title-based) ────────────────────────────────────────────

    private void playSlotRoll(Player player, PlayerData data) {
        Dimension assigned = data.getDimension();
        int target = assigned == Dimension.OVERWORLD ? 0
                   : assigned == Dimension.NETHER    ? 1
                   : 2;

        int[] schedule = buildSchedule(target);

        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                if (frame >= TOTAL_FRAMES) {
                    cancel();
                    finishRoll(player, data, assigned);
                    return;
                }

                int idx  = schedule[frame];
                int prev = (idx + 2) % 3;
                int next = (idx + 1) % 3;

                // Title = big spinning dimension name
                String title = SLOTS[idx][1] + "⬡ " + SLOTS[idx][0];

                // Subtitle = three-slot visual row
                String subtitle = "§8" + SLOTS[prev][2] + SLOTS[prev][0]
                        + "  §8|  "
                        + SLOTS[idx][1] + "» " + SLOTS[idx][0] + " «"
                        + "  §8|  "
                        + SLOTS[next][2] + SLOTS[next][0];

                // fadeIn=0, stay=15 ticks (slightly more than 10 so no flicker), fadeOut=0
                player.sendTitle(title, subtitle, 0, 15, 0);
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * Frames 0-19: fast free spin. Frames 20-28: slow toward target. Frame 29: snap.
     */
    private int[] buildSchedule(int target) {
        int[] s = new int[TOTAL_FRAMES];
        for (int i = 0; i < SLOW_START; i++) s[i] = i % 3;

        int cur = s[SLOW_START - 1];
        for (int i = SLOW_START; i < TOTAL_FRAMES - 1; i++) {
            cur = (cur + 1) % 3;
            s[i] = cur;
        }
        s[TOTAL_FRAMES - 2] = (target + 2) % 3;
        s[TOTAL_FRAMES - 1] = target;
        return s;
    }

    // ── Reveal ────────────────────────────────────────────────────────────────

    private void finishRoll(Player player, PlayerData data, Dimension assigned) {
        data.setDimensionPending(false);
        plugin.getDataManager().savePlayer(data);

        int idx = assigned == Dimension.OVERWORLD ? 0
                : assigned == Dimension.NETHER    ? 1
                : 2;

        String color = SLOTS[idx][2];

        // Big reveal title — stays 4 seconds
        player.sendTitle(
            color + "§l✔ " + SLOTS[idx][0],
            "§7This is your dimension. Survive.",
            5, 80, 20
        );

        // Chat
        player.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        player.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME));
        player.sendMessage(MessageUtil.colorize("  §7Welcome, §e" + player.getName() + "§7!"));
        player.sendMessage(MessageUtil.colorize(
            "  §7Your dimension: " + color + assigned.getDisplayName()));
        player.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));

        // Teleport
        Location spawn = plugin.getSpawnManager().getSpawn(assigned);
        if (spawn != null) player.teleport(spawn);

        updateTabName(player);
    }

    private void updateTabName(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;
        String prefix = plugin.getConfig().getString(
            "tab-colors." + data.getDimension().name().toLowerCase(), "");
        player.setPlayerListName(MessageUtil.colorize(prefix + player.getName()));
    }
}