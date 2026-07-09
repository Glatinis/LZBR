package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.world.WorldController;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;

public class BRService {
    private final WorldController worldController;

    public BRService(WorldController worldController) {
        this.worldController = worldController;
    }

    public void sendToArena(Collection<Player> players) {
        for (Player player : players) {
            resetState(player);
            worldController.teleportToBR(player);
        }
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
