package dev.itzabood69.extrateritory.listeners;

import dev.itzabood69.extrateritory.ExtraTeritory;
import dev.itzabood69.extrateritory.models.Dimension;
import dev.itzabood69.extrateritory.models.PlayerData;
import dev.itzabood69.extrateritory.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles dimension abilities.
 *
 * Activation key is configurable in config.yml:
 *   ability-activation-key: F              # swap-hand key (default)
 *   ability-activation-key: SHIFT          # sneak toggle
 *   ability-activation-key: RIGHT_CLICK    # shift+right click
 *   ability-activation-key: LEFT_CLICK     # shift+left click
 *
 * OVERWORLD — Ground Dash
 *   Launches the player forward 10 blocks (configurable) instantly.
 *   Only works when standing on the ground.
 *   Breaks leaves and crops in the dash path.
 *
 * NETHER — Magma Walk
 *   Grants Fire Resistance for 10 seconds plus lava-walk protection.
 *
 * END — Void Blink
 *   Raytrace teleport in look direction (max 20 blocks configurable).
 *
 * @author ItzAbood69
 */
public class AbilityListener implements Listener {

    /** Supported activation key modes (set in config: ability-activation-key). */
    public enum ActivationKey {
        F,            // PlayerSwapHandItemsEvent  (default)
        SHIFT,        // PlayerToggleSneakEvent (sneak press, not hold)
        RIGHT_CLICK,  // PlayerInteractEvent — SHIFT + right click
        LEFT_CLICK    // PlayerInteractEvent — SHIFT + left click
    }

    private final ExtraTeritory plugin;

    public AbilityListener(ExtraTeritory plugin) {
        this.plugin = plugin;
        startAbilityIndicatorTask();
    }

    /**
     * Runs every 20 ticks (1 s). Shows action-bar feedback:
     *  - §a READY  — ability is off cooldown
     *  - §e Xsec   — cooldown remaining (counts down)
     */
    private void startAbilityIndicatorTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
                    if (data == null || data.isDimensionPending()) continue;

                    String keyHint = getActivationKeyHint();
                    String bar;
                    if (data.isAbilityOnCooldown()) {
                        long remaining = data.getRemainingCooldownSeconds();
                        bar = "§8[Ability] §e" + remaining + "s §8cooldown";
                    } else {
                        bar = "§8[Ability] §a§lREADY §8— press §e" + keyHint;
                    }
                    MessageUtil.sendActionBar(player, bar);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Returns a human-readable name for the configured activation key. */
    private String getActivationKeyHint() {
        return switch (getActivationKey()) {
            case F           -> "F";
            case SHIFT       -> "Shift";
            case RIGHT_CLICK -> "Shift+RClick";
            case LEFT_CLICK  -> "Shift+LClick";
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Event handlers — only the active key fires; the others are ignored
    // ══════════════════════════════════════════════════════════════════════════

    /** Default: F key (swap-hand). */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (getActivationKey() != ActivationKey.F) return;
        if (tryActivate(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** SHIFT activation: fires on sneak-toggle press (first sneak, not hold). */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (getActivationKey() != ActivationKey.SHIFT) return;
        if (!event.isSneaking()) return; // only on press, not release
        tryActivate(event.getPlayer());
    }

    /** RIGHT_CLICK / LEFT_CLICK: shift+click while holding any item. */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ActivationKey key = getActivationKey();
        if (key != ActivationKey.RIGHT_CLICK && key != ActivationKey.LEFT_CLICK) return;

        // Only fire from main hand to avoid double-fire
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        boolean isRightClick = (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK);
        boolean isLeftClick  = (event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK);

        if (key == ActivationKey.RIGHT_CLICK && !isRightClick) return;
        if (key == ActivationKey.LEFT_CLICK  && !isLeftClick)  return;

        if (tryActivate(player)) {
            event.setCancelled(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Core activation logic
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Attempts to fire the player's ability. Returns true if it fired (so callers
     * can cancel the original event).
     */
    private boolean tryActivate(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return false;

        if (data.isAbilityOnCooldown()) {
            long remaining = data.getRemainingCooldownSeconds();
            MessageUtil.send(player, plugin, "ability-cooldown", "time", String.valueOf(remaining));
            return true; // still consume the key press so it doesn't do default action
        }

        boolean used = activateAbility(player, data.getDimension());
        if (!used) return false;

        int cooldownSec = plugin.getConfig().getInt("ability-cooldown", 60);
        data.setAbilityCooldownExpiry(System.currentTimeMillis() + (cooldownSec * 1000L));
        plugin.getDataManager().savePlayer(data);
        MessageUtil.send(player, plugin, "ability-activated");
        return true;
    }

    // ── Ability dispatcher ────────────────────────────────────────────────────

    private boolean activateAbility(Player player, Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> activateGroundDash(player);
            case NETHER    -> activateMagmaWalk(player);
            case END       -> activateVoidBlink(player);
        };
    }

    // ── Config helper ─────────────────────────────────────────────────────────

    private ActivationKey getActivationKey() {
        String raw = plugin.getConfig().getString("ability-activation-key", "F").toUpperCase();
        try {
            return ActivationKey.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ActivationKey.F; // fallback to default
        }
    }

    // ── OVERWORLD: Ground Dash ───────────────────────────────────────────────

    private boolean activateGroundDash(Player player) {
        if (!player.isOnGround()) {
            player.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                            + "&cGround Dash only works when standing on the ground!"));
            return false;
        }

        double distance     = plugin.getConfig().getDouble("ability-overworld-dash-distance", 10.0);
        boolean breakBlocks = plugin.getConfig().getBoolean("ability-overworld-dash-break-blocks", true);

        org.bukkit.util.Vector dir      = player.getLocation().getDirection().setY(0).normalize();
        org.bukkit.util.Vector velocity = dir.clone().multiply(distance * 0.35);
        velocity.setY(0.3);
        player.setVelocity(velocity);

        if (breakBlocks) {
            plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                    new Runnable() {
                        int ticks = 0;
                        final org.bukkit.util.Vector step = dir.clone().multiply(0.5);
                        Location scan = player.getLocation().clone().add(0, 0.5, 0);

                        @Override
                        public void run() {
                            ticks++;
                            if (ticks > 20) return;
                            scan.add(step);
                            Block b = scan.getBlock();
                            if (isDashBreakable(b)) {
                                b.breakNaturally();
                                player.getWorld().spawnParticle(
                                        org.bukkit.Particle.BLOCK,
                                        b.getLocation().add(0.5, 0.5, 0.5),
                                        6, 0.2, 0.2, 0.2, 0,
                                        b.getBlockData());
                            }
                        }
                    }, 0L, 1L);
        }

        player.getWorld().spawnParticle(
                org.bukkit.Particle.SWEEP_ATTACK,
                player.getLocation().add(0, 1, 0),
                3, 0.2, 0.3, 0.2, 0.01);
        return true;
    }

