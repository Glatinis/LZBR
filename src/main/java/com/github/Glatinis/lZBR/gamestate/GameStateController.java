package com.github.Glatinis.lZBR.gamestate;

import com.github.Glatinis.lZBR.gamestate.br.BRManager;
import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.github.Glatinis.lZBR.returncode.LeaveCode;
import com.github.Glatinis.lZBR.returncode.StartCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameStateController {
    private GameState gameState = GameState.LOBBY;

    private LobbyManager lobbyManager;
    private BRManager brManager;

    public GameStateController(LobbyManager lobbyManager, BRManager brManager) {
        this.lobbyManager = lobbyManager;
        this.brManager = brManager;
    }

    public GameState getGameState() {
        return gameState;
    }

    public StartCode startGame() {
        if (gameState != GameState.LOBBY)
            return StartCode.GAME_IN_PROGRESS;

        // TODO: Add player check to return error if player count too low

        gameState = GameState.PRE_GAME;

        // TODO: Also implement listener for detecting leaving players so this is a safeguard only
        List<Player> onlinePlayers = lobbyManager.getLobbyPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

        brManager.startPreGame(onlinePlayers);

        return StartCode.SUCCESS;
    }

    public JoinCode joinLobby(Player player) {
        if (lobbyManager.isInLobby(player.getUniqueId()))
            return JoinCode.ALREADY_IN_LOBBY;
        else if (!gameState.equals(GameState.LOBBY))
            return JoinCode.GAME_STARTED;
        else if (lobbyManager.isFull())
            return JoinCode.LOBBY_FULL;

        lobbyManager.addLobbyPlayer(player.getUniqueId());
        return JoinCode.SUCCESS;
    }

    public LeaveCode leaveLobby(Player player) {
        if (!lobbyManager.isInLobby(player.getUniqueId()))
            return LeaveCode.NOT_IN_LOBBY;
        else if (!gameState.equals(GameState.LOBBY))
            return LeaveCode.GAME_STARTED;

        lobbyManager.removeLobbyPlayer(player.getUniqueId());
        return LeaveCode.SUCCESS;
    }
}
