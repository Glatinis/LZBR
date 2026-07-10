package com.github.Glatinis.lZBR.mob;

import org.bukkit.entity.EntityType;

// A spawnable mob and its relative spawn weight.
public record MobType(EntityType type, double weight) {
}
