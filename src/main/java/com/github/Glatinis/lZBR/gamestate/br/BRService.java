package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.world.WorldController;
import com.github.Glatinis.lZBR.world.spawn.PlayerScatterService;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BRService {
    private final WorldController worldController;
    private final PlayerScatterService scatterService;

    public BRService(WorldController worldController, PlayerScatterService scatterService) {
        this.worldController = worldController;
        this.scatterService = scatterService;
    }

    // Returns the arena world players were sent to, or null if it couldn't be resolved. On null nobody
    // has been moved or had their state touched, so the caller can abort the match safely.
    public World sendToArena(Collection<Player> players) {
        World arena = worldController.resolveBrWorld();
        if (arena == null) return null;

        // Copy the roster first: scatter teleports players, and a teleport can fire events that
        // mutate the live roster mid-iteration.
        List<Player> participants = new ArrayList<>(players);
        participants.forEach(this::resetState);
        scatterService.scatter(arena, participants);
        return arena;
    }

    public void returnToLobby(Collection<Player> players) {
        for (Player player : players) {
            // A player killed by the final blow is still on the death screen; clear it so we can heal
            // and move them.
            if (player.isDead()) {
                player.spigot().respawn();
            }
            resetState(player);
            worldController.teleportToLobby(player);
        }
    }

    private void resetState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setLevel(0);
        player.setExp(0f);

        // Everyone enters the arena empty-handed so all gear comes from arena loot, and nothing carries
        // back out into the lobby or the next match.
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setItemInOffHand(null);

        if (!player.isDead()) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                player.setHealth(maxHealth.getValue());
            }
        }

        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }
}
