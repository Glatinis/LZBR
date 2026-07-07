package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerInitializeWorldBorder;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ZoneBorder {
    private static final int PORTAL_BOUNDARY = 29999984;
    private static final double MAX_DIAMETER = 60000000.0;

    private final ConfigRepository config;

    public ZoneBorder(ConfigRepository config) {
        this.config = config;
    }

    // Send a static border at the given radius to a single player, centered on the configured zone center.
    public void sendStatic(Player player, double radius) {
        sendStatic(player, config.getZoneCenterX(), config.getZoneCenterZ(), radius);
    }

    // Send a static border at the given radius to a single player, centered on an arbitrary point.
    public void sendStatic(Player player, double centerX, double centerZ, double radius) {
        double diameter = radius * 2;
        send(player, centerX, centerZ, diameter, diameter, 0L);
    }

    // Tell all players to begin smoothly shrinking from fromRadius to toRadius over durationMs.
    // We re-send a full Initialize World Border packet with a non-zero speed rather than the
    // dedicated lerp-size packet: on the client both packets funnel into the same
    // WorldBorder#lerpSizeBetween(old, new, time) call, but the standalone lerp-size packet does
    // not reliably animate the rendered wall in this setup (the client applies the shrink colour
    // but leaves the wall in place), whereas Initialize — the same packet that renders the initial
    // border — animates it correctly.
    public void broadcastLerp(Collection<Player> players, double centerX, double centerZ,
                              double fromRadius, double toRadius, long durationMs) {
        WrapperPlayServerInitializeWorldBorder packet =
                buildPacket(centerX, centerZ, fromRadius * 2, toRadius * 2, durationMs);
        players.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet));
    }

    // Send the current mid-shrink state to a single player (used for late arrivals), centered on an arbitrary point.
    public void sendMidShrink(Player player, double centerX, double centerZ, double currentRadius, double targetRadius, long remainingMs) {
        send(player, centerX, centerZ, currentRadius * 2, targetRadius * 2, remainingMs);
    }

    // Reset the border to an invisible state for a player (max size, instant).
    public void clear(Player player) {
        send(player, config.getZoneCenterX(), config.getZoneCenterZ(), MAX_DIAMETER, MAX_DIAMETER, 0L);
    }

    private void send(Player player, double centerX, double centerZ, double oldDiameter, double newDiameter, long speed) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, buildPacket(centerX, centerZ, oldDiameter, newDiameter, speed));
    }

    private WrapperPlayServerInitializeWorldBorder buildPacket(double centerX, double centerZ, double oldDiameter, double newDiameter, long speed) {
        return new WrapperPlayServerInitializeWorldBorder(
                centerX, centerZ,
                oldDiameter, newDiameter, speed,
                PORTAL_BOUNDARY,
                config.getZoneWarningDistance(), config.getZoneWarningTime()
        );
    }
}
