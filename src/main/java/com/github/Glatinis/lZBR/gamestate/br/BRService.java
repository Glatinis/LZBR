package com.github.Glatinis.lZBR.gamestate.br;

import com.github.Glatinis.lZBR.world.WorldController;
import org.bukkit.entity.Player;

import java.util.List;

public class BRService {
    private WorldController worldController;

    public BRService(WorldController worldController) {
        this.worldController = worldController;
    }

    public void startPreGame(List<Player> players) {
        players.forEach(plr -> { worldController.teleportToBR(plr);});
    }
}
