package com.github.Glatinis.lZBR.loot.chest;

import com.github.Glatinis.lZBR.loot.table.LootTable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

// Performs the world-side work for loot chests: (re)placing the chest block with its saved facing,
// filling it with rolled loot, and emptying it. Holds no state — that's ChestRegistry's job.
public class ChestService {
    private final Logger logger;
    private final Random random = new Random();

    public ChestService(Logger logger) {
        this.logger = logger;
    }

    // Places (with the stored facing), empties and re-fills the chest. Returns true if it was filled.
    public boolean fill(ChestLocation chest, LootTable table, ChestFillSettings settings) {
        World world = Bukkit.getWorld(chest.world());
        if (world == null) {
            logger.warning("Cannot fill loot chest: world '" + chest.world() + "' is not loaded.");
            return false;
        }

        Block block = world.getBlockAt(chest.x(), chest.y(), chest.z());
        placeChest(block, chest.facing());

        if (!(block.getState() instanceof Container container)) {
            logger.warning("Cannot fill loot chest at " + chest.x() + ", " + chest.y() + ", " + chest.z()
                    + " — block is not a container.");
            return false;
        }

        Inventory inventory = container.getInventory();
        inventory.clear();
        fillInventory(inventory, table, settings);
        return true;
    }

    public void clear(ChestLocation chest) {
        containerAt(chest).ifPresent(container -> container.getInventory().clear());
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

    private void fillInventory(Inventory inventory, LootTable table, ChestFillSettings settings) {
        if (table.isEmpty()) return;

        int min = Math.max(0, settings.minItems());
        int max = Math.max(min, settings.maxItems());
        int size = inventory.getSize();
        int count = Math.min(size, (min == max) ? min : min + random.nextInt(max - min + 1));

        List<Integer> slots = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) slots.add(slot);
        if (settings.scatter()) {
            Collections.shuffle(slots, random);
        }

        for (int i = 0; i < count; i++) {
            ItemStack item = table.roll();
            if (item == null) break;
            inventory.setItem(slots.get(i), item);
        }
    }

    private Optional<Container> containerAt(ChestLocation chest) {
        World world = Bukkit.getWorld(chest.world());
        if (world == null) return Optional.empty();
        Block block = world.getBlockAt(chest.x(), chest.y(), chest.z());
        return (block.getState() instanceof Container container) ? Optional.of(container) : Optional.empty();
    }
}
