package com.github.Glatinis.lZBR.gamestate.listeners;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerBRListener implements Listener {
    private final GameStateController gameStateController;
    private final JavaPlugin plugin;

    public PlayerBRListener(GameStateController gameStateController, JavaPlugin plugin) {
        this.gameStateController = gameStateController;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        gameStateController.handlePlayerDeath(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!gameStateController.shouldSpectateOnRespawn(player)) return;

        // Delay by 1 tick so the respawn teleport completes before the gamemode is applied
        plugin.getServer().getScheduler().runTask(plugin, () -> player.setGameMode(GameMode.SPECTATOR));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        gameStateController.sendZoneBorder(event.getPlayer());
    }
}
