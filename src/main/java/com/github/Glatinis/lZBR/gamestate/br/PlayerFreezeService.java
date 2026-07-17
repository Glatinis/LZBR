package com.github.Glatinis.lZBR.gamestate.br;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Locks frozen players in place (they can still look around) by snapping position back to where
// they were, without cancelling the move event outright — cancelling PlayerMoveEvent also blocks
// yaw/pitch updates and makes the client jitter.
public class PlayerFreezeService implements Listener {
    private final Set<UUID> frozen = new HashSet<>();

    public void freeze(Collection<? extends Player> players) {
        players.forEach(player -> frozen.add(player.getUniqueId()));
    }

    public void unfreeze(Collection<? extends Player> players) {
        players.forEach(player -> frozen.remove(player.getUniqueId()));
    }

    public void clear() {
        frozen.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!frozen.contains(event.getPlayer().getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        event.setTo(new Location(to.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
    }
}
