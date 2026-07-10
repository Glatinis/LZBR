package com.github.Glatinis.lZBR.loot;

import com.github.Glatinis.lZBR.ui.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// A single loot-table entry. create() rolls a fresh ItemStack each time, so the same entry can yield
// a different stack size on every draw.
public final class LootItem {
    private final Material material;
    private final int minAmount;
    private final int maxAmount;
    private final Rarity rarity;
    private final String name;                       // MiniMessage template, may be null
    private final List<String> lore;                 // MiniMessage templates
    private final Map<Enchantment, Integer> enchantments;
    private final boolean showRarity;

    public LootItem(Material material, int minAmount, int maxAmount, Rarity rarity,
                    String name, List<String> lore, Map<Enchantment, Integer> enchantments, boolean showRarity) {
        this.material = material;
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
        this.rarity = rarity;
        this.name = name;
        this.lore = lore;
        this.enchantments = enchantments;
        this.showRarity = showRarity;
    }

    public String rarityId() {
        return rarity.id();
    }

    public ItemStack create(Random random) {
        int amount = (minAmount == maxAmount) ? minAmount : minAmount + random.nextInt(maxAmount - minAmount + 1);
        ItemStack stack = new ItemStack(material, amount);

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        if (name != null && !name.isBlank()) {
            meta.displayName(withoutItalic(Text.mini(name)));
        }

        List<Component> loreLines = new ArrayList<>();
        for (String line : lore) {
            loreLines.add(withoutItalic(Text.mini(line)));
        }
        if (showRarity && rarity.displayName() != null && !rarity.displayName().isBlank()) {
            loreLines.add(withoutItalic(Text.mini(rarity.displayName())));
        }
        if (!loreLines.isEmpty()) {
            meta.lore(loreLines);
        }

        enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));

        stack.setItemMeta(meta);
        return stack;
    }

    // Item names/lore render italic by default; pin it off so config text shows exactly as written.
    private static Component withoutItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
