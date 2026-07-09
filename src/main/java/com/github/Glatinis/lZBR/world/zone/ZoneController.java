package com.github.Glatinis.lZBR.world.zone;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Supplier;

// Drives the shrinking zone: reveals the border, schedules the shrink after a delay, and runs the
// damage task that hurts players the border has swept outside. The border is a hard wall players
// cannot cross, so they only end up outside when the shrink passes over them.
public class ZoneController {
    private static final long TICKS_PER_SECOND = 20L;

    private final JavaPlugin plugin;
    private final ConfigRepository config;
    private final ZoneBorder border;

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

    // Starts a small, fast-shrinking zone centered on a single player, for testing without a full match.
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

        border.create(centerX, centerZ, initialRadius);
        border.showAll(participants);

        long delayTicks = (long) shrinkDelaySeconds * TICKS_PER_SECOND;
        shrinkStartTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            shrinkStartTime = System.currentTimeMillis();
            shrinking = true;
            border.shrink(finalRadius, (long) shrinkDurationSeconds * TICKS_PER_SECOND);
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

        Bukkit.getOnlinePlayers().forEach(border::clear);
        border.dispose();
    }

    // Shows the current border to a player entering the arena mid-game. The shared border carries its
    // own shrink animation, so a mid-shrink arrival picks it up.
    public void sendBorderTo(Player player) {
        if (!active) return;
        border.show(player);
    }

    // Current radius, interpolated over the shrink for the damage check.
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
