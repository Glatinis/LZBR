package com.github.Glatinis.lZBR.world.zone;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.Collection;

// Uses Paper's native virtual world border (Bukkit#createWorldBorder + Player#setWorldBorder), a
// real server-owned boundary players cannot walk through — hand-built border packets were tried first,
// but their lerp-time field changed units (ms -> ticks) in 1.21.11 and silently broke the shrink
// animation. A single border instance is shared by every viewer and tracks its own shrink animation,
// so a late arrival assigned the same instance via show() picks up the in-progress shrink automatically.
public class ZoneBorder {
    private final ConfigRepository config;

    private WorldBorder border;

    public ZoneBorder(ConfigRepository config) {
        this.config = config;
    }

    public void create(double centerX, double centerZ, double radius) {
        border = Bukkit.createWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(radius * 2);
        border.setWarningDistance(config.getZoneWarningDistance());
        border.setWarningTimeTicks(config.getZoneWarningTime() * 20);
    }

    public void show(Player player) {
        if (border != null) {
            player.setWorldBorder(border);
        }
    }

    public void showAll(Collection<Player> players) {
        players.forEach(this::show);
    }

    public void shrink(double toRadius, long durationTicks) {
        if (border != null) {
            border.changeSize(toRadius * 2, durationTicks);
        }
    }

    public void clear(Player player) {
        player.setWorldBorder(null);
    }

    public void dispose() {
        border = null;
    }
}
