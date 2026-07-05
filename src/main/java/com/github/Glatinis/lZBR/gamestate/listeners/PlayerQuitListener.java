package com.github.Glatinis.lZBR.gamestate.listeners;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final GameStateController gameStateController;

    public PlayerQuitListener(GameStateController gameStateController) {
        this.gameStateController = gameStateController;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        gameStateController.handlePlayerQuit(event.getPlayer());
    }
}