    private boolean isDashBreakable(Block block) {
        Material m = block.getType();
        return m.name().contains("LEAVES")
                || m == Material.SHORT_GRASS
                || m == Material.TALL_GRASS
                || m == Material.FERN
                || m == Material.LARGE_FERN
                || m == Material.WHEAT
                || m == Material.CARROTS
                || m == Material.POTATOES
                || m == Material.BEETROOTS
                || m == Material.SUGAR_CANE
                || m == Material.VINE;
    }

    // ── NETHER: Magma Walk ────────────────────────────────────────────────────

    private boolean activateMagmaWalk(Player player) {
        int durationSec = plugin.getConfig().getInt("ability-nether-magma-duration", 10);
        int ticks       = durationSec * 20;

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, ticks, 0, false, true, true));

        player.getWorld().spawnParticle(
                org.bukkit.Particle.FLAME,
                player.getLocation().add(0, 1, 0),
                20, 0.3, 0.5, 0.3, 0.05);

        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline()) return;
            player.setFireTicks(0);
            if (player.getLocation().getBlock().getType() == Material.LAVA ||
                    player.getLocation().clone().subtract(0,1,0).getBlock().getType() == Material.LAVA) {
                org.bukkit.util.Vector vel = player.getVelocity();
                if (vel.getY() < 0) player.setVelocity(vel.setY(0.1));
            }
        }, 0L, 1L);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                () -> plugin.getServer().getScheduler().cancelTask(taskId), ticks);
        return true;
    }

    // ── END: Void Blink ───────────────────────────────────────────────────────

    private boolean activateVoidBlink(Player player) {
        double maxDistance = plugin.getConfig().getDouble("ability-end-blink-max-distance", 20.0);

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                maxDistance,
                org.bukkit.FluidCollisionMode.NEVER,
                true);

        if (result == null || result.getHitBlock() == null) {
            player.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                            + "&cNo solid block in range to blink to."));
            return false;
        }

        Block hit   = result.getHitBlock();
        BlockFace face = result.getHitBlockFace();
        if (face == null) face = BlockFace.UP;

        Location destination = hit.getRelative(face).getLocation().add(0.5, 0, 0.5);
        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());

        Block feet = destination.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        if (!feet.getType().isAir() || !head.getType().isAir()) {
            player.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("prefix", "&8[&6ExtraTeritory&8]&r ")
                            + "&cVoid Blink blocked — no space at target."));
            return false;
        }

        Location origin = player.getLocation();
        int steps = (int) origin.distance(destination);
        for (int i = 0; i <= steps; i++) {
            double t = (steps == 0) ? 0 : (double) i / steps;
            Location point = origin.clone().add(
                    (destination.getX() - origin.getX()) * t,
                    (destination.getY() - origin.getY()) * t + 1,
                    (destination.getZ() - origin.getZ()) * t);
            player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, point, 3, 0.1, 0.1, 0.1, 0.02);
        }

        player.teleport(destination);
        return true;
    }
}