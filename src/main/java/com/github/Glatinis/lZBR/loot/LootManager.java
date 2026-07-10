package com.github.Glatinis.lZBR.loot;

import com.github.Glatinis.lZBR.core.LootRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

// Ties the loot table to the world: places and fills the configured chests at the start of every
// match (fresh loot each time), and handles the in-game add/remove/list of chest positions.
public class LootManager {
    private static final int TARGET_RANGE = 6;

    private final Logger logger;
    private final LootRepository repository;
    private final Random random = new Random();

    private LootTable table;
    private final List<ChestLocation> chests = new ArrayList<>();

    public LootManager(JavaPlugin plugin, LootRepository repository) {
        this.logger = plugin.getLogger();
        this.repository = repository;
        load();
    }

    public void reload() {
        repository.reload();
        load();
    }

    private void load() {
        table = LootTable.load(repository, logger);
        chests.clear();
        chests.addAll(repository.loadChestLocations());
    }

    public List<ChestLocation> getChests() {
        return Collections.unmodifiableList(chests);
    }

    public int getLootItemCount() {
        return table.itemCount();
    }

    // Draws a single item from the loot table (weighted by rarity), or null if the table is empty.
    // Shared with the mob system so mob drops use the same items and probabilities as chests.
    public ItemStack rollLoot() {
        return table.roll();
    }

    // --- Match hooks ---------------------------------------------------------

    // Places (with the stored facing), empties and re-fills every chest. Returns how many were filled.
    public int fillAllChests() {
        int filled = 0;
        for (ChestLocation chest : chests) {
            if (fillChest(chest)) filled++;
        }
        return filled;
    }

    public void clearAllChests() {
        for (ChestLocation chest : chests) {
            Container container = containerAt(chest);
            if (container != null) container.getInventory().clear();
        }
    }

    private boolean fillChest(ChestLocation chest) {
        World world = Bukkit.getWorld(chest.world());
        if (world == null) {
            logger.warning("Cannot fill loot chest: world '" + chest.world() + "' is not loaded.");
            return false;
        }

        Block block = world.getBlockAt(chest.x(), chest.y(), chest.z());
        placeChest(block, chest.facing());

        if (!(block.getState() instanceof Container container)) {
            logger.warning("Cannot fill loot chest at " + describe(chest) + " — block is not a container.");
            return false;
        }

        Inventory inventory = container.getInventory();
        inventory.clear();
        fillInventory(inventory);
        return true;
    }

    // Ensures the block is a chest facing the stored direction, restoring it if the arena reset (or a
    // player) changed it.
    private void placeChest(Block block, BlockFace facing) {
        if (block.getType() != Material.CHEST) {
            block.setType(Material.CHEST, false);
        }
        BlockData data = block.getBlockData();
        if (data instanceof org.bukkit.block.data.type.Chest chestData) {
            chestData.setFacing(facing);
            // Keep chests single so a neighbouring chest never merges into a double chest.
            chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
            block.setBlockData(chestData, false);
        }
    }

    private void fillInventory(Inventory inventory) {
        if (table.isEmpty()) return;

        int min = Math.max(0, repository.getMinItemsPerChest());
        int max = Math.max(min, repository.getMaxItemsPerChest());
        int size = inventory.getSize();
        int count = Math.min(size, (min == max) ? min : min + random.nextInt(max - min + 1));

        List<Integer> slots = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) slots.add(slot);
        if (repository.isScatterItems()) {
            Collections.shuffle(slots, random);
        }

        for (int i = 0; i < count; i++) {
            ItemStack item = table.roll();
            if (item == null) break;
            inventory.setItem(slots.get(i), item);
        }
    }

    // --- In-game management --------------------------------------------------

    public AddResult addTargetedChest(Player player) {
        Block target = player.getTargetBlockExact(TARGET_RANGE);
        if (target == null || target.getType() != Material.CHEST) {
            return AddResult.NOT_A_CHEST;
        }
        if (chests.stream().anyMatch(chest -> chest.isAt(target))) {
            return AddResult.ALREADY_ADDED;
        }

        BlockFace facing = (target.getBlockData() instanceof Directional directional)
                ? directional.getFacing()
                : BlockFace.NORTH;

        chests.add(ChestLocation.of(target, facing));
        repository.saveChestLocations(chests);
        return AddResult.ADDED;
    }

    public boolean removeTargetedChest(Player player) {
        Block target = player.getTargetBlockExact(TARGET_RANGE);
        if (target == null) return false;

        boolean removed = chests.removeIf(chest -> chest.isAt(target));
        if (removed) {
            repository.saveChestLocations(chests);
        }
        return removed;
    }

    public static String describe(ChestLocation chest) {
        return chest.world() + " " + chest.x() + ", " + chest.y() + ", " + chest.z()
                + " (" + chest.facing() + ")";
    }

    private Container containerAt(ChestLocation chest) {
        World world = Bukkit.getWorld(chest.world());
        if (world == null) return null;
        Block block = world.getBlockAt(chest.x(), chest.y(), chest.z());
        return (block.getState() instanceof Container container) ? container : null;
    }

    public enum AddResult { ADDED, ALREADY_ADDED, NOT_A_CHEST }
}
