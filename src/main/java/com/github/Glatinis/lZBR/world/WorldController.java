package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.core.LZBR;
import org.bukkit.World;
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

    // Resolves the loaded BR world, or null if Multiverse isn't hooked yet or the configured world
    // doesn't exist. Returning the world (rather than swallowing the failure) lets callers abort
    // cleanly instead of running a match nobody was teleported into.
    public World resolveBrWorld() {
        if (!multiverseAvailable) {
            plugin.getLogger().warning("Tried to access the BR world, but Multiverse-Core isn't loaded.");
            return null;
        }

        String worldName = configRepository.getBrWorldName();
        World[] resolved = { null };

        WorldManager worldManager = multiverseApi.getWorldManager();
        worldManager.getWorld(worldName)
                .peek(mvWorld -> resolved[0] = mvWorld.getSpawnLocation().getWorld())
                .onEmpty(() -> plugin.getLogger().warning(
                        "Could not find BR world '" + worldName + "'. Check worlds.br in config.yml."));

        return resolved[0];
    }

    // Pre-flight check so an admin gets told before the countdown rather than after it.
    public boolean isBrWorldReady() {
        return resolveBrWorld() != null;
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
