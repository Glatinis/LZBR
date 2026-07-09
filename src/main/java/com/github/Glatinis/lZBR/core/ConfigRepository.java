package com.github.Glatinis.lZBR.core;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigRepository {
    private final LZBR plugin;

    public ConfigRepository(LZBR plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    // --- Players -------------------------------------------------------------

    public int getMinimumPlayerCount() {
        return config().getInt("players.minimum");
    }

    public int getMaximumPlayerCount() {
        return config().getInt("players.maximum");
    }

    // --- Worlds --------------------------------------------------------------

    public String getLobbyWorldName() {
        return config().getString("worlds.lobby");
    }

    public String getBrWorldName() {
        return config().getString("worlds.br");
    }

    // --- Zone ----------------------------------------------------------------

    public double getZoneCenterX() { return config().getDouble("zone.center.x"); }
    public double getZoneCenterZ() { return config().getDouble("zone.center.z"); }
    public double getZoneInitialRadius() { return config().getDouble("zone.radius.initial"); }
    public double getZoneFinalRadius() { return config().getDouble("zone.radius.final"); }
    public int getZoneShrinkDelay() { return config().getInt("zone.shrink.delay-seconds"); }
    public int getZoneShrinkDuration() { return config().getInt("zone.shrink.duration-seconds"); }
    public double getZoneDamageAmount() { return config().getDouble("zone.damage.amount"); }
    public int getZoneDamageInterval() { return config().getInt("zone.damage.interval-ticks"); }
    public int getZoneWarningDistance() { return config().getInt("zone.warning.distance-blocks"); }
    public int getZoneWarningTime() { return config().getInt("zone.warning.time-seconds"); }

    // --- Zone shrink telegraph ----------------------------------------------

    public boolean isZoneShrinkTelegraphEnabled() { return config().getBoolean("zone.shrink.telegraph.enabled", true); }
    public int getZoneShrinkTelegraphLead() { return config().getInt("zone.shrink.telegraph.lead-seconds", 10); }
    public String getZoneShrinkTelegraphTitle() { return config().getString("zone.shrink.telegraph.title", ""); }
    public String getZoneShrinkTelegraphSubtitle() { return config().getString("zone.shrink.telegraph.subtitle", ""); }
    public int getZoneShrinkTelegraphFadeInTicks() { return config().getInt("zone.shrink.telegraph.fade-in-ticks", 10); }
    public int getZoneShrinkTelegraphStayTicks() { return config().getInt("zone.shrink.telegraph.stay-ticks", 40); }
    public int getZoneShrinkTelegraphFadeOutTicks() { return config().getInt("zone.shrink.telegraph.fade-out-ticks", 10); }
    public String getZoneShrinkTelegraphSoundKey() { return config().getString("zone.shrink.telegraph.sound.key", ""); }
    public double getZoneShrinkTelegraphSoundVolume() { return config().getDouble("zone.shrink.telegraph.sound.volume", 1.0); }
    public double getZoneShrinkTelegraphSoundPitch() { return config().getDouble("zone.shrink.telegraph.sound.pitch", 1.0); }

    // --- Match flow: pre-game countdown -------------------------------------

    public int getCountdownSeconds() { return config().getInt("game.countdown.seconds", 10); }
    public String getCountdownTitle() { return config().getString("game.countdown.title", ""); }
    public String getCountdownSubtitle() { return config().getString("game.countdown.subtitle", ""); }
    public String getCountdownSoundKey() { return config().getString("game.countdown.sound.key", ""); }
    public double getCountdownSoundVolume() { return config().getDouble("game.countdown.sound.volume", 1.0); }
    public double getCountdownSoundPitch() { return config().getDouble("game.countdown.sound.pitch", 1.0); }

    // --- Match flow: start banner -------------------------------------------

    public String getStartTitle() { return config().getString("game.start.title", ""); }
    public String getStartSubtitle() { return config().getString("game.start.subtitle", ""); }
    public int getStartFadeInTicks() { return config().getInt("game.start.fade-in-ticks", 5); }
    public int getStartStayTicks() { return config().getInt("game.start.stay-ticks", 30); }
    public int getStartFadeOutTicks() { return config().getInt("game.start.fade-out-ticks", 10); }
    public String getStartSoundKey() { return config().getString("game.start.sound.key", ""); }
    public double getStartSoundVolume() { return config().getDouble("game.start.sound.volume", 1.0); }
    public double getStartSoundPitch() { return config().getDouble("game.start.sound.pitch", 1.0); }

    // --- Match flow: win screen ---------------------------------------------

    public String getWinTitle() { return config().getString("game.win.title", ""); }
    public String getWinSubtitle() { return config().getString("game.win.subtitle", ""); }
    public String getWinBroadcast() { return config().getString("game.win.broadcast", ""); }
    public String getWinNoWinnerBroadcast() { return config().getString("game.win.no-winner-broadcast", ""); }
    public int getWinFadeInTicks() { return config().getInt("game.win.fade-in-ticks", 10); }
    public int getWinStayTicks() { return config().getInt("game.win.stay-ticks", 60); }
    public int getWinFadeOutTicks() { return config().getInt("game.win.fade-out-ticks", 20); }
    public String getWinSoundKey() { return config().getString("game.win.sound.key", ""); }
    public double getWinSoundVolume() { return config().getDouble("game.win.sound.volume", 1.0); }
    public double getWinSoundPitch() { return config().getDouble("game.win.sound.pitch", 1.0); }
    public int getWinReturnDelaySeconds() { return config().getInt("game.win.return-delay-seconds", 5); }

    // --- Arena reset ---------------------------------------------------------

    public boolean isArenaResetEnabled() { return config().getBoolean("arena.reset.enabled", true); }
    public String getArenaSchematic() { return config().getString("arena.reset.schematic", "arena.schem"); }
    public int getArenaOriginX() { return config().getInt("arena.reset.origin.x"); }
    public int getArenaOriginY() { return config().getInt("arena.reset.origin.y"); }
    public int getArenaOriginZ() { return config().getInt("arena.reset.origin.z"); }
    public boolean isArenaPasteAir() { return config().getBoolean("arena.reset.paste-air", true); }
    public boolean isArenaPasteEntities() { return config().getBoolean("arena.reset.paste-entities", false); }

    public void reload() {
        plugin.reloadConfig();
    }
}
