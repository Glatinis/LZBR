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

    // Send a static border at the given radius to a single player.
    public void sendStatic(Player player, double radius) {
        double diameter = radius * 2;
        send(player, diameter, diameter, 0L);
    }

    // Tell all players to begin smoothly shrinking from fromRadius to toRadius over durationMs.
    public void broadcastLerp(Collection<Player> players, double fromRadius, double toRadius, long durationMs) {
        WrapperPlayServerInitializeWorldBorder packet = buildPacket(fromRadius * 2, toRadius * 2, durationMs);
        players.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet));
    }

    // Send the current mid-shrink state to a single player (used for late arrivals).
    public void sendMidShrink(Player player, double currentRadius, double targetRadius, long remainingMs) {
        send(player, currentRadius * 2, targetRadius * 2, remainingMs);
    }

    // Reset the border to an invisible state for a player (max size, instant).
    public void clear(Player player) {
        send(player, MAX_DIAMETER, MAX_DIAMETER, 0L);
    }

    private void send(Player player, double oldDiameter, double newDiameter, long speed) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, buildPacket(oldDiameter, newDiameter, speed));
    }

    private WrapperPlayServerInitializeWorldBorder buildPacket(double oldDiameter, double newDiameter, long speed) {
        return new WrapperPlayServerInitializeWorldBorder(
                config.getZoneCenterX(), config.getZoneCenterZ(),
                oldDiameter, newDiameter, speed,
                PORTAL_BOUNDARY,
                config.getZoneWarningDistance(), config.getZoneWarningTime()
        );
    }
}
