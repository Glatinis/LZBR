package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Supplier;

public class ZoneController {
    private final JavaPlugin plugin;
    private final ConfigRepository config;
    private final ZoneBorder border; // null when PacketEvents is absent

    private Supplier<List<Player>> activePlayers;
    private BukkitTask shrinkStartTask;
    private BukkitTask damageTask;

    private double centerX;
    private double centerZ;
    private double initialRadius;
    private double finalRadius;
    private long shrinkDurationMs;

    private long shrinkStartTime = -1;
    private boolean shrinking = false;
    private boolean active = false;

    public ZoneController(JavaPlugin plugin, ConfigRepository config, ZoneBorder border) {
        this.plugin = plugin;
        this.config = config;
        this.border = border;
    }

    public void start(List<Player> participants, Supplier<List<Player>> activePlayers) {
        start(participants, activePlayers,
                config.getZoneCenterX(), config.getZoneCenterZ(),
                config.getZoneInitialRadius(), config.getZoneFinalRadius(),
                config.getZoneShrinkDelay(), config.getZoneShrinkDuration());
    }

    // Starts a small, fast-shrinking zone centered on a single player, for testing the zone feature without a full match.
    public void startTest(Player player, double initialRadius, double finalRadius, int shrinkDelaySeconds, int shrinkDurationSeconds) {
        Location loc = player.getLocation();
        start(List.of(player), () -> List.of(player),
                loc.getX(), loc.getZ(),
                initialRadius, finalRadius,
                shrinkDelaySeconds, shrinkDurationSeconds);
    }

    private void start(List<Player> participants, Supplier<List<Player>> activePlayers,
                        double centerX, double centerZ,
                        double initialRadius, double finalRadius,
                        int shrinkDelaySeconds, int shrinkDurationSeconds) {
        this.activePlayers = activePlayers;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.initialRadius = initialRadius;
        this.finalRadius = finalRadius;
        this.shrinkDurationMs = (long) shrinkDurationSeconds * 1000L;
        active = true;
        shrinking = false;
        shrinkStartTime = -1;

        if (border != null) {
            participants.forEach(p -> border.sendStatic(p, centerX, centerZ, initialRadius));
        }

        long delayTicks = (long) shrinkDelaySeconds * 20L;
        shrinkStartTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            shrinkStartTime = System.currentTimeMillis();
            shrinking = true;

            if (border != null) {
                border.broadcastLerp(activePlayers.get(), centerX, centerZ, initialRadius, finalRadius, shrinkDurationMs);
            }
        }, delayTicks);

        long intervalTicks = config.getZoneDamageInterval();
        damageTask = new ZoneDamageTask(
                activePlayers,
                this::getCurrentRadius,
                centerX, centerZ,
                config.getZoneDamageAmount()
        ).runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (!active) return;
        active = false;
        shrinking = false;
        shrinkStartTime = -1;

        if (shrinkStartTask != null) { shrinkStartTask.cancel(); shrinkStartTask = null; }
        if (damageTask != null) { damageTask.cancel(); damageTask = null; }

        if (border != null) {
            Bukkit.getOnlinePlayers().forEach(border::clear);
        }
    }

    // Sends the correct current border state to a player — used when a player enters the arena mid-game.
    public void sendBorderTo(Player player) {
        if (border == null || !active) return;
        if (shrinking) {
            long elapsed = System.currentTimeMillis() - shrinkStartTime;
            long remaining = Math.max(0L, shrinkDurationMs - elapsed);
            border.sendMidShrink(player, centerX, centerZ, getCurrentRadius(), finalRadius, remaining);
        } else {
            border.sendStatic(player, centerX, centerZ, initialRadius);
        }
    }

    public double getCurrentRadius() {
        if (!shrinking || shrinkStartTime < 0) return initialRadius;
        long elapsed = System.currentTimeMillis() - shrinkStartTime;
        double progress = Math.min(1.0, (double) elapsed / shrinkDurationMs);
        return initialRadius - (initialRadius - finalRadius) * progress;
    }

    public boolean isActive() {
        return active;
    }
}
