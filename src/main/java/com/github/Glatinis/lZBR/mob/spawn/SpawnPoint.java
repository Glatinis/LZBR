package com.github.Glatinis.lZBR.mob.spawn;

import org.bukkit.Location;

// A saved mob spawn point. Mobs appear within a configurable radius of it.
public record SpawnPoint(String world, int x, int y, int z) {

    public static SpawnPoint of(Location location) {
        return new SpawnPoint(location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    // Squared block distance from a location, or -1 if it is in another world.
    public double distanceSquared(Location location) {
        if (!location.getWorld().getName().equals(world)) return -1;
        double dx = location.getX() - x;
        double dy = location.getY() - y;
        double dz = location.getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
