package com.github.Glatinis.lZBR.mob.spawn;

import com.github.Glatinis.lZBR.core.MobRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Owns the set of mob spawn points and keeps them persisted. Pure bookkeeping — the spawning itself is
// MobSpawner's job.
public class SpawnPointRegistry {
    private final MobRepository repository;
    private final List<SpawnPoint> points = new ArrayList<>();

    public SpawnPointRegistry(MobRepository repository) {
        this.repository = repository;
        reload();
    }

    public void reload() {
        points.clear();
        points.addAll(repository.loadSpawnPoints());
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public List<SpawnPoint> getPoints() {
        return List.copyOf(points);
    }

    public SpawnPoint random(Random random) {
        return points.isEmpty() ? null : points.get(random.nextInt(points.size()));
    }

    public void add(SpawnPoint point) {
        points.add(point);
        repository.saveSpawnPoints(points);
    }

    // Removes and returns the nearest point within maxDistance blocks of the location, or null.
    public SpawnPoint removeNearest(Location location, double maxDistance) {
        double bestDistance = maxDistance * maxDistance;
        SpawnPoint nearest = null;
        for (SpawnPoint point : points) {
            double distanceSquared = point.distanceSquared(location);
            if (distanceSquared >= 0 && distanceSquared <= bestDistance) {
                bestDistance = distanceSquared;
                nearest = point;
            }
        }
        if (nearest != null) {
            points.remove(nearest);
            repository.saveSpawnPoints(points);
        }
        return nearest;
    }

    // The distinct loaded worlds these points live in — used to sweep for stray mobs on cleanup.
    public Set<World> worlds() {
        Set<World> worlds = new HashSet<>();
        for (SpawnPoint point : points) {
            World world = Bukkit.getWorld(point.world());
            if (world != null) worlds.add(world);
        }
        return worlds;
    }
}
