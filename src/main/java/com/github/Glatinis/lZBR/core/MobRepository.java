package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.mob.SpawnPoint;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Owns mobs.yml. Exposes the spawn/density settings, mob-type weights and drop rules, and reads/writes
// the spawn-location list that admins edit in-game.
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

    public boolean isEnabled() { return config.getBoolean("settings.enabled", true); }

    // --- Spawning ------------------------------------------------------------

    public int getIntervalSeconds() { return config.getInt("spawning.interval-seconds", 20); }
    public int getMinPerTick() { return config.getInt("spawning.per-tick.minimum", 0); }
    public int getMaxPerTick() { return config.getInt("spawning.per-tick.maximum", 2); }
    public int getMaxAlive() { return config.getInt("spawning.max-alive", 12); }
    public int getSpreadRadius() { return config.getInt("spawning.spread-radius", 6); }
    public int getMinPlayerDistance() { return config.getInt("spawning.min-player-distance", 12); }
    public int getStopBelowZoneRadius() { return config.getInt("spawning.stop-below-zone-radius", 60); }

    // --- Mob types -----------------------------------------------------------

    public ConfigurationSection getMobTypesSection() {
        return config.getConfigurationSection("mob-types");
    }

    // --- Drops ---------------------------------------------------------------

    public double getDropChance() { return config.getDouble("drops.chance", 25.0); }
    public int getMinDropRolls() { return config.getInt("drops.rolls.minimum", 1); }
    public int getMaxDropRolls() { return config.getInt("drops.rolls.maximum", 1); }
    public boolean isKeepVanillaDrops() { return config.getBoolean("drops.keep-vanilla-drops", true); }

    // --- Spawn locations -----------------------------------------------------

    public List<SpawnPoint> loadSpawnPoints() {
        List<SpawnPoint> points = new ArrayList<>();
        for (Map<?, ?> raw : config.getMapList("spawn-locations")) {
            try {
                points.add(SpawnPoint.fromMap(raw));
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping malformed spawn-location entry in mobs.yml: " + raw);
            }
        }
        return points;
    }

    public void saveSpawnPoints(List<SpawnPoint> points) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (SpawnPoint point : points) {
            serialized.add(point.toMap());
        }
        config.set("spawn-locations", serialized);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save spawn locations to mobs.yml", e);
        }
    }
}
