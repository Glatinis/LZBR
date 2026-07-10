package com.github.Glatinis.lZBR.loot;

import com.github.Glatinis.lZBR.core.LootRepository;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

// The parsed loot table: the rarity tiers and the items grouped under each. roll() draws a random
// item by first picking a rarity (weighted by its chance) and then a uniform item within it.
public final class LootTable {
    private final List<Rarity> rarities;
    private final Map<String, List<LootItem>> itemsByRarity;
    private final Random random = new Random();

    private LootTable(List<Rarity> rarities, Map<String, List<LootItem>> itemsByRarity) {
        this.rarities = rarities;
        this.itemsByRarity = itemsByRarity;
    }

    public boolean isEmpty() {
        return itemsByRarity.values().stream().allMatch(List::isEmpty);
    }

    public int itemCount() {
        return itemsByRarity.values().stream().mapToInt(List::size).sum();
    }

    // Draws one item, or null if the table is empty.
    public ItemStack roll() {
        List<Rarity> eligible = rarities.stream()
                .filter(rarity -> !itemsByRarity.getOrDefault(rarity.id(), List.of()).isEmpty())
                .toList();
        if (eligible.isEmpty()) return null;

        Rarity chosen = pickRarity(eligible);
        List<LootItem> items = itemsByRarity.get(chosen.id());
        return items.get(random.nextInt(items.size())).create(random);
    }

    private Rarity pickRarity(List<Rarity> eligible) {
        double total = eligible.stream().mapToDouble(rarity -> Math.max(0, rarity.chance())).sum();
        if (total <= 0) {
            // No usable weights — treat every eligible rarity as equally likely.
            return eligible.get(random.nextInt(eligible.size()));
        }

        double target = random.nextDouble() * total;
        double cumulative = 0;
        for (Rarity rarity : eligible) {
            cumulative += Math.max(0, rarity.chance());
            if (target < cumulative) return rarity;
        }
        return eligible.get(eligible.size() - 1);
    }

    // --- Loading -------------------------------------------------------------

    public static LootTable load(LootRepository repository, Logger logger) {
        Map<String, Rarity> rarities = loadRarities(repository.getRaritiesSection());

        Map<String, List<LootItem>> itemsByRarity = new LinkedHashMap<>();
        rarities.keySet().forEach(id -> itemsByRarity.put(id, new ArrayList<>()));

        for (Map<?, ?> raw : repository.getLootEntries()) {
            LootItem item = parseItem(raw, rarities, logger);
            if (item != null) {
                itemsByRarity.get(item.rarityId()).add(item);
            }
        }

        return new LootTable(new ArrayList<>(rarities.values()), itemsByRarity);
    }

    private static Map<String, Rarity> loadRarities(ConfigurationSection section) {
        Map<String, Rarity> rarities = new LinkedHashMap<>();
        if (section == null) return rarities;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;
            String id = key.toLowerCase(Locale.ROOT);
            String displayName = entry.getString("display-name", key);
            double chance = entry.getDouble("chance", 0.0);
            rarities.put(id, new Rarity(id, displayName, chance));
        }
        return rarities;
    }

    private static LootItem parseItem(Map<?, ?> raw, Map<String, Rarity> rarities, Logger logger) {
        Object materialRaw = raw.get("material");
        if (materialRaw == null) {
            logger.warning("Skipping loot entry with no material: " + raw);
            return null;
        }
        Material material = Material.matchMaterial(String.valueOf(materialRaw));
        if (material == null || !material.isItem()) {
            logger.warning("Skipping loot entry with unknown material '" + materialRaw + "'.");
            return null;
        }

        Object rarityRaw = raw.get("rarity");
        String rarityId = (rarityRaw == null ? "" : String.valueOf(rarityRaw)).toLowerCase(Locale.ROOT);
        Rarity rarity = rarities.get(rarityId);
        if (rarity == null) {
            logger.warning("Skipping loot entry '" + material + "' — unknown rarity '" + rarityId + "'.");
            return null;
        }

        int minAmount = 1;
        int maxAmount = 1;
        if (raw.get("amount") instanceof Map<?, ?> amount) {
            minAmount = toInt(amount.get("minimum"), 1);
            maxAmount = toInt(amount.get("maximum"), minAmount);
        }

        String name = raw.get("name") == null ? null : String.valueOf(raw.get("name"));

        List<String> lore = new ArrayList<>();
        if (raw.get("lore") instanceof List<?> loreList) {
            for (Object line : loreList) lore.add(String.valueOf(line));
        }

        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
        if (raw.get("enchantments") instanceof Map<?, ?> enchantMap) {
            enchantMap.forEach((enchantKey, level) -> {
                Enchantment enchantment = resolveEnchantment(String.valueOf(enchantKey));
                if (enchantment == null) {
                    logger.warning("Unknown enchantment '" + enchantKey + "' on loot entry '" + material + "' — ignored.");
                } else {
                    enchantments.put(enchantment, toInt(level, 1));
                }
            });
        }

        boolean showRarity = !Boolean.FALSE.equals(raw.get("show-rarity"));

        return new LootItem(material, minAmount, maxAmount, rarity, name, lore, enchantments, showRarity);
    }

    private static Enchantment resolveEnchantment(String key) {
        return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
    }

    private static int toInt(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
