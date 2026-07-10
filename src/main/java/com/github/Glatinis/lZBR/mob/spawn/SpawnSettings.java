package com.github.Glatinis.lZBR.mob.spawn;

// The spawn cadence and density limits from mobs.yml. These are the levers that keep mobs sparse so
// PvP stays the main event.
public record SpawnSettings(
        int intervalSeconds,
        int minPerTick,
        int maxPerTick,
        int maxAlive,
        int spreadRadius,
        int minPlayerDistance,
        int stopBelowZoneRadius) {
}
