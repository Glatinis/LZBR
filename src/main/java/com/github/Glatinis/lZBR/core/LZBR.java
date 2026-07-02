package com.github.Glatinis.lZBR.core;

import com.github.Glatinis.lZBR.commands.LZBRCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LZBR extends JavaPlugin {

    private GameStateController gameStateController;

    @Override
    public void onEnable() {
        gameStateController = new GameStateController();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    new LZBRCommand(gameStateController).build(),
                    "LiveZone Battle Royale admin command",
                    List.of("lz") // alias so /lz start also works
            );
        });

        getLogger().info("Loaded plugin");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloaded plugin");
    }
}
