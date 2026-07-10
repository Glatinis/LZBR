package com.github.Glatinis.lZBR.mob;

// Loot-drop rules for LZBR mobs. chance is 0–100; rolls is how many items to pull from the loot table
// when a drop happens; keepVanilla keeps the mob's normal vanilla loot as well.
public record DropSettings(double chance, int minRolls, int maxRolls, boolean keepVanilla) {
}
