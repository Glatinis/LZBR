package com.github.Glatinis.lZBR.gamestate.br;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BRManager {
    private List<Player> players = new ArrayList<>();
    private Set<UUID> spectators = new HashSet<>();

    private BRService brService;

    public BRManager(BRService brService) {
        this.brService = brService;
    }

    public void startPreGame(List<Player> participatingPlayers) {
        players = new ArrayList<>(participatingPlayers);
        brService.startPreGame(players);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void eliminatePlayer(Player player) {
        players.remove(player);
        spectators.add(player.getUniqueId());
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public Set<UUID> getSpectators() {
        return new HashSet<>(spectators);
    }

    public void reset() {
        players.clear();
        spectators.clear();
    }
}
