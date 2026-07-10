package com.github.Glatinis.lZBR.mob;

import com.github.Glatinis.lZBR.util.WeightedPool;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;
import java.util.logging.Logger;

// Builds the weighted pool of mob types from the mob-types section of mobs.yml. Non-living or unknown
// entity names are logged and skipped.
public final class MobTypes {
    private MobTypes() {}

    public static WeightedPool<EntityType> load(ConfigurationSection section, Logger logger) {
        WeightedPool.Builder<EntityType> pool = WeightedPool.builder();
        if (section == null) return pool.build();

        for (String key : section.getKeys(false)) {
            EntityType type = resolve(key);
            if (type == null) {
                logger.warning("Unknown or non-living mob type '" + key + "' in mobs.yml — ignored.");
                continue;
            }
            pool.add(type, section.getDouble(key + ".weight", 1.0));
        }
        return pool.build();
    }

    private static EntityType resolve(String key) {
        try {
            EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
            Class<?> entityClass = type.getEntityClass();
            if (!type.isSpawnable() || entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass)) {
                return null;
            }
            return type;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
