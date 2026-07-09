package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.ui.Announcement;
import com.github.Glatinis.lZBR.ui.Sounds;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MatchCountdown {
    // Stay time is slightly over one second so consecutive numbers replace each other cleanly.
    private static final int FADE_IN_TICKS = 0;
    private static final int STAY_TICKS = 22;
    private static final int FADE_OUT_TICKS = 4;

    private final JavaPlugin plugin;
    private final ConfigRepository config;

    private BukkitTask task;

    public MatchCountdown(JavaPlugin plugin, ConfigRepository config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start(Supplier<List<Player>> audience, Runnable onComplete) {
        cancel();

        int seconds = Math.max(0, config.getCountdownSeconds());
        if (seconds == 0) {
            onComplete.run();
            return;
        }

        Announcement announcement = new Announcement(
                config.getCountdownTitle(),
                config.getCountdownSubtitle(),
                Announcement.times(FADE_IN_TICKS, STAY_TICKS, FADE_OUT_TICKS),
                Sounds.of(config.getCountdownSoundKey(), config.getCountdownSoundVolume(),
                        config.getCountdownSoundPitch(), plugin.getLogger()));

        task = new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    cancel();
                    task = null;
                    onComplete.run();
                    return;
                }
                announcement.showAll(audience.get(), Map.of("seconds", String.valueOf(remaining)));
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
