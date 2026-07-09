package com.github.Glatinis.lZBR.world.zone;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.ui.Announcement;
import com.github.Glatinis.lZBR.ui.Sounds;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

// Telegraphs an imminent zone shrink: shows a center-screen title (and subtitle) and plays a sound to
// every active player, a configurable number of seconds before the shrink actually begins.
public class ZoneTelegraph {
    private final ConfigRepository config;
    private final Logger logger;

    public ZoneTelegraph(ConfigRepository config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public boolean isEnabled() {
        return config.isZoneShrinkTelegraphEnabled();
    }

    public int getLeadSeconds() {
        return config.getZoneShrinkTelegraphLead();
    }

    // Shows the telegraph to the given players. secondsUntilShrink fills the {seconds} placeholder.
    public void announce(Collection<? extends Player> players, int secondsUntilShrink) {
        if (!isEnabled()) return;
        buildAnnouncement().showAll(players, Map.of("seconds", String.valueOf(secondsUntilShrink)));
    }

    private Announcement buildAnnouncement() {
        return new Announcement(
                config.getZoneShrinkTelegraphTitle(),
                config.getZoneShrinkTelegraphSubtitle(),
                Announcement.times(
                        config.getZoneShrinkTelegraphFadeInTicks(),
                        config.getZoneShrinkTelegraphStayTicks(),
                        config.getZoneShrinkTelegraphFadeOutTicks()),
                Sounds.of(config.getZoneShrinkTelegraphSoundKey(),
                        config.getZoneShrinkTelegraphSoundVolume(),
                        config.getZoneShrinkTelegraphSoundPitch(),
                        logger));
    }
}
