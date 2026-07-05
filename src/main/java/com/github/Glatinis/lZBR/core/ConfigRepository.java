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

    public int getMinimumPlayerCount() {
        return config().getInt("minimum-player-count");
    }

    public int getMaximumPlayerCount() {
        return config().getInt("maximum-player-count");
    }

    public String getLobbyWorldName() {
        return config().getString("lobby-world-name");
    }

    public String getBrWorldName() {
        return config().getString("br-world-name");
    }

    public double getZoneCenterX() { return config().getDouble("zone.center-x"); }
    public double getZoneCenterZ() { return config().getDouble("zone.center-z"); }
    public double getZoneInitialRadius() { return config().getDouble("zone.initial-radius"); }
    public double getZoneFinalRadius() { return config().getDouble("zone.final-radius"); }
    public int getZoneShrinkDelay() { return config().getInt("zone.shrink-delay"); }
    public int getZoneShrinkDuration() { return config().getInt("zone.shrink-duration"); }
    public double getZoneDamageAmount() { return config().getDouble("zone.damage-amount"); }
    public int getZoneDamageInterval() { return config().getInt("zone.damage-interval"); }
    public int getZoneWarningDistance() { return config().getInt("zone.warning-distance"); }
    public int getZoneWarningTime() { return config().getInt("zone.warning-time"); }

    public void reload() {
        plugin.reloadConfig();
    }
}
