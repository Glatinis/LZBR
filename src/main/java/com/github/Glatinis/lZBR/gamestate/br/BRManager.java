package com.github.Glatinis.lZBR.gamestate.br;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// Owns the match roster (alive players / eliminated spectators); BRService handles the actual
// teleport and state changes.
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

    // Returns the arena world, or null if it couldn't be resolved and nobody was moved.
    public World sendToArena() {
        return brService.sendToArena(alivePlayers);
    }

    public List<Player> getPlayers() {
        return alivePlayers;
    }

    public List<Player> getAllParticipants() {
        List<Player> everyone = new ArrayList<>(alivePlayers);
        spectators.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(everyone::add);
        return everyone;
    }

    // Unlike eliminatePlayer, does not add them to spectators (e.g. they quit rather than died).
    public void removePlayer(Player player) {
        alivePlayers.remove(player);
        spectators.remove(player.getUniqueId());
    }

    public void eliminatePlayer(Player player) {
        alivePlayers.remove(player);
        spectators.add(player.getUniqueId());
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public void returnAllToLobby() {
        brService.returnToLobby(getAllParticipants());
    }

    public void reset() {
        alivePlayers.clear();
        spectators.clear();
    }
}
