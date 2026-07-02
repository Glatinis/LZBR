package com.github.Glatinis.lZBR.gamestate.lobby;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbyManager {
    private List<UUID> lobbyPlayers = new ArrayList<>();

    public LobbyManager() {}

    public List<UUID> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public void addLobbyPlayer(UUID playerUUID) {
        lobbyPlayers.add(playerUUID);
    }

    public void addLobbyPlayer(Player player) {
        lobbyPlayers.add(player.getUniqueId());
    }

    public void removeLobbyPlayer(UUID playerUUID) {
        lobbyPlayers.remove(playerUUID);
    }

    public void removeLobbyPlayer(Player player) {
        lobbyPlayers.remove(player.getUniqueId());
    }
}
