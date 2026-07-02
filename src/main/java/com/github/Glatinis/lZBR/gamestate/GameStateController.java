package com.github.Glatinis.lZBR.gamestate;

import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.returncode.StartCode;

public class GameStateController {
    private GameState gameState = GameState.LOBBY;

    private LobbyManager lobbyManager;

    public GameStateController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public GameState getGameState() {
        return gameState;
    }

    public StartCode startMatch() {
        if (gameState != GameState.LOBBY)
            return StartCode.MATCH_IN_PROGRESS;

        // TODO: Add player check to return error if player count too low

        gameState = GameState.PRE_MATCH;
        return StartCode.SUCCESS;
    }
}
