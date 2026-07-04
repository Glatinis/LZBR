package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.world.WorldController;
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
        players = participatingPlayers.stream().toList();
        brService.startPreGame(players);
    }
}
