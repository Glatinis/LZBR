package com.github.Glatinis.lZBR.loot.table;

import com.github.Glatinis.lZBR.ui.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// One loot-table entry. toItemStack() rolls a fresh ItemStack each call, so the same entry can yield a
// different stack size every draw.
public record LootItem(
        Material material,
        int minAmount,
        int maxAmount,
        Rarity rarity,
        String name,                          // MiniMessage template, may be null/blank
        List<String> lore,                    // MiniMessage templates
        Map<Enchantment, Integer> enchantments,
        boolean showRarity) {

    public LootItem {
        minAmount = Math.max(1, minAmount);
        maxAmount = Math.max(minAmount, maxAmount);
        lore = List.copyOf(lore);
        enchantments = Map.copyOf(enchantments);
    }

    public String rarityId() {
        return rarity.id();
    }

    public ItemStack toItemStack(Random random) {
        int amount = (minAmount == maxAmount) ? minAmount : minAmount + random.nextInt(maxAmount - minAmount + 1);
        ItemStack stack = new ItemStack(material, amount);

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        if (name != null && !name.isBlank()) {
            meta.displayName(plain(Text.mini(name)));
        }

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(plain(Text.mini(line)));
        }
        if (showRarity && rarity.displayName() != null && !rarity.displayName().isBlank()) {
            loreLines.add(plain(Text.mini(rarity.displayName())));
        }
        if (!loreLines.isEmpty()) {
            meta.lore(loreLines);
        }

        enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));

        stack.setItemMeta(meta);
        return stack;
    }

    // Item names/lore render italic by default; pin it off so config text shows exactly as written.
    private static Component plain(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
