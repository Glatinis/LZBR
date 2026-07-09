package com.github.Glatinis.lZBR.ui;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

// Built once from config; shown to any set of players with per-show placeholder values.
public final class Announcement {
    private final String titleTemplate;
    private final String subtitleTemplate;
    private final Title.Times times;
    private final Sound sound;

    public Announcement(String titleTemplate, String subtitleTemplate, Title.Times times, Sound sound) {
        this.titleTemplate = titleTemplate;
        this.subtitleTemplate = subtitleTemplate;
        this.times = times;
        this.sound = sound;
    }

    public void show(Player player, Map<String, String> placeholders) {
        player.showTitle(buildTitle(placeholders));
        if (sound != null) {
            player.playSound(sound);
        }
    }

    public void showAll(Collection<? extends Player> players, Map<String, String> placeholders) {
        Title title = buildTitle(placeholders);
        for (Player player : players) {
            player.showTitle(title);
            if (sound != null) {
                player.playSound(sound);
            }
        }
    }

    private Title buildTitle(Map<String, String> placeholders) {
        return Title.title(
                Text.mini(titleTemplate, placeholders),
                Text.mini(subtitleTemplate, placeholders),
                times);
    }

    public static Title.Times times(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        return Title.Times.times(ticks(fadeInTicks), ticks(stayTicks), ticks(fadeOutTicks));
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }
}
