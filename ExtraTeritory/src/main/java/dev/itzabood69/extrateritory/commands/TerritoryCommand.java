package dev.itzabood69.extrateritory.commands;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.populators.StructurePopulator;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all /territory (alias: /et) subcommands.
 *
 * <pre>
 * ── Player commands ───────────────────────────────────────────────────────
 *   /territory help
 *   /territory info [player]
 *   /territory locate structure <name>   ← finds nearest generated instance
 *
 * ── Admin commands (extrateritory.admin) ──────────────────────────────────
 *   /territory setdimension <player> <dim>
 *   /territory setspawn <dim>
 *   /territory revive <player>
 *   /territory stats
 *   /territory reload
 * </pre>
 *
 * @author ItzAbood69
 */
public class TerritoryCommand implements CommandExecutor, TabCompleter {

    private final ExtraTeritory plugin;

    public TerritoryCommand(ExtraTeritory plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  onCommand
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "help"                                            -> sendHelp(sender);
            case "info"                                            -> handleInfo(sender, args);
            case "locate"                                          -> handleLocate(sender, args);
            case "setdimension", "setdim", "changedimension",
                 "changedim"                                       -> handleSetDimension(sender, args);
            case "setspawn"                                        -> handleSetSpawn(sender, args);
            case "revive"                                          -> handleRevive(sender, args);
            case "stats"                                           -> handleStats(sender);
            case "reload"                                          -> handleReload(sender);
            default -> sender.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                    + "&cUnknown subcommand. Use &e/territory help &cfor a list."));
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory help
    // ═══════════════════════════════════════════════════════════════════════

    private void sendHelp(CommandSender sender) {
        boolean admin = sender.hasPermission("extrateritory.admin");
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        sender.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME + "  &7— Commands"));
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        sender.sendMessage(MessageUtil.colorize("  &e/territory info &8[player]               &7Dimension info"));
        sender.sendMessage(MessageUtil.colorize("  &e/territory locate structure &8<name>     &7Find nearest structure"));
        if (admin) {
            sender.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            sender.sendMessage(MessageUtil.colorize("  &c/territory setdimension &8<player> <dim>  &7Change dimension"));
            sender.sendMessage(MessageUtil.colorize("  &c/territory setspawn &8<dim>              &7Set spawn at your pos"));
            sender.sendMessage(MessageUtil.colorize("  &c/territory revive &8<player>             &7Unban a player"));
            sender.sendMessage(MessageUtil.colorize("  &c/territory stats                         &7Population counts"));
            sender.sendMessage(MessageUtil.colorize("  &c/territory reload                        &7Reload config"));
        }
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory info [player]
    // ═══════════════════════════════════════════════════════════════════════

    private void handleInfo(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("extrateritory.admin")) {
                MessageUtil.sendNoPermission(sender); return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) { MessageUtil.send(sender, plugin, "player-not-found"); return; }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Usage: /territory info <player>"); return;
            }
            target = p;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (data == null) { MessageUtil.send(sender, plugin, "player-not-found"); return; }

        Set<Dimension> immunities = data.getHeartImmunities();
        String immStr = immunities.isEmpty()
                ? "&cNone"
                : immunities.stream().map(d -> "&e" + d.getDisplayName()).collect(Collectors.joining("&7, "));

        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        sender.sendMessage(MessageUtil.colorize("  &6" + data.getName() + " &7— Territory Info"));
        sender.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
        sender.sendMessage(MessageUtil.colorize("  &7Dimension  &8: &e" + data.getDimension().getDisplayName()));
        sender.sendMessage(MessageUtil.colorize("  &7PvP Deaths &8: &c" + data.getPvpDeaths()
                + " &7/ &c" + plugin.getConfig().getInt("pvp-deaths-until-ban", 10)));
        sender.sendMessage(MessageUtil.colorize("  &7Immunities &8: " + immStr));
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory locate structure <name>
    // ═══════════════════════════════════════════════════════════════════════

    private void handleLocate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /territory locate."); return;
        }

        // Usage guard
        if (args.length < 3 || !args[1].equalsIgnoreCase("structure")) {
            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            player.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME));
            player.sendMessage(MessageUtil.colorize("  &7Usage: &e/territory locate structure &8<name>"));
            player.sendMessage(MessageUtil.colorize("  &7Example: &e/territory locate structure ancient-well"));
            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            return;
        }

        String name = args[2].toLowerCase();

        // Validate name
        if (!StructurePopulator.allStructureNames().contains(name)) {
            // Try partial match for helpful error
            String suggestion = StructurePopulator.allStructureNames().stream()
                    .filter(s -> s.contains(name) || name.contains(s.substring(0, Math.min(4, s.length()))))
                    .findFirst().orElse(null);

            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            player.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME));
            player.sendMessage(MessageUtil.colorize("  &cUnknown structure: &e" + name));
            if (suggestion != null)
                player.sendMessage(MessageUtil.colorize("  &7Did you mean &e" + suggestion + "&7?"));
            player.sendMessage(MessageUtil.colorize("  &7Use &e/territory locate structure &8<TAB> &7to see all names."));
            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            return;
        }

        // Search for nearest placed instance
        Location nearest = StructurePopulator.findNearest(name, player.getLocation());

        if (nearest == null) {
            // Structure valid but hasn't generated near you yet
            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            player.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME));
            player.sendMessage(MessageUtil.colorize("  &e" + formatName(name) + " &7has not been generated yet."));
            player.sendMessage(MessageUtil.colorize("  &7Explore more of the world for it to appear!"));
            player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
            return;
        }

        // Calculate distance
        double dist = player.getLocation().distance(nearest);
        String distStr = dist < 1000
                ? String.format("%.0f", dist) + " blocks"
                : String.format("%.1f", dist / 1000) + "k blocks";

        // Show the styled result — title on screen + chat message
        MessageUtil.sendQuickTitle(player,
                "&6" + formatName(name),
                "&7Found at X:" + nearest.getBlockX() + " Z:" + nearest.getBlockZ()
                        + "  &8(" + distStr + " away)");

        player.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        player.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME + "  &7— Locate"));
        player.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
        player.sendMessage(MessageUtil.colorize("  &7Structure &8: &6" + formatName(name)));
        player.sendMessage(MessageUtil.colorize("  &7Location  &8: &eX:" + nearest.getBlockX()
                + "  Y:" + nearest.getBlockY()
                + "  Z:" + nearest.getBlockZ()));
        player.sendMessage(MessageUtil.colorize("  &7Distance  &8: &a" + distStr));
        player.sendMessage(MessageUtil.colorize("  &7World     &8: &7" + nearest.getWorld().getName()));
        player.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
    }

    /** Converts kebab-case to Title Case for display. */
    private String formatName(String key) {
        String[] parts = key.split("-");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)))
                                .append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory setdimension
    // ═══════════════════════════════════════════════════════════════════════

    private void handleSetDimension(CommandSender sender, String[] args) {
        if (!sender.hasPermission("extrateritory.admin")) { MessageUtil.sendNoPermission(sender); return; }
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.colorize("&cUsage: &e/territory setdimension <player> <overworld|nether|end>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { MessageUtil.send(sender, plugin, "player-not-found"); return; }

        Dimension newDim = Dimension.fromString(args[2]);
        if (newDim == null) { MessageUtil.send(sender, plugin, "invalid-dimension"); return; }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (data == null) { MessageUtil.send(sender, plugin, "player-not-found"); return; }

        Dimension oldDim = data.getDimension();
        plugin.getDataManager().setPlayerDimension(data, newDim);

        String tabPrefix = plugin.getConfig().getString("tab-colors." + newDim.name().toLowerCase(), "");
        target.setPlayerListName(MessageUtil.colorize(tabPrefix + target.getName()));

        Location spawn = plugin.getSpawnManager().getSpawn(newDim);
        if (spawn != null) target.teleport(spawn);

        plugin.getServer().getPluginManager().callEvent(
                new org.bukkit.event.player.PlayerChangedWorldEvent(target, target.getWorld()));

        MessageUtil.send(sender, plugin, "dimension-set",
                "player", target.getName(), "dimension", newDim.getDisplayName());
        MessageUtil.send(target, plugin, "dimension-set-self",
                "dimension", newDim.getDisplayName());

        plugin.getLogger().info(sender.getName() + " changed " + target.getName()
                + "'s dimension: " + oldDim.getDisplayName() + " → " + newDim.getDisplayName());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory setspawn
    // ═══════════════════════════════════════════════════════════════════════

    private void handleSetSpawn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("extrateritory.admin")) { MessageUtil.sendNoPermission(sender); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can set a spawn point."); return; }
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.colorize("&cUsage: &e/territory setspawn <overworld|nether|end>")); return;
        }
        Dimension dim = Dimension.fromString(args[1]);
        if (dim == null) { MessageUtil.send(sender, plugin, "invalid-dimension"); return; }

        plugin.getSpawnManager().setSpawn(dim, player.getLocation());
        MessageUtil.send(sender, plugin, "spawn-set", "dimension", dim.getDisplayName());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory revive
    // ═══════════════════════════════════════════════════════════════════════

    private void handleRevive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("extrateritory.admin")) { MessageUtil.sendNoPermission(sender); return; }
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.colorize("&cUsage: &e/territory revive <player>")); return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayerIfCached(targetName);
        ProfileBanList banList = Bukkit.getBanList(org.bukkit.BanList.Type.PROFILE);

        boolean isBanned = offlineTarget != null
                && banList.getBanEntry(offlineTarget.getPlayerProfile()) != null;
        if (!isBanned) {
            sender.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                    + "&e" + targetName + " &cis not banned."));
            return;
        }

        banList.pardon(offlineTarget.getPlayerProfile());

        for (PlayerData pd : plugin.getDataManager().getAllPlayerData()) {
            if (pd.getName().equalsIgnoreCase(targetName)) {
                pd.setPvpDeaths(0);
                plugin.getDataManager().savePlayer(pd);
                break;
            }
        }

        Bukkit.broadcastMessage(MessageUtil.format(plugin, "player-revived", "player", targetName));
        plugin.getLogger().info(sender.getName() + " revived banned player: " + targetName);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory stats
    // ═══════════════════════════════════════════════════════════════════════

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("extrateritory.admin")) { MessageUtil.sendNoPermission(sender); return; }

        Map<Dimension, Integer> counts = plugin.getDataManager().getDimensionCounts();
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
        sender.sendMessage(MessageUtil.colorize("  " + MessageUtil.PLUGIN_NAME + "  &7— Population"));
        sender.sendMessage(MessageUtil.colorize(MessageUtil.THIN_LINE));
        for (Dimension d : Dimension.values()) {
            sender.sendMessage(MessageUtil.colorize(
                    "  " + d.getTabPrefix() + d.getDisplayName()
                    + "  &8» &e" + counts.getOrDefault(d, 0) + " &7players"));
        }
        sender.sendMessage(MessageUtil.colorize(MessageUtil.SEPARATOR));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  /territory reload
    // ═══════════════════════════════════════════════════════════════════════

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("extrateritory.admin")) { MessageUtil.sendNoPermission(sender); return; }
        plugin.reloadConfig();
        sender.sendMessage(MessageUtil.colorize(
                plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                + "&aConfig reloaded successfully."));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Tab completion
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean admin = sender.hasPermission("extrateritory.admin");

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("help", "info", "locate"));
            if (admin) subs.addAll(List.of("setdimension", "setspawn", "revive", "stats", "reload"));
            return filter(subs, args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "info" -> admin ? onlineNames(args[1]) : Collections.emptyList();
                case "locate" -> filter(List.of("structure"), args[1]);
                case "setdimension", "setdim", "changedimension", "changedim" ->
                        admin ? onlineNames(args[1]) : Collections.emptyList();
                case "setspawn" -> admin ? dimNames(args[1]) : Collections.emptyList();
                case "revive"   -> admin ? bannedNames(args[1]) : Collections.emptyList();
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if ((sub.equals("setdimension") || sub.equals("setdim") ||
                 sub.equals("changedimension") || sub.equals("changedim")) && admin)
                return dimNames(args[2]);
            if (sub.equals("locate") && args[1].equalsIgnoreCase("structure"))
                return filter(StructurePopulator.allStructureNames(), args[2]);
        }

        return Collections.emptyList();
    }

    // ── Tab helpers ───────────────────────────────────────────────────────────

    private List<String> filter(List<String> list, String partial) {
        String lp = partial.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lp)).collect(Collectors.toList());
    }

    private List<String> onlineNames(String partial) {
        return filter(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName).collect(Collectors.toList()), partial);
    }

    private List<String> bannedNames(String partial) {
        List<String> banned = new ArrayList<>();
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (op.isBanned() && op.getName() != null) banned.add(op.getName());
        }
        return filter(banned, partial);
    }

    private List<String> dimNames(String partial) {
        return filter(List.of("overworld", "nether", "end"), partial);
    }
}
