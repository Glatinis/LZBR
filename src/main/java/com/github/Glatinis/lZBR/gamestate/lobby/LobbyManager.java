package com.github.Glatinis.lZBR.gamestate.lobby;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbyManager {
    // TODO: Get max lobby size from config
    private final int MAX_SIZE = 25;
    private List<UUID> lobbyPlayers = new ArrayList<>();

    public LobbyManager() {}

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
        return lobbyPlayers.size() >= MAX_SIZE;
    }
}
