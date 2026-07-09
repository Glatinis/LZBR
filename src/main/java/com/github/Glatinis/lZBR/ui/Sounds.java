package com.github.Glatinis.lZBR.ui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.logging.Logger;

// Builds Adventure sounds from configured values. Returns null (meaning "play nothing") for a blank
// key, and logs — rather than throws — for a malformed key so a bad config never breaks the flow.
public final class Sounds {
    private Sounds() {}

    public static Sound of(String key, double volume, double pitch, Logger logger) {
        if (key == null || key.isBlank()) return null;
        try {
            return Sound.sound(Key.key(key), Sound.Source.MASTER, (float) volume, (float) pitch);
        } catch (Exception e) {
            logger.warning("Invalid sound key '" + key + "' — no sound will play.");
            return null;
        }
    }
}
