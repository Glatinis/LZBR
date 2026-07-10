package com.github.Glatinis.lZBR.mob;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Tracks the mobs LZBR has spawned. Each is stamped with a persistent tag so we can recognise our own
// mobs on death (for drops) and clean them up, even across chunk reloads. Owns both the live UUID set
// and the tag key.
public class ActiveMobs {
    private final NamespacedKey tag;
    private final Set<UUID> tracked = new HashSet<>();

    public ActiveMobs(Plugin plugin) {
        this.tag = new NamespacedKey(plugin, "lzbr_mob");
    }

    public void track(Entity entity) {
        entity.getPersistentDataContainer().set(tag, PersistentDataType.BYTE, (byte) 1);
        tracked.add(entity.getUniqueId());
    }

    public boolean isLzbrMob(Entity entity) {
        return entity.getPersistentDataContainer().has(tag, PersistentDataType.BYTE);
    }

    public void untrack(UUID uuid) {
        tracked.remove(uuid);
    }

    public int count() {
        prune();
        return tracked.size();
    }

    // Drops UUIDs whose entity is gone or dead so the alive count stays accurate.
    public void prune() {
        tracked.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            return entity == null || entity.isDead();
        });
    }

    // Removes every tracked mob, plus any tagged strays in the given worlds (e.g. left over after a
    // server crash mid-match), then clears the set.
    public void despawnAll(Collection<World> worlds) {
        for (UUID uuid : tracked) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        }
        tracked.clear();

        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                if (isLzbrMob(entity)) entity.remove();
            }
        }
    }
}
