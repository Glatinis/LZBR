package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.loot.chest.ChestFillSettings;
import com.github.Glatinis.lZBR.loot.chest.ChestLocation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

// Owns loot.yml (separate from config.yml). The single boundary between the loot feature and the file:
// exposes the fill settings, rarities and loot-table entries, and reads/writes the chest-location list
// that admins edit in-game. Translating chest records to/from YAML maps lives here so the models stay
// free of persistence concerns.
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

    public ChestFillSettings getChestFillSettings() {
        return new ChestFillSettings(
                config.getInt("settings.items-per-chest.minimum", 3),
                config.getInt("settings.items-per-chest.maximum", 7),
                config.getBoolean("settings.scatter-items", true));
    }

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
                chests.add(new ChestLocation(
                        String.valueOf(raw.get("world")),
                        toInt(raw.get("x")),
                        toInt(raw.get("y")),
                        toInt(raw.get("z")),
                        parseFacing(raw.get("facing"))));
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping malformed chest-location entry in loot.yml: " + raw);
            }
        }
        return chests;
    }

    public void saveChestLocations(List<ChestLocation> chests) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ChestLocation chest : chests) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("world", chest.world());
            map.put("x", chest.x());
            map.put("y", chest.y());
            map.put("z", chest.z());
            map.put("facing", chest.facing().name());
            serialized.add(map);
        }
        config.set("chest-locations", serialized);
        save();
    }

    // --- Helpers -------------------------------------------------------------

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save chest locations to loot.yml", e);
        }
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private static BlockFace parseFacing(Object value) {
        if (value == null) return BlockFace.NORTH;
        try {
            return BlockFace.valueOf(String.valueOf(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BlockFace.NORTH;
        }
    }
}
