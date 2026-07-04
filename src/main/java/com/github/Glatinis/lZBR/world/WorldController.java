package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.core.LZBR;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.WorldManager;

public class WorldController {
    private final LZBR plugin;
    private final ConfigRepository configRepository;
    private MultiverseCoreApi multiverseApi;
    private boolean multiverseAvailable = false;

    public WorldController(LZBR plugin, ConfigRepository configRepository) {
        this.plugin = plugin;
        this.configRepository = configRepository;
        hookMultiverse();
    }

    private void hookMultiverse() {
        MultiverseCoreApi.whenLoaded(api -> {
            this.multiverseApi = api;
            this.multiverseAvailable = true;
            plugin.getLogger().info("Hooked into Multiverse-Core successfully.");
        });
    }

    public boolean isMultiverseAvailable() {
        return multiverseAvailable;
    }

    public void teleportToBR(Player player) {
        if (!multiverseAvailable) {
            plugin.getLogger().warning("Tried to teleport to BR world, but Multiverse-Core isn't loaded.");
            return;
        }

        WorldManager worldManager = multiverseApi.getWorldManager();
        worldManager.getWorld(configRepository.getBrWorldName()).peek(mvWorld -> {
            player.teleport(mvWorld.getSpawnLocation());
        }).onEmpty(() -> {
            plugin.getLogger().warning("Could not find BR world.");
        });
    }

    public void teleportToLobby(Player player) {
        if (!multiverseAvailable) {
            plugin.getLogger().warning("Tried to teleport to lobby, but Multiverse-Core isn't loaded.");
            return;
        }

        WorldManager worldManager = multiverseApi.getWorldManager();
        worldManager.getWorld(configRepository.getLobbyWorldName()).peek(mvWorld -> {
            player.teleport(mvWorld.getSpawnLocation());
        }).onEmpty(() -> {
            plugin.getLogger().warning("Could not find lobby world.");
        });
    }
}
