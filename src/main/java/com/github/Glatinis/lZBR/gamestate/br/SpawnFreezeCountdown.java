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

// Freezes players in place right after they land in the arena and counts down on-screen before
// releasing them, so nobody moves (or takes zone/mob action) until everyone's settled in.
public class SpawnFreezeCountdown {
    private final JavaPlugin plugin;
    private final ConfigRepository config;
    private final PlayerFreezeService freezeService;

    private BukkitTask task;
    private List<Player> activePlayers;

    public SpawnFreezeCountdown(JavaPlugin plugin, ConfigRepository config, PlayerFreezeService freezeService) {
        this.plugin = plugin;
        this.config = config;
        this.freezeService = freezeService;
    }

    public void start(List<Player> players, Runnable onComplete) {
        cancel();

        int seconds = Math.max(0, config.getSpawnFreezeSeconds());
        if (seconds == 0) {
            onComplete.run();
            return;
        }

        activePlayers = players;
        freezeService.freeze(players);

        Announcement announcement = new Announcement(
                config.getSpawnFreezeTitle(),
                config.getSpawnFreezeSubtitle(),
                Announcement.times(config.getSpawnFreezeFadeInTicks(), config.getSpawnFreezeStayTicks(), config.getSpawnFreezeFadeOutTicks()),
                Sounds.of(config.getSpawnFreezeSoundKey(), config.getSpawnFreezeSoundVolume(),
                        config.getSpawnFreezeSoundPitch(), plugin.getLogger()));

        task = new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    cancel();
                    finish(onComplete);
                    return;
                }
                announcement.showAll(players, Map.of("seconds", String.valueOf(remaining)));
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void finish(Runnable onComplete) {
        task = null;
        releaseEveryone();
        onComplete.run();
    }

    // Cancels the countdown if still running and releases anyone it had frozen, so a match that
    // ends early (e.g. an admin force-stop) never leaves players stuck in place.
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        releaseEveryone();
    }

    // Clears the whole frozen set rather than only the current roster: a player who disconnects mid
    // countdown is already off the roster, so unfreezing just the roster would leave their UUID frozen
    // and lock them in place when they reconnect.
    private void releaseEveryone() {
        activePlayers = null;
        freezeService.clear();
    }
}
