package com.github.Glatinis.lZBR.loot.table;

import com.github.Glatinis.lZBR.util.WeightedPool;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

// The parsed loot table. Rolling picks a rarity (weighted by its chance, among rarities that actually
// have items) and then a uniform item within that rarity. Building it from config lives in
// LootTableLoader — this class only holds and samples.
public final class LootTable {
    private final WeightedPool<Rarity> rarityPool;
    private final Map<String, List<LootItem>> itemsByRarity;
    private final Random random = new Random();

    LootTable(WeightedPool<Rarity> rarityPool, Map<String, List<LootItem>> itemsByRarity) {
        this.rarityPool = rarityPool;
        this.itemsByRarity = itemsByRarity;
    }

    public boolean isEmpty() {
        return rarityPool.isEmpty();
    }

    public int itemCount() {
        return itemsByRarity.values().stream().mapToInt(List::size).sum();
    }

    // Draws one item, or null if the table is empty.
    public ItemStack roll() {
        Rarity rarity = rarityPool.roll(random);
        if (rarity == null) return null;

        List<LootItem> items = itemsByRarity.get(rarity.id());
        return items.get(random.nextInt(items.size())).toItemStack(random);
    }
}
