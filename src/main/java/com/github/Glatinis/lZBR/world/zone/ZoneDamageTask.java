package com.github.Glatinis.lZBR.world.zone;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class ZoneDamageTask extends BukkitRunnable {
    private final Supplier<List<Player>> activePlayers;
    private final DoubleSupplier currentRadius;
    private final World world;
    private final double centerX;
    private final double centerZ;
    private final double damageAmount;

    public ZoneDamageTask(Supplier<List<Player>> activePlayers, DoubleSupplier currentRadius, World world,
                          double centerX, double centerZ, double damageAmount) {
        this.activePlayers = activePlayers;
        this.currentRadius = currentRadius;
        this.world = world;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.damageAmount = damageAmount;
    }

    @Override
    public void run() {
        double radius = currentRadius.getAsDouble();
        double radiusSq = radius * radius;

        for (Player player : activePlayers.get()) {
            Location location = player.getLocation();

            // The zone's coordinates only mean anything inside its own world, so anyone who ended up
            // elsewhere (e.g. sent back to the lobby) is left alone rather than damaged by proxy.
            if (world != null && !world.equals(location.getWorld())) continue;

            double dx = location.getX() - centerX;
            double dz = location.getZ() - centerZ;
            if (dx * dx + dz * dz > radiusSq) {
                player.damage(damageAmount);
            }
        }
    }
}
