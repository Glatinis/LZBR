package com.github.Glatinis.lZBR.loot.table;

import com.github.Glatinis.lZBR.core.LootRepository;
import com.github.Glatinis.lZBR.util.WeightedPool;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

// Builds a LootTable from loot.yml. All the fragile config reading (materials, enchantments, rarities)
// lives here so LootTable itself stays a clean sampler; bad entries are logged and skipped.
public final class LootTableLoader {
    private final Logger logger;

    public LootTableLoader(Logger logger) {
        this.logger = logger;
    }

    public LootTable load(LootRepository repository) {
        Map<String, Rarity> rarities = loadRarities(repository.getRaritiesSection());

        Map<String, List<LootItem>> itemsByRarity = new LinkedHashMap<>();
        for (Map<?, ?> raw : repository.getLootEntries()) {
            LootItem item = parseItem(raw, rarities);
            if (item != null) {
                itemsByRarity.computeIfAbsent(item.rarityId(), key -> new ArrayList<>()).add(item);
            }
        }

        // Only rarities that actually have items are eligible to roll.
        WeightedPool.Builder<Rarity> pool = WeightedPool.builder();
        for (Rarity rarity : rarities.values()) {
            if (itemsByRarity.containsKey(rarity.id())) {
                pool.add(rarity, rarity.chance());
            }
        }

        return new LootTable(pool.build(), itemsByRarity);
    }

    private Map<String, Rarity> loadRarities(ConfigurationSection section) {
        Map<String, Rarity> rarities = new LinkedHashMap<>();
        if (section == null) return rarities;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;
            String id = key.toLowerCase(Locale.ROOT);
            rarities.put(id, new Rarity(id, entry.getString("display-name", key), entry.getDouble("chance", 0.0)));
        }
        return rarities;
    }

    private LootItem parseItem(Map<?, ?> raw, Map<String, Rarity> rarities) {
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

        Map<Enchantment, Integer> enchantments = parseEnchantments(raw.get("enchantments"), material);
        boolean showRarity = !Boolean.FALSE.equals(raw.get("show-rarity"));

        return new LootItem(material, minAmount, maxAmount, rarity, name, lore, enchantments, showRarity);
    }

    private Map<Enchantment, Integer> parseEnchantments(Object raw, Material material) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
        if (!(raw instanceof Map<?, ?> enchantMap)) return enchantments;

        enchantMap.forEach((key, level) -> {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(String.valueOf(key).toLowerCase(Locale.ROOT)));
            if (enchantment == null) {
                logger.warning("Unknown enchantment '" + key + "' on loot entry '" + material + "' — ignored.");
            } else {
                enchantments.put(enchantment, toInt(level, 1));
            }
        });
        return enchantments;
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
