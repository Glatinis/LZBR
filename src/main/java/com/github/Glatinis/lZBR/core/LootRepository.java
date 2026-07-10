package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.loot.ChestLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Owns loot.yml (separate from config.yml). Exposes the raw fill settings, rarities and loot-table
// entries, and reads/writes the chest-location list that admins edit in-game.
public class LootRepository {
    private static final String FILE_NAME = "loot.yml";

    private final LZBR plugin;
    private final File file;
    private FileConfiguration config;

    public LootRepository(LZBR plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    // --- Fill settings -------------------------------------------------------

    public int getMinItemsPerChest() { return config.getInt("settings.items-per-chest.minimum", 3); }
    public int getMaxItemsPerChest() { return config.getInt("settings.items-per-chest.maximum", 7); }
    public boolean isScatterItems() { return config.getBoolean("settings.scatter-items", true); }

    // --- Loot table ----------------------------------------------------------

    public ConfigurationSection getRaritiesSection() {
        return config.getConfigurationSection("rarities");
    }

    public List<Map<?, ?>> getLootEntries() {
        return config.getMapList("loot");
    }

    // --- Chest locations -----------------------------------------------------

    public List<ChestLocation> loadChestLocations() {
        List<ChestLocation> chests = new ArrayList<>();
        for (Map<?, ?> raw : config.getMapList("chest-locations")) {
            try {
                chests.add(ChestLocation.fromMap(raw));
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping malformed chest-location entry in loot.yml: " + raw);
            }
        }
        return chests;
    }

    public void saveChestLocations(List<ChestLocation> chests) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ChestLocation chest : chests) {
            serialized.add(chest.toMap());
        }
        config.set("chest-locations", serialized);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save chest locations to loot.yml", e);
        }
    }
}
