package com.github.Glatinis.lZBR.world;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.core.LZBR;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.WorldManager;

import java.util.function.Consumer;

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

    // Resolves the loaded BR world and hands it to the callback. Used instead of a single fixed
    // teleport target so callers (e.g. player scatter) can pick their own locations within it.
    public void withBrWorld(Consumer<World> whenLoaded) {
        if (!multiverseAvailable) {
            plugin.getLogger().warning("Tried to access the BR world, but Multiverse-Core isn't loaded.");
            return;
        }

        WorldManager worldManager = multiverseApi.getWorldManager();
        worldManager.getWorld(configRepository.getBrWorldName()).peek(mvWorld -> {
            whenLoaded.accept(mvWorld.getSpawnLocation().getWorld());
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
