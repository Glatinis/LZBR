package com.github.Glatinis.lZBR.gamestate.br;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// Owns the match roster: who is still alive and who has been eliminated (spectators). Delegates the
// actual teleport/state changes to BRService.
public class BRManager {
    private final BRService brService;

    private List<Player> alivePlayers = new ArrayList<>();
    private final Set<UUID> spectators = new HashSet<>();

    public BRManager(BRService brService) {
        this.brService = brService;
    }

    // Sets the roster for a fresh match. Players stay where they are (the lobby) until sendToArena().
    public void prepareMatch(List<Player> participants) {
        alivePlayers = new ArrayList<>(participants);
        spectators.clear();
    }

    // Teleports the current roster into the arena — called once the lobby countdown finishes.
    public void sendToArena() {
        brService.sendToArena(alivePlayers);
    }

    public List<Player> getPlayers() {
        return alivePlayers;
    }

    // Alive players plus any still-online spectators — everyone involved in the match.
    public List<Player> getAllParticipants() {
        List<Player> everyone = new ArrayList<>(alivePlayers);
        spectators.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(everyone::add);
        return everyone;
    }

    // Removes a player from the match entirely (e.g. they quit) without making them a spectator.
    public void removePlayer(Player player) {
        alivePlayers.remove(player);
        spectators.remove(player.getUniqueId());
    }

    // Marks a player as eliminated: out of the running, now a spectator.
    public void eliminatePlayer(Player player) {
        alivePlayers.remove(player);
        spectators.add(player.getUniqueId());
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    // Sends every remaining participant back to the lobby with a clean state.
    public void returnAllToLobby() {
        brService.returnToLobby(getAllParticipants());
    }

    public void reset() {
        alivePlayers.clear();
        spectators.clear();
    }
}
