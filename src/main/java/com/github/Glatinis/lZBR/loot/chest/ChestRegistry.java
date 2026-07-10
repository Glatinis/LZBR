package com.github.Glatinis.lZBR.loot.chest;

import com.github.Glatinis.lZBR.core.LootRepository;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

// Owns the set of registered loot-chest positions and keeps it persisted. Pure bookkeeping — placing
// and filling the actual chest blocks is ChestService's job.
public class ChestRegistry {
    private final LootRepository repository;
    private final List<ChestLocation> chests = new ArrayList<>();

    public ChestRegistry(LootRepository repository) {
        this.repository = repository;
        reload();
    }

    public void reload() {
        chests.clear();
        chests.addAll(repository.loadChestLocations());
    }

    public List<ChestLocation> getChests() {
        return List.copyOf(chests);
    }

    public int size() {
        return chests.size();
    }

    public boolean contains(ChestLocation chest) {
        return chests.stream().anyMatch(existing -> existing.samePosition(chest));
    }

    public void add(ChestLocation chest) {
        chests.add(chest);
        repository.saveChestLocations(chests);
    }

    public boolean removeAt(Block block) {
        boolean removed = chests.removeIf(chest -> chest.isAt(block));
        if (removed) {
            repository.saveChestLocations(chests);
        }
        return removed;
    }
}
