package com.github.Glatinis.lZBR.gamestate.lobby;

import com.github.Glatinis.lZBR.core.ConfigRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbyManager {
    private final ConfigRepository configRepository;
    private List<UUID> lobbyPlayers = new ArrayList<>();

    public LobbyManager(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<UUID> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public void addLobbyPlayer(UUID playerUUID) {
        lobbyPlayers.add(playerUUID);
    }

    public void removeLobbyPlayer(UUID playerUUID) {
        lobbyPlayers.remove(playerUUID);
    }

    public boolean isInLobby(UUID playerUUID) {
        return lobbyPlayers.contains(playerUUID);
    }

    public boolean isFull() {
        return lobbyPlayers.size() >= configRepository.getMaximumPlayerCount();
    }
}
