package com.github.Glatinis.lZBR.world.zone;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.Collection;

// Renders the zone as a shared virtual world border using Paper's native API (Bukkit#createWorldBorder
// + Player#setWorldBorder).
//
// The server owns this border, so it is a real, enforced boundary: a hard wall players cannot walk
// through — they only end up outside when the shrink sweeps past them, at which point ZoneDamageTask
// hurts them. The server also serializes the border packets itself, so the shrink animation stays
// correct across protocol versions (unlike hand-built packets, whose lerp time changed to ticks in
// 1.21.11 and silently broke).
//
// A single border instance is shared by every viewer. It tracks its own shrink animation, so calling
// shrink() animates the wall for all current viewers, and a late arrival assigned the same instance
// via show() picks up the in-progress shrink automatically.
public class ZoneBorder {
    private final ConfigRepository config;

    private WorldBorder border;

    public ZoneBorder(ConfigRepository config) {
        this.config = config;
    }

    // (Re)create the shared border, centered on the given point, at a static radius.
    public void create(double centerX, double centerZ, double radius) {
        border = Bukkit.createWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(radius * 2);
        border.setWarningDistance(config.getZoneWarningDistance());
        border.setWarningTimeTicks(config.getZoneWarningTime() * 20);
    }

    // Show the shared border to a player (initial reveal or late arrival mid-shrink).
    public void show(Player player) {
        if (border != null) {
            player.setWorldBorder(border);
        }
    }

    public void showAll(Collection<Player> players) {
        players.forEach(this::show);
    }

    // Smoothly animate the shared border down to toRadius over durationTicks; every viewer follows.
    public void shrink(double toRadius, long durationTicks) {
        if (border != null) {
            border.changeSize(toRadius * 2, durationTicks);
        }
    }

    // Restore the world's real border for a player.
    public void clear(Player player) {
        player.setWorldBorder(null);
    }

    // Drop the shared border once the zone is over.
    public void dispose() {
        border = null;
    }
}
