package com.github.Glatinis.lZBR.world.spawn;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Picks a landing spot for each player at match start: scattered randomly within a radius of the
// zone center, kept apart from other players' spots where possible, never inside a block or over a
// hazard (lava, void), and kept off rooftops by staying close to the arena's typical ground height.
public class PlayerScatterService {
    private static final int GROUND_SAMPLE_COUNT = 24;

    private final ConfigRepository config;
    private final Random random = new Random();

    public PlayerScatterService(ConfigRepository config) {
        this.config = config;
    }

    public void scatter(World world, Collection<? extends Player> players) {
        double centerX = config.getZoneCenterX();
        double centerZ = config.getZoneCenterZ();
        double radius = Math.max(1, config.getSpawnRadius());
        double minDistance = config.getSpawnMinDistance();
        int maxAttempts = Math.max(1, config.getSpawnMaxAttempts());
        int maxHeightAboveGround = config.getSpawnMaxHeightAboveGround();

        // Sampled once per match (not per player, and not per attempt) so it reflects the arena
        // rather than getting skewed by whichever building a given attempt happened to land on.
        Integer groundLevel = maxHeightAboveGround > 0
                ? estimateGroundLevel(world, centerX, centerZ, radius)
                : null;

        List<Location> placed = new ArrayList<>(players.size());
        for (Player player : players) {
            Location location = findSpot(world, centerX, centerZ, radius, minDistance, maxAttempts,
                    groundLevel, maxHeightAboveGround, placed);
            placed.add(location);
            player.teleport(location);
        }
    }

    // Tries maxAttempts random points, preferring one that's far enough from every already-placed
    // player. If none qualifies, falls back to the last safe point found rather than leaving the
    // player stuck in the lobby.
    private Location findSpot(World world, double centerX, double centerZ, double radius, double minDistance,
                               int maxAttempts, Integer groundLevel, int maxHeightAboveGround, List<Location> placed) {
        Location fallback = null;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Location candidate = randomSafeLocation(world, centerX, centerZ, radius, groundLevel, maxHeightAboveGround);
            if (candidate == null) continue;
            if (fallback == null) fallback = candidate;
            if (isFarEnough(candidate, placed, minDistance)) return candidate;
        }
        return fallback != null ? fallback : world.getSpawnLocation();
    }

    private boolean isFarEnough(Location candidate, List<Location> placed, double minDistance) {
        if (minDistance <= 0) return true;
        double minDistanceSquared = minDistance * minDistance;
        for (Location other : placed) {
            if (candidate.distanceSquared(other) < minDistanceSquared) return false;
        }
        return true;
    }

    // Samples random points across the scatter disc and takes the median ground height. Rooftops
    // are a minority of the arena's footprint and skew high, so the median stays a good proxy for
    // "actual terrain" even when a few samples land on buildings.
    private Integer estimateGroundLevel(World world, double centerX, double centerZ, double radius) {
        List<Integer> samples = new ArrayList<>(GROUND_SAMPLE_COUNT);
        for (int i = 0; i < GROUND_SAMPLE_COUNT; i++) {
            int[] point = randomPoint(centerX, centerZ, radius);
            int feetY = safeFeetY(world, point[0], point[1]);
            if (feetY != Integer.MIN_VALUE) samples.add(feetY);
        }
        if (samples.isEmpty()) return null;
        Collections.sort(samples);
        return samples.get(samples.size() / 2);
    }

    // Uniform sampling over the disc (not the bounding square) so points don't bunch up near center.
    private int[] randomPoint(double centerX, double centerZ, double radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = radius * Math.sqrt(random.nextDouble());
        int x = (int) Math.floor(centerX + distance * Math.cos(angle));
        int z = (int) Math.floor(centerZ + distance * Math.sin(angle));
        return new int[] { x, z };
    }

    private Location randomSafeLocation(World world, double centerX, double centerZ, double radius,
                                         Integer groundLevel, int maxHeightAboveGround) {
        int[] point = randomPoint(centerX, centerZ, radius);
        int feetY = safeFeetY(world, point[0], point[1]);
        if (feetY == Integer.MIN_VALUE) return null;
        if (groundLevel != null && maxHeightAboveGround > 0 && feetY - groundLevel > maxHeightAboveGround) {
            return null; // too far above the arena's typical ground — almost certainly a rooftop
        }
        return new Location(world, point[0] + 0.5, feetY, point[1] + 0.5);
    }

    // Scans down from the world's heightmap for a solid, non-hazardous block with two clear blocks
    // above it, so the player lands standing up instead of suffocating or dropping into a hazard.
    private int safeFeetY(World world, int x, int z) {
        int top = Math.min(world.getMaxHeight() - 2, world.getHighestBlockYAt(x, z) + 1);
        int minY = world.getMinHeight();
        for (int y = top; y > minY; y--) {
            Block ground = world.getBlockAt(x, y, z);
            if (!isSafeGround(ground)) continue;

            Block feet = world.getBlockAt(x, y + 1, z);
            Block head = world.getBlockAt(x, y + 2, z);
            if (isClear(feet) && isClear(head)) return y + 1;
        }
        return Integer.MIN_VALUE;
    }

    private boolean isSafeGround(Block block) {
        Material type = block.getType();
        if (!block.getType().isSolid() || block.isLiquid()) return false;
        return type != Material.MAGMA_BLOCK && type != Material.FIRE && type != Material.CACTUS;
    }

    private boolean isClear(Block block) {
        return block.isPassable() && !block.isLiquid() && block.getType() != Material.FIRE;
    }
}
