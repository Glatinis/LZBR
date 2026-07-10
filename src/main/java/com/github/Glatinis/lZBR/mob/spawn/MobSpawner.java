package com.github.Glatinis.lZBR.mob.spawn;

import com.github.Glatinis.lZBR.mob.ActiveMobs;
import com.github.Glatinis.lZBR.util.WeightedPool;
import com.github.Glatinis.lZBR.world.zone.ZoneController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Random;

// Spawns a single mob at a random configured point: chooses a type, computes a location, applies the
// low-density gates (zone cutoff, player distance, staying inside the zone) and tags the result so the
// rest of the system can track it. The periodic driving is MobSpawnTask's job.
public class MobSpawner {
    private final SpawnPointRegistry points;
    private final ActiveMobs activeMobs;
    private final ZoneController zone;
    private final Random random = new Random();

    public MobSpawner(SpawnPointRegistry points, ActiveMobs activeMobs, ZoneController zone) {
        this.points = points;
        this.activeMobs = activeMobs;
        this.zone = zone;
    }

    // Attempts one spawn. With enforceGates=false (used by the test command) the density gates are
    // skipped so an admin standing on a point still sees a spawn. Returns true if a mob was spawned.
    public boolean spawn(WeightedPool<EntityType> types, SpawnSettings settings, boolean enforceGates) {
        if (points.isEmpty() || types.isEmpty()) return false;
        if (enforceGates && isPastZoneCutoff(settings)) return false;

        SpawnPoint point = points.random(random);
        World world = Bukkit.getWorld(point.world());
        if (world == null) return false;

        Location location = randomLocationAround(world, point, settings.spreadRadius());
        if (enforceGates) {
            if (isPlayerNearby(location, settings.minPlayerDistance())) return false;
            if (!isInsideZone(location)) return false;
        }

        EntityType type = types.roll(random);
        if (type == null) return false;

        Entity entity = world.spawnEntity(location, type);
        activeMobs.track(entity);
        return true;
    }

    private Location randomLocationAround(World world, SpawnPoint point, int spreadRadius) {
        int spread = Math.max(0, spreadRadius);
        int offsetX = spread == 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
        int offsetZ = spread == 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
        // Centre of the block, at the point's height so mobs appear where the point was placed.
        return new Location(world, point.x() + offsetX + 0.5, point.y(), point.z() + offsetZ + 0.5);
    }

    private boolean isPastZoneCutoff(SpawnSettings settings) {
        return settings.stopBelowZoneRadius() > 0
                && zone.isActive()
                && zone.getCurrentRadius() <= settings.stopBelowZoneRadius();
    }

    private boolean isPlayerNearby(Location location, double distance) {
        if (distance <= 0) return false;
        double distanceSquared = distance * distance;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            if (player.getLocation().distanceSquared(location) <= distanceSquared) return true;
        }
        return false;
    }

    private boolean isInsideZone(Location location) {
        if (!zone.isActive()) return true;
        double dx = location.getX() - zone.getCenterX();
        double dz = location.getZ() - zone.getCenterZ();
        double radius = zone.getCurrentRadius();
        return dx * dx + dz * dz <= radius * radius;
    }
}
