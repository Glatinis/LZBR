package com.github.Glatinis.lZBR.core;

import org.bukkit.plugin.java.JavaPlugin;

public final class LZBR extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Loaded plugin");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloaded plugin");
    }
}
