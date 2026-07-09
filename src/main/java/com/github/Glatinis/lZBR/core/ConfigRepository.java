package com.github.Glatinis.lZBR.core;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigRepository {
    private final LZBR plugin;

    public ConfigRepository(LZBR plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    // --- Players -------------------------------------------------------------

    public int getMinimumPlayerCount() {
        return config().getInt("players.minimum");
    }

    public int getMaximumPlayerCount() {
        return config().getInt("players.maximum");
    }

    // --- Worlds --------------------------------------------------------------

    public String getLobbyWorldName() {
        return config().getString("worlds.lobby");
    }

    public String getBrWorldName() {
        return config().getString("worlds.br");
    }

    // --- Zone ----------------------------------------------------------------

    public double getZoneCenterX() { return config().getDouble("zone.center.x"); }
    public double getZoneCenterZ() { return config().getDouble("zone.center.z"); }
    public double getZoneInitialRadius() { return config().getDouble("zone.radius.initial"); }
    public double getZoneFinalRadius() { return config().getDouble("zone.radius.final"); }
    public int getZoneShrinkDelay() { return config().getInt("zone.shrink.delay-seconds"); }
    public int getZoneShrinkDuration() { return config().getInt("zone.shrink.duration-seconds"); }
    public double getZoneDamageAmount() { return config().getDouble("zone.damage.amount"); }
    public int getZoneDamageInterval() { return config().getInt("zone.damage.interval-ticks"); }
    public int getZoneWarningDistance() { return config().getInt("zone.warning.distance-blocks"); }
    public int getZoneWarningTime() { return config().getInt("zone.warning.time-seconds"); }

    // --- Arena reset ---------------------------------------------------------

    public boolean isArenaResetEnabled() { return config().getBoolean("arena.reset.enabled", true); }
    public String getArenaSchematic() { return config().getString("arena.reset.schematic", "arena.schem"); }
    public int getArenaOriginX() { return config().getInt("arena.reset.origin.x"); }
    public int getArenaOriginY() { return config().getInt("arena.reset.origin.y"); }
    public int getArenaOriginZ() { return config().getInt("arena.reset.origin.z"); }
    public boolean isArenaPasteAir() { return config().getBoolean("arena.reset.paste-air", true); }
    public boolean isArenaPasteEntities() { return config().getBoolean("arena.reset.paste-entities", false); }

    public void reload() {
        plugin.reloadConfig();
    }
}
