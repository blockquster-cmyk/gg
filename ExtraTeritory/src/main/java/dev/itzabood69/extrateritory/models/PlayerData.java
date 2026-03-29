package dev.itzabood69.extrateritory.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Stores all persistent data for a single player in ExtraTeritory.
 *
 * @author ItzAbood69
 */
public class PlayerData {

    private final UUID uuid;
    private final String name;
    private Dimension dimension;
    private int pvpDeaths;
    private final Set<Dimension> heartImmunities; // dimensions this player is immune to
    private long abilityCooldownExpiry; // epoch ms when ability cooldown expires
    private boolean dimensionPending;   // true while the slot-roll is playing on first join

    public PlayerData(UUID uuid, String name, Dimension dimension) {
        this.uuid = uuid;
        this.name = name;
        this.dimension = dimension;
        this.pvpDeaths = 0;
        this.heartImmunities = new HashSet<>();
        this.abilityCooldownExpiry = 0L;
        this.dimensionPending = false;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public Set<Dimension> getHeartImmunities() {
        return heartImmunities;
    }

    public long getAbilityCooldownExpiry() {
        return abilityCooldownExpiry;
    }

    // ── Setters / Mutators ──────────────────────────────────────────────────

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public void incrementPvpDeaths() {
        this.pvpDeaths++;
    }

    public void setPvpDeaths(int pvpDeaths) {
        this.pvpDeaths = pvpDeaths;
    }

    public void addHeartImmunity(Dimension dimension) {
        heartImmunities.add(dimension);
    }

    public void removeHeartImmunity(Dimension dimension) {
        heartImmunities.remove(dimension);
    }

    public boolean hasHeartImmunity(Dimension dimension) {
        return heartImmunities.contains(dimension);
    }

    public void setAbilityCooldownExpiry(long expiry) {
        this.abilityCooldownExpiry = expiry;
    }

    public boolean isAbilityOnCooldown() {
        return System.currentTimeMillis() < abilityCooldownExpiry;
    }

    public long getRemainingCooldownSeconds() {
        long remaining = abilityCooldownExpiry - System.currentTimeMillis();
        return remaining > 0 ? (remaining / 1000) : 0;
    }

    public boolean isDimensionPending() {
        return dimensionPending;
    }

    public void setDimensionPending(boolean pending) {
        this.dimensionPending = pending;
    }
}