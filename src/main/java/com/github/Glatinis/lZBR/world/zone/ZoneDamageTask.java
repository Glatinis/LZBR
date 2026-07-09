package com.github.Glatinis.lZBR.world.zone;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

// Periodically damages every active player standing outside the current zone radius.
public class ZoneDamageTask extends BukkitRunnable {
    private final Supplier<List<Player>> activePlayers;
    private final DoubleSupplier currentRadius;
    private final double centerX;
    private final double centerZ;
    private final double damageAmount;

    public ZoneDamageTask(Supplier<List<Player>> activePlayers, DoubleSupplier currentRadius,
                          double centerX, double centerZ, double damageAmount) {
        this.activePlayers = activePlayers;
        this.currentRadius = currentRadius;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.damageAmount = damageAmount;
    }

    @Override
    public void run() {
        double radius = currentRadius.getAsDouble();
        double radiusSq = radius * radius;

        for (Player player : activePlayers.get()) {
            double dx = player.getLocation().getX() - centerX;
            double dz = player.getLocation().getZ() - centerZ;
            if (dx * dx + dz * dz > radiusSq) {
                player.damage(damageAmount);
            }
        }
    }
}
