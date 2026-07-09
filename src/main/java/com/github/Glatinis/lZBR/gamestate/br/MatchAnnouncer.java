package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.ui.Announcement;
import com.github.Glatinis.lZBR.ui.Sounds;
import com.github.Glatinis.lZBR.ui.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

public class MatchAnnouncer {
    private final ConfigRepository config;
    private final Logger logger;

    public MatchAnnouncer(ConfigRepository config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void announceStart(Collection<? extends Player> players) {
        startBanner().showAll(players, Map.of());
    }

    public void announceWinner(Collection<? extends Player> audience, String winnerName) {
        winBanner().showAll(audience, Map.of("winner", winnerName));
        broadcast(config.getWinBroadcast(), Map.of("winner", winnerName));
    }

    public void announceNoWinner(Collection<? extends Player> audience) {
        broadcast(config.getWinNoWinnerBroadcast(), Map.of());
    }

    private void broadcast(String template, Map<String, String> placeholders) {
        if (template == null || template.isBlank()) return;
        Bukkit.broadcast(Text.mini(template, placeholders));
    }

    private Announcement startBanner() {
        return new Announcement(
                config.getStartTitle(),
                config.getStartSubtitle(),
                Announcement.times(config.getStartFadeInTicks(), config.getStartStayTicks(), config.getStartFadeOutTicks()),
                Sounds.of(config.getStartSoundKey(), config.getStartSoundVolume(), config.getStartSoundPitch(), logger));
    }

    private Announcement winBanner() {
        return new Announcement(
                config.getWinTitle(),
                config.getWinSubtitle(),
                Announcement.times(config.getWinFadeInTicks(), config.getWinStayTicks(), config.getWinFadeOutTicks()),
                Sounds.of(config.getWinSoundKey(), config.getWinSoundVolume(), config.getWinSoundPitch(), logger));
    }
}
