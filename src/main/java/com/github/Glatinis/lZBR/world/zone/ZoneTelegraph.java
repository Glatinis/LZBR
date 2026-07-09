package com.github.Glatinis.lZBR.world.zone;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;
import java.util.logging.Logger;

// Telegraphs an imminent zone shrink: shows a center-screen title (and subtitle) and plays a sound to
// every active player, a configurable number of seconds before the shrink actually begins. Uses the
// native Adventure Title/Sound APIs bundled with Paper — no external library needed.
public class ZoneTelegraph {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

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

        Title title = buildTitle(secondsUntilShrink);
        Sound sound = buildSound();

        for (Player player : players) {
            player.showTitle(title);
            if (sound != null) {
                player.playSound(sound);
            }
        }
    }

    private Title buildTitle(int secondsUntilShrink) {
        String seconds = String.valueOf(secondsUntilShrink);
        Component titleText = render(config.getZoneShrinkTelegraphTitle(), seconds);
        Component subtitleText = render(config.getZoneShrinkTelegraphSubtitle(), seconds);

        Title.Times times = Title.Times.times(
                ticks(config.getZoneShrinkTelegraphFadeInTicks()),
                ticks(config.getZoneShrinkTelegraphStayTicks()),
                ticks(config.getZoneShrinkTelegraphFadeOutTicks()));

        return Title.title(titleText, subtitleText, times);
    }

    private static Component render(String template, String seconds) {
        if (template == null || template.isBlank()) return Component.empty();
        return MINI_MESSAGE.deserialize(template.replace("{seconds}", seconds));
    }

    private Sound buildSound() {
        String key = config.getZoneShrinkTelegraphSoundKey();
        if (key == null || key.isBlank()) return null;
        try {
            return Sound.sound(Key.key(key), Sound.Source.MASTER,
                    (float) config.getZoneShrinkTelegraphSoundVolume(),
                    (float) config.getZoneShrinkTelegraphSoundPitch());
        } catch (Exception e) {
            logger.warning("Invalid zone telegraph sound key '" + key + "'. No sound will play.");
            return null;
        }
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }
}
