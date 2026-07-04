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

    public void reload() {
        plugin.reloadConfig();
    }
}
