package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Bukkit;
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

    private long shrinkStartTime = -1;
    private boolean shrinking = false;
    private boolean active = false;

    public ZoneController(JavaPlugin plugin, ConfigRepository config, ZoneBorder border) {
        this.plugin = plugin;
        this.config = config;
        this.border = border;
    }

    public void start(List<Player> participants, Supplier<List<Player>> activePlayers) {
        this.activePlayers = activePlayers;
        active = true;
        shrinking = false;
        shrinkStartTime = -1;

        if (border != null) {
            double initialRadius = config.getZoneInitialRadius();
            participants.forEach(p -> border.sendStatic(p, initialRadius));
        }

        long delayTicks = (long) config.getZoneShrinkDelay() * 20L;
        shrinkStartTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            shrinkStartTime = System.currentTimeMillis();
            shrinking = true;

            if (border != null) {
                long durationMs = (long) config.getZoneShrinkDuration() * 1000L;
                border.broadcastLerp(
                        activePlayers.get(),
                        config.getZoneInitialRadius(),
                        config.getZoneFinalRadius(),
                        durationMs
                );
            }
        }, delayTicks);

        long intervalTicks = config.getZoneDamageInterval();
        damageTask = new ZoneDamageTask(
                activePlayers,
                this::getCurrentRadius,
                config.getZoneCenterX(),
                config.getZoneCenterZ(),
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
            long durationMs = (long) config.getZoneShrinkDuration() * 1000L;
            long remaining = Math.max(0L, durationMs - elapsed);
            border.sendMidShrink(player, getCurrentRadius(), config.getZoneFinalRadius(), remaining);
        } else {
            border.sendStatic(player, config.getZoneInitialRadius());
        }
    }

    public double getCurrentRadius() {
        if (!shrinking || shrinkStartTime < 0) return config.getZoneInitialRadius();
        long elapsed = System.currentTimeMillis() - shrinkStartTime;
        long durationMs = (long) config.getZoneShrinkDuration() * 1000L;
        double progress = Math.min(1.0, (double) elapsed / durationMs);
        double initial = config.getZoneInitialRadius();
        double finalR = config.getZoneFinalRadius();
        return initial - (initial - finalR) * progress;
    }

    public boolean isActive() {
        return active;
    }
}
