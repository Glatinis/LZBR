package com.github.Glatinis.lZBR.mob;

import com.github.Glatinis.lZBR.core.MobRepository;
import com.github.Glatinis.lZBR.loot.LootManager;
import com.github.Glatinis.lZBR.mob.spawn.MobSpawnTask;
import com.github.Glatinis.lZBR.mob.spawn.MobSpawner;
import com.github.Glatinis.lZBR.mob.spawn.SpawnPoint;
import com.github.Glatinis.lZBR.mob.spawn.SpawnPointRegistry;
import com.github.Glatinis.lZBR.mob.spawn.SpawnSettings;
import com.github.Glatinis.lZBR.util.WeightedPool;
import com.github.Glatinis.lZBR.world.zone.ZoneController;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

// Orchestrates the mob feature: composes the spawn-point registry (state), the spawner (world ops),
// the periodic task (cadence), the active-mob tracker and the death listener, and drives their
// lifecycle from the match flow. Kept deliberately thin — the real work lives in the collaborators.
public class MobManager {
    private static final long TICKS_PER_SECOND = 20L;

    private final JavaPlugin plugin;
    private final MobRepository repository;
    private final SpawnPointRegistry registry;
    private final ActiveMobs activeMobs;
    private final MobSpawner spawner;
    private final MobDeathListener deathListener;

    private WeightedPool<EntityType> mobTypes;
    private MobSpawnTask spawnTask;

    public MobManager(JavaPlugin plugin, MobRepository repository, LootManager lootManager,
                      ZoneController zoneController) {
        this.plugin = plugin;
        this.repository = repository;
        this.registry = new SpawnPointRegistry(repository);
        this.activeMobs = new ActiveMobs(plugin);
        this.spawner = new MobSpawner(registry, activeMobs, zoneController);
        this.deathListener = new MobDeathListener(activeMobs, repository, lootManager);
        this.mobTypes = MobTypes.load(repository.getMobTypesSection(), plugin.getLogger());
    }

    public MobDeathListener getDeathListener() {
        return deathListener;
    }

    public void reload() {
        boolean wasRunning = spawnTask != null;
        repository.reload();
        registry.reload();
        mobTypes = MobTypes.load(repository.getMobTypesSection(), plugin.getLogger());
        if (wasRunning) startSpawning();
    }

    // --- Match hooks ---------------------------------------------------------

    public void startSpawning() {
        stopSpawning();
        despawnAll();
        if (!repository.isEnabled() || registry.isEmpty() || mobTypes.isEmpty()) return;

        SpawnSettings settings = repository.getSpawnSettings();
        long interval = Math.max(1, settings.intervalSeconds()) * TICKS_PER_SECOND;
        spawnTask = new MobSpawnTask(spawner, activeMobs, mobTypes, settings);
        spawnTask.runTaskTimer(plugin, interval, interval);
    }

    public void stopSpawning() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }

    public void despawnAll() {
        activeMobs.despawnAll(registry.worlds());
    }

    // --- Command-facing operations -------------------------------------------

    // Spawns up to count mobs immediately (for /lzbr test mob), skipping the density gates but keeping
    // the alive cap. Returns how many spawned.
    public int testSpawn(int count) {
        SpawnSettings settings = repository.getSpawnSettings();
        int spawned = 0;
        for (int i = 0; i < count && activeMobs.count() < settings.maxAlive(); i++) {
            if (spawner.spawn(mobTypes, settings, false)) spawned++;
        }
        return spawned;
    }

    public void addSpawnPoint(Player player) {
        registry.add(SpawnPoint.of(player.getLocation()));
    }

    public SpawnPoint removeNearestSpawnPoint(Player player, double maxDistance) {
        return registry.removeNearest(player.getLocation(), maxDistance);
    }

    public List<SpawnPoint> getSpawnPoints() {
        return registry.getPoints();
    }

    public int getMobTypeCount() {
        return mobTypes.size();
    }
}
