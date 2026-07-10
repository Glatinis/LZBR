package com.github.Glatinis.lZBR.mob.spawn;

import com.github.Glatinis.lZBR.mob.ActiveMobs;
import com.github.Glatinis.lZBR.util.WeightedPool;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

// The periodic driver: each run tries to spawn a small random number of mobs, always respecting the
// alive cap. All the per-mob decisions belong to MobSpawner; this class only handles cadence and the
// count/cap, mirroring ZoneDamageTask.
public class MobSpawnTask extends BukkitRunnable {
    private final MobSpawner spawner;
    private final ActiveMobs activeMobs;
    private final WeightedPool<EntityType> types;
    private final SpawnSettings settings;
    private final Random random = new Random();

    public MobSpawnTask(MobSpawner spawner, ActiveMobs activeMobs,
                        WeightedPool<EntityType> types, SpawnSettings settings) {
        this.spawner = spawner;
        this.activeMobs = activeMobs;
        this.types = types;
        this.settings = settings;
    }

    @Override
    public void run() {
        if (activeMobs.count() >= settings.maxAlive()) return;

        int min = Math.max(0, settings.minPerTick());
        int max = Math.max(min, settings.maxPerTick());
        int attempts = (min == max) ? min : min + random.nextInt(max - min + 1);

        for (int i = 0; i < attempts && activeMobs.count() < settings.maxAlive(); i++) {
            spawner.spawn(types, settings, true);
        }
    }
}
