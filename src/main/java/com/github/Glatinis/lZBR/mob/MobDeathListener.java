package com.github.Glatinis.lZBR.mob;

import com.github.Glatinis.lZBR.core.MobRepository;
import com.github.Glatinis.lZBR.loot.LootManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

// Grants loot to LZBR mobs when they die, pulling items from the shared loot table so mob loot matches
// chest loot. Only mobs we tagged are affected, so player deaths and PvP drops are left untouched.
public class MobDeathListener implements Listener {
    private final ActiveMobs activeMobs;
    private final MobRepository repository;
    private final LootManager lootManager;
    private final Random random = new Random();

    public MobDeathListener(ActiveMobs activeMobs, MobRepository repository, LootManager lootManager) {
        this.activeMobs = activeMobs;
        this.repository = repository;
        this.lootManager = lootManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!activeMobs.isLzbrMob(entity)) return;

        activeMobs.untrack(entity.getUniqueId());

        DropSettings settings = repository.getDropSettings();
        if (!settings.keepVanilla()) {
            event.getDrops().clear();
        }

        if (random.nextDouble() * 100.0 >= settings.chance()) return;

        int min = Math.max(0, settings.minRolls());
        int max = Math.max(min, settings.maxRolls());
        int rolls = (min == max) ? min : min + random.nextInt(max - min + 1);

        for (int i = 0; i < rolls; i++) {
            ItemStack loot = lootManager.rollLoot();
            if (loot != null) event.getDrops().add(loot);
        }
    }
}
