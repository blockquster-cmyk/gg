package dev.itzabood69.extrateritory.utils;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Central utility for sending consistently formatted plugin messages.
 * Supports chat messages, title/subtitle overlays, and action-bar messages.
 *
 * @author ItzAbood69
 */
public final class MessageUtil {

    // ── Decorative separators ────────────────────────────────────────────────

    /** Full-width separator line used in console logs and help menus. */
    public static final String SEPARATOR =
            "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    /** Short separator used inside bordered panels. */
    public static final String THIN_LINE =
            "§8§m──────────────────────────────────────────";

    /** The styled plugin name shown in titles and error headers. */
    public static final String PLUGIN_NAME = "§6✦ §lExtraTeritory §6✦";

    private MessageUtil() {}

    // ══ Chat messages ════════════════════════════════════════════════════════

    public static String format(ExtraTeritory plugin, String key) {
        String prefix  = colorize(plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r "));
        String message = plugin.getConfig().getString("messages." + key, "§cMissing message: " + key);
        return colorize(prefix + message);
    }

    public static String format(ExtraTeritory plugin, String key, String... placeholders) {
        String msg = format(plugin, key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return msg;
    }

    public static void send(CommandSender sender, ExtraTeritory plugin, String key, String... placeholders) {
        sender.sendMessage(format(plugin, key, placeholders));
    }

    // ══ Permission / error messages ══════════════════════════════════════════

    /**
     * Sends a professional, styled "no permission" error to the sender.
     */
    public static void sendNoPermission(CommandSender sender) {
        sender.sendMessage(colorize(THIN_LINE));
        sender.sendMessage(colorize("  " + PLUGIN_NAME));
        sender.sendMessage(colorize("  §cYou don't have permission to do that."));
        sender.sendMessage(colorize(THIN_LINE));
        if (sender instanceof Player player) {
            sendActionBar(player, "§c✘ No permission  §8|  §6ExtraTeritory");
        }
    }

    /**
     * Sends a styled error message with the plugin brand as context header.
     */
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(colorize(THIN_LINE));
        sender.sendMessage(colorize("  " + PLUGIN_NAME));
        sender.sendMessage(colorize("  §c" + message));
        sender.sendMessage(colorize(THIN_LINE));
    }

    // ══ Title / subtitle overlays ════════════════════════════════════════════

    // ══ Slot-roll animation ══════════════════════════════════════════════════

    // Internal slot entries: label shown in the action bar during the roll
    private static final String[][] SLOT_ENTRIES = {
            { "§a§l⬡ Overworld", "§a" },
            { "§c§l⬡ The Nether", "§c" },
            { "§5§l⬡ The End",    "§5" }
    };

    /**
     * Plays a 10-second slot-machine roll on the action bar, then settles
     * on the real destination dimension.
     *
     * Timing:
     *  - Ticks 0-10  (0-5 s)  : fast spin every 4 ticks  (~0.2 s per frame)
     *  - Ticks 10-16 (5-8 s)  : slow spin every 10 ticks (~0.5 s per frame)
     *  - Ticks 16-20 (8-10 s) : one final frame, then done
     *
     * After the roll finishes the normal dimension title is shown.
     *
     * @param plugin    the plugin instance (needed to schedule tasks)
     * @param player    the recipient
     * @param landed    the dimension they actually entered
     * @param isForeign true if this is a foreign dimension for them
     */
    public static void playDimensionSlotRoll(ExtraTeritory plugin,
                                             Player player,
                                             Dimension landed,
                                             boolean isForeign) {

        // Build a schedule of (delayTicks, slotIndex) pairs that simulate
        // a fast-then-slow spin landing on the correct dimension.
        // Total frames: 10 fast + 6 slow + 1 final = 17 frames over ~200 ticks (10 s)
        final int[] schedule = buildSchedule(landed);

        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                if (frame >= schedule.length) {
                    // Roll finished — show the proper dimension title
                    cancel();
                    sendDimensionTitle(player, landed, isForeign);
                    return;
                }

                int slotIdx = schedule[frame];
                String[] entry = SLOT_ENTRIES[slotIdx];
                String color   = entry[1];

                // Build a visual "[ PREV | >>> CURRENT <<< | NEXT ]" bar
                int prev = (slotIdx + 2) % 3;   // wrap backwards
                int next = (slotIdx + 1) % 3;

                String bar = "§8[ "
                        + dim(SLOT_ENTRIES[prev][0], "§8") + " §8| "
                        + color + "§l» " + stripColor(entry[0]) + " «" + " §8| "
                        + dim(SLOT_ENTRIES[next][0], "§8") + " §8]";

                sendActionBar(player, bar);
                frame++;
            }

            // Run every 10 ticks (0.5 s) — fast frames are repeated via schedule
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * Builds the sequence of slot indices for the animation.
     * Fast phase: cycles quickly; slow phase: slows near the target.
     */
    private static int[] buildSchedule(Dimension landed) {
        int target = landed == Dimension.OVERWORLD ? 0
                : landed == Dimension.NETHER    ? 1
                : 2;

        // 20 total frames at 10-tick intervals = 200 ticks = 10 seconds
        int totalFrames = 20;
        int[] sched = new int[totalFrames];

        // Fast phase: frames 0-13 spin freely
        for (int i = 0; i < 14; i++) {
            sched[i] = i % 3;
        }

        // Slow phase: frames 14-18 — each step lands one closer to target
        // Starting from wherever the fast phase left off
        int cur = sched[13];
        for (int i = 14; i < 19; i++) {
            cur = (cur + 1) % 3;
            sched[i] = cur;
        }

        // Final frame: force land on target
        sched[19] = target;
        // Also nudge frame 18 to be the slot before target so the last
        // step feels like it "clicks" into place
        sched[18] = (target + 2) % 3;

        return sched;
    }

    /** Strips the leading color/format codes from a slot label for display. */
    private static String stripColor(String label) {
        return label.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    /** Dims a slot label to the given color (used for prev/next slots). */
    private static String dim(String label, String color) {
        return color + stripColor(label);
    }

    /**
     * Shows a dimension-entry title screen to the player.
     * Called whenever a player enters a new dimension world.
     *
     * @param player    the recipient
     * @param dimension the dimension they just entered
     * @param isForeign true if this is a foreign (debuffed) dimension for them
     */
    public static void sendDimensionTitle(Player player, Dimension dimension, boolean isForeign) {
        String title;
        String subtitle;

        switch (dimension) {
            case OVERWORLD -> {
                title    = colorize("&a&l⬡ Overworld");
                subtitle = isForeign
                        ? colorize("&c⚠ Foreign territory — debuffs active")
                        : colorize("&7Your home dimension");
            }
            case NETHER -> {
                title    = colorize("&c&l⬡ The Nether");
                subtitle = isForeign
                        ? colorize("&4⚠ Foreign territory — debuffs active")
                        : colorize("&7Your home dimension");
            }
            case END -> {
                title    = colorize("&5&l⬡ The End");
                subtitle = isForeign
                        ? colorize("&d⚠ Foreign territory — debuffs active")
                        : colorize("&7Your home dimension");
            }
            default -> {
                title    = colorize("&e&lUnknown Dimension");
                subtitle = colorize("&7Entering unknown territory");
            }
        }

        player.sendTitle(title, subtitle, 10, 70, 20);
    }

    /** Shows a general-purpose title overlay. */
    public static void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(colorize(title), colorize(subtitle), 10, 60, 20);
    }

    /** Quick title that disappears faster (ability activations, etc.). */
    public static void sendQuickTitle(Player player, String title, String subtitle) {
        player.sendTitle(colorize(title), colorize(subtitle), 5, 30, 10);
    }

    // ══ Action bar ═══════════════════════════════════════════════════════════

    /** Sends a message to the player's action bar (above the hot-bar). */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(colorize(message)));
    }

    // ══ Utility ══════════════════════════════════════════════════════════════

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}