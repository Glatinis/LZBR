package com.github.Glatinis.lZBR.gamestate;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.gamestate.br.BRManager;
import com.github.Glatinis.lZBR.gamestate.br.MatchAnnouncer;
import com.github.Glatinis.lZBR.gamestate.br.MatchCountdown;
import com.github.Glatinis.lZBR.gamestate.lobby.LobbyManager;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.github.Glatinis.lZBR.returncode.LeaveCode;
import com.github.Glatinis.lZBR.returncode.StartCode;
import com.github.Glatinis.lZBR.world.zone.ZoneController;
import com.github.Glatinis.lZBR.world.arena.ArenaResetService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

// Coordinates the whole game lifecycle: lobby queue -> countdown -> live match -> win/end -> reset.
// It owns the GameState and sequences the collaborators (lobby, roster, zone, countdown, announcer,
// arena reset); each of those owns its own slice of the work.
public class GameStateController {
    private static final long TICKS_PER_SECOND = 20L;

    private final JavaPlugin plugin;
    private final LobbyManager lobbyManager;
    private final BRManager brManager;
    private final ZoneController zoneController;
    private final ArenaResetService arenaResetService;
    private final ConfigRepository config;

    private final MatchCountdown countdown;
    private final MatchAnnouncer announcer;

    private GameState gameState = GameState.LOBBY;

    public GameStateController(JavaPlugin plugin, ConfigRepository config, LobbyManager lobbyManager,
                              BRManager brManager, ZoneController zoneController, ArenaResetService arenaResetService) {
        this.plugin = plugin;
        this.config = config;
        this.lobbyManager = lobbyManager;
        this.brManager = brManager;
        this.zoneController = zoneController;
        this.arenaResetService = arenaResetService;
        this.countdown = new MatchCountdown(plugin, config);
        this.announcer = new MatchAnnouncer(config, plugin.getLogger());
    }

    public GameState getGameState() {
        return gameState;
    }

    // --- Lobby ---------------------------------------------------------------

    public JoinCode joinLobby(Player player) {
        if (lobbyManager.isInLobby(player.getUniqueId()))
            return JoinCode.ALREADY_IN_LOBBY;
        else if (gameState != GameState.LOBBY)
            return JoinCode.GAME_STARTED;
        else if (lobbyManager.isFull())
            return JoinCode.LOBBY_FULL;

        lobbyManager.addLobbyPlayer(player.getUniqueId());
        return JoinCode.SUCCESS;
    }

    public LeaveCode leaveLobby(Player player) {
        if (!lobbyManager.isInLobby(player.getUniqueId()))
            return LeaveCode.NOT_IN_LOBBY;
        else if (gameState != GameState.LOBBY)
            return LeaveCode.GAME_STARTED;

        lobbyManager.removeLobbyPlayer(player.getUniqueId());
        return LeaveCode.SUCCESS;
    }

    // --- Match lifecycle -----------------------------------------------------

    public StartCode startGame() {
        if (gameState != GameState.LOBBY)
            return StartCode.GAME_IN_PROGRESS;

        List<Player> participants = onlineLobbyPlayers();
        if (participants.size() < config.getMinimumPlayerCount())
            return StartCode.PLAYER_COUNT_INSUFFICIENT;

        gameState = GameState.PRE_GAME;
        lobbyManager.clear();
        brManager.prepareMatch(participants);

        // Count down in the lobby, then go live. The roster supplier keeps the audience current if
        // players drop out during the countdown.
        countdown.start(brManager::getPlayers, this::beginMatch);
        return StartCode.SUCCESS;
    }

    private void beginMatch() {
        gameState = GameState.IN_GAME;

        List<Player> players = brManager.getPlayers();
        if (players.size() <= 1) {
            finishMatch(players.isEmpty() ? null : players.get(0));
            return;
        }

        brManager.sendToArena();
        announcer.announceStart(players);
        zoneController.start(players, brManager::getPlayers);
    }

    // Admin-triggered early end. Returns false if there is no match to end.
    public boolean endGame() {
        if (gameState != GameState.PRE_GAME && gameState != GameState.IN_GAME)
            return false;

        finishMatch(null);
        return true;
    }

    private void checkForWinner() {
        if (gameState != GameState.IN_GAME) return;

        List<Player> alive = brManager.getPlayers();
        if (alive.size() <= 1) {
            finishMatch(alive.isEmpty() ? null : alive.get(0));
        }
    }

    // Wraps up the match: freezes the zone, shows the win banner, then (after a delay) sends everyone
    // to the lobby and resets the arena before returning to LOBBY.
    private void finishMatch(Player winner) {
        gameState = GameState.POST_GAME;
        countdown.cancel();
        zoneController.stop();

        List<Player> participants = brManager.getAllParticipants();
        if (winner != null) {
            announcer.announceWinner(participants, winner.getName());
        } else {
            announcer.announceNoWinner(participants);
        }

        long delayTicks = (long) Math.max(0, config.getWinReturnDelaySeconds()) * TICKS_PER_SECOND;
        plugin.getServer().getScheduler().runTaskLater(plugin, this::returnToLobbyAndReset, delayTicks);
    }

    private void returnToLobbyAndReset() {
        brManager.returnAllToLobby();
        arenaResetService.reset(success -> {
            brManager.reset();
            gameState = GameState.LOBBY;
        });
    }

    // --- Player events -------------------------------------------------------

    public void handlePlayerQuit(Player player) {
        if (gameState == GameState.LOBBY) {
            lobbyManager.removeLobbyPlayer(player.getUniqueId());
            return;
        }

        if (gameState == GameState.PRE_GAME || gameState == GameState.IN_GAME) {
            brManager.removePlayer(player);
            checkForWinner();
        }
    }

    public void handlePlayerDeath(Player player) {
        // Only during the live match — deaths in the lobby during the countdown don't eliminate anyone.
        if (gameState == GameState.IN_GAME) {
            brManager.eliminatePlayer(player);
            checkForWinner();
        }
    }

    // Whether a respawning player should be put into spectator mode: only while a match is actually
    // running, so the post-game cleanup that respawns eliminated players doesn't re-lock them as
    // spectators just before they are sent back to the lobby.
    public boolean shouldSpectateOnRespawn(Player player) {
        return (gameState == GameState.IN_GAME || gameState == GameState.PRE_GAME)
                && brManager.isSpectator(player.getUniqueId());
    }

    public void sendZoneBorder(Player player) {
        zoneController.sendBorderTo(player);
    }

    // --- Admin / testing tools ----------------------------------------------

    public void stopZone() {
        zoneController.stop();
    }

    // Manually triggers an arena reset (admin/testing). Does not touch match state.
    public void resetArena(Consumer<Boolean> onComplete) {
        arenaResetService.reset(onComplete);
    }

    // Starts a small test zone centered on the player, for trying out the zone feature outside a match.
    // Refuses if a zone (real match or another test) is already running, since only one can be active.
    public boolean startZoneTest(Player player) {
        if (zoneController.isActive())
            return false;

        zoneController.startTest(player, 15.0, 3.0, 5, 15);
        return true;
    }

    private List<Player> onlineLobbyPlayers() {
        return lobbyManager.getLobbyPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }
}
