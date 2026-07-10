package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.mob.DropSettings;
import com.github.Glatinis.lZBR.mob.spawn.SpawnPoint;
import com.github.Glatinis.lZBR.mob.spawn.SpawnSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Owns mobs.yml. The single boundary between the mob feature and the file: exposes the spawn and drop
// settings as typed records, the mob-type weights, and reads/writes the spawn-location list that admins
// edit in-game. Translating spawn points to/from YAML maps lives here so the models stay pure.
public class MobRepository {
    private static final String FILE_NAME = "mobs.yml";

    private final LZBR plugin;
    private final File file;
    private FileConfiguration config;

    public MobRepository(LZBR plugin) {
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

    // --- Settings ------------------------------------------------------------

    public boolean isEnabled() {
        return config.getBoolean("settings.enabled", true);
    }

    public SpawnSettings getSpawnSettings() {
        return new SpawnSettings(
                config.getInt("spawning.interval-seconds", 20),
                config.getInt("spawning.per-tick.minimum", 0),
                config.getInt("spawning.per-tick.maximum", 2),
                config.getInt("spawning.max-alive", 12),
                config.getInt("spawning.spread-radius", 6),
                config.getInt("spawning.min-player-distance", 12),
                config.getInt("spawning.stop-below-zone-radius", 60));
    }

    public DropSettings getDropSettings() {
        return new DropSettings(
                config.getDouble("drops.chance", 25.0),
                config.getInt("drops.rolls.minimum", 1),
                config.getInt("drops.rolls.maximum", 1),
                config.getBoolean("drops.keep-vanilla-drops", true));
    }

    // --- Mob types -----------------------------------------------------------

    public ConfigurationSection getMobTypesSection() {
        return config.getConfigurationSection("mob-types");
    }

    // --- Spawn locations -----------------------------------------------------

    public List<SpawnPoint> loadSpawnPoints() {
        List<SpawnPoint> points = new ArrayList<>();
        for (Map<?, ?> raw : config.getMapList("spawn-locations")) {
            try {
                points.add(new SpawnPoint(
                        String.valueOf(raw.get("world")),
                        toInt(raw.get("x")),
                        toInt(raw.get("y")),
                        toInt(raw.get("z"))));
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping malformed spawn-location entry in mobs.yml: " + raw);
            }
        }
        return points;
    }

    public void saveSpawnPoints(List<SpawnPoint> points) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (SpawnPoint point : points) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("world", point.world());
            map.put("x", point.x());
            map.put("y", point.y());
            map.put("z", point.z());
            serialized.add(map);
        }
        config.set("spawn-locations", serialized);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save spawn locations to mobs.yml", e);
        }
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }
}
