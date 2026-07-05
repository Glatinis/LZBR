package com.github.Glatinis.lZBR.gamestate.br;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BRManager {
    private List<Player> players = new ArrayList<>();

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

    public void reset() {
        players.clear();
    }
}
