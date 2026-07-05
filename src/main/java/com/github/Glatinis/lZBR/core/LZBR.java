package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.commands.LZBRCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.gamestate.br.BRManager;
import com.github.Glatinis.lZBR.gamestate.br.BRService;
import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.gamestate.listeners.PlayerQuitListener;
import com.github.Glatinis.lZBR.world.WorldController;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LZBR extends JavaPlugin {

    private ConfigRepository configRepository;
    private GameStateController gameStateController;
    private WorldController worldController;

    private BRManager brManager;
    private BRService brService;
    private LobbyManager lobbyManager;

    @Override
    public void onEnable() {
        configRepository = new ConfigRepository(this);
        worldController = new WorldController(this, configRepository);

        lobbyManager = new LobbyManager(configRepository);
        brService = new BRService(worldController);
        brManager = new BRManager(brService);

        gameStateController = new GameStateController(lobbyManager, brManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    new LZBRCommand(gameStateController, worldController).build(),
                    "LiveZone Battle Royale admin command",
                    List.of("lz") // alias so /lz start also works
            );
        });

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gameStateController), this);

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
