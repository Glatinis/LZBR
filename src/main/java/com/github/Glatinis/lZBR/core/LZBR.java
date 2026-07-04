package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.commands.LZBRCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.world.WorldController;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LZBR extends JavaPlugin {

    private GameStateController gameStateController;
    private LobbyManager lobbyManager;
    private WorldController worldController;

    @Override
    public void onEnable() {
        lobbyManager = new LobbyManager();
        gameStateController = new GameStateController(lobbyManager);
        worldController = new WorldController(this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    new LZBRCommand(gameStateController).build(),
                    "LiveZone Battle Royale admin command",
                    List.of("lz") // alias so /lz start also works
            );
        });

        if (getServer().getPluginManager().getPlugin("Multiverse-Core") == null) {
            getLogger().severe("Multiverse-Core is not installed! World switching will not work until it's added.");
        }

        getLogger().info("Loaded plugin");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloaded plugin");
    }
}
