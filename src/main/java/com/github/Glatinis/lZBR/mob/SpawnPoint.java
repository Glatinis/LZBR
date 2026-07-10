package com.github.Glatinis.lZBR.mob;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

// A saved mob spawn point. Mobs appear within a configurable radius of it.
public record SpawnPoint(String world, int x, int y, int z) {

    public static SpawnPoint of(Location location) {
        return new SpawnPoint(location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    // Squared horizontal+vertical block distance from a location, or -1 if in another world.
    public double distanceSquared(Location location) {
        if (!location.getWorld().getName().equals(world)) return -1;
        double dx = location.getX() - x;
        double dy = location.getY() - y;
        double dz = location.getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        return map;
    }

    public static SpawnPoint fromMap(Map<?, ?> map) {
        return new SpawnPoint(
                String.valueOf(map.get("world")),
                toInt(map.get("x")),
                toInt(map.get("y")),
                toInt(map.get("z")));
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }
}
