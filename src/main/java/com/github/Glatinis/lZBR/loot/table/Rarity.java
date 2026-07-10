package com.github.Glatinis.lZBR.loot.table;

// A loot rarity tier. displayName is a MiniMessage template shown in item lore; chance is the relative
// weight used when rolling which rarity a drop belongs to.
public record Rarity(String id, String displayName, double chance) {
}
