package com.github.Glatinis.lZBR.gamestate;

import com.github.Glatinis.lZBR.gamestate.br.BRManager;
import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.github.Glatinis.lZBR.returncode.LeaveCode;
import com.github.Glatinis.lZBR.returncode.StartCode;
import com.github.Glatinis.lZBR.world.WorldController;
import com.github.Glatinis.lZBR.world.arena.ArenaResetService;
import com.github.Glatinis.lZBR.world.zone.ZoneController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GameStateController {
    private GameState gameState = GameState.LOBBY;

    private LobbyManager lobbyManager;
    private BRManager brManager;
    private ZoneController zoneController;
    private WorldController worldController;
    private ArenaResetService arenaResetService;

    public GameStateController(LobbyManager lobbyManager, BRManager brManager, ZoneController zoneController,
                              WorldController worldController, ArenaResetService arenaResetService) {
        this.lobbyManager = lobbyManager;
        this.brManager = brManager;
        this.zoneController = zoneController;
        this.worldController = worldController;
        this.arenaResetService = arenaResetService;
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
        zoneController.start(onlinePlayers, brManager::getPlayers);

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

    public void handlePlayerQuit(Player player) {
        if (gameState == GameState.LOBBY) {
            lobbyManager.removeLobbyPlayer(player.getUniqueId());
            return;
        }

        if (gameState == GameState.PRE_GAME || gameState == GameState.IN_GAME) {
            brManager.removePlayer(player);
        }
    }

    public void handlePlayerDeath(Player player) {
        if (gameState == GameState.PRE_GAME || gameState == GameState.IN_GAME) {
            brManager.eliminatePlayer(player);
        }
    }

    public boolean isSpectatorInBR(Player player) {
        return brManager.isSpectator(player.getUniqueId());
    }

    public void sendZoneBorder(Player player) {
        zoneController.sendBorderTo(player);
    }

    public void stopZone() {
        zoneController.stop();
    }

    // Ends the current match: stops the zone, sends everyone back to the lobby, and resets the arena
    // (pasting a fresh schematic) before returning to the LOBBY state so the next round starts clean.
    // Returns false if no match is in progress.
    public boolean endGame() {
        if (gameState == GameState.LOBBY)
            return false;

        gameState = GameState.POST_GAME;
        zoneController.stop();
        returnPlayersToLobby();

        arenaResetService.reset(success -> {
            brManager.reset();
            gameState = GameState.LOBBY;
        });
        return true;
    }

    // Manually triggers an arena reset (admin/testing). Does not touch match state.
    public void resetArena(Consumer<Boolean> onComplete) {
        arenaResetService.reset(onComplete);
    }

    private void returnPlayersToLobby() {
        List<Player> matchPlayers = new ArrayList<>(brManager.getPlayers());
        brManager.getSpectators().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(matchPlayers::add);

        for (Player player : matchPlayers) {
            player.setGameMode(GameMode.SURVIVAL);
            worldController.teleportToLobby(player);
        }
    }

    // Starts a small test zone centered on the player, for trying out the zone feature outside of a real match.
    // Refuses if a zone (real match or another test) is already running, since only one can be active at a time.
    public boolean startZoneTest(Player player) {
        if (zoneController.isActive())
            return false;

        zoneController.startTest(player, 15.0, 3.0, 5, 15);
        return true;
    }
}
