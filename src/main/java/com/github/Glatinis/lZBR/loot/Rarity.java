package com.github.Glatinis.lZBR.loot;

// A loot rarity tier. displayName is a MiniMessage template shown in item lore; chance is a relative
// weight used when rolling which rarity a chest slot receives.
public record Rarity(String id, String displayName, double chance) {
}
