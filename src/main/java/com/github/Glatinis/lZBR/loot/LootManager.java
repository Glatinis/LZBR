package com.github.Glatinis.lZBR.loot;

import com.github.Glatinis.lZBR.core.LootRepository;
import com.github.Glatinis.lZBR.loot.chest.ChestAddResult;
import com.github.Glatinis.lZBR.loot.chest.ChestLocation;
import com.github.Glatinis.lZBR.loot.chest.ChestRegistry;
import com.github.Glatinis.lZBR.loot.chest.ChestService;
import com.github.Glatinis.lZBR.loot.table.LootTable;
import com.github.Glatinis.lZBR.loot.table.LootTableLoader;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

// Orchestrates the loot feature: composes the loot table, the chest registry (positions) and the chest
// service (world ops), and exposes the handful of operations the match flow and commands need. Kept
// deliberately thin — the real work lives in the collaborators.
public class LootManager {
    private static final int TARGET_RANGE = 6;

    private final LootRepository repository;
    private final LootTableLoader tableLoader;
    private final ChestRegistry registry;
    private final ChestService chestService;

    private LootTable table;

    public LootManager(JavaPlugin plugin, LootRepository repository) {
        this.repository = repository;
        this.tableLoader = new LootTableLoader(plugin.getLogger());
        this.registry = new ChestRegistry(repository);
        this.chestService = new ChestService(plugin.getLogger());
        this.table = tableLoader.load(repository);
    }

    public void reload() {
        repository.reload();
        table = tableLoader.load(repository);
        registry.reload();
    }

    // --- Match hooks ---------------------------------------------------------

    // Places, empties and re-fills every chest with fresh loot. Returns how many were filled.
    public int fillAllChests() {
        int filled = 0;
        for (ChestLocation chest : registry.getChests()) {
            if (chestService.fill(chest, table, repository.getChestFillSettings())) filled++;
        }
        return filled;
    }

    public void clearAllChests() {
        registry.getChests().forEach(chestService::clear);
    }

    // Shared with the mob system so mob drops use the same items and probabilities as chests.
    public ItemStack rollLoot() {
        return table.roll();
    }

    // --- Command-facing operations -------------------------------------------

    public ChestAddResult addTargetedChest(Player player) {
        Block target = player.getTargetBlockExact(TARGET_RANGE);
        if (target == null || target.getType() != Material.CHEST) {
            return ChestAddResult.NOT_A_CHEST;
        }

        BlockFace facing = (target.getBlockData() instanceof Directional directional)
                ? directional.getFacing()
                : BlockFace.NORTH;
        ChestLocation chest = ChestLocation.of(target, facing);

        if (registry.contains(chest)) {
            return ChestAddResult.ALREADY_ADDED;
        }
        registry.add(chest);
        return ChestAddResult.ADDED;
    }

    public boolean removeTargetedChest(Player player) {
        Block target = player.getTargetBlockExact(TARGET_RANGE);
        return target != null && registry.removeAt(target);
    }

    public List<ChestLocation> getChests() {
        return registry.getChests();
    }

    public int getLootItemCount() {
        return table.itemCount();
    }
}
