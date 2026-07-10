package com.github.Glatinis.lZBR.loot.chest;

// How a chest is stocked each match: a random stack count between min and max (inclusive), and whether
// items are scattered across random slots or packed into the first ones.
public record ChestFillSettings(int minItems, int maxItems, boolean scatter) {
}
