package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

// /lzbr leave — removes the player from the lobby queue.
public class LeaveCommand implements SubCommand {
    private final GameStateController gameState;

    public LeaveCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("leave")
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        switch (gameState.leaveLobby(player)) {
            case NOT_IN_LOBBY -> Messages.warn(player, "You are not in the lobby.");
            case GAME_STARTED -> Messages.error(player, "You cannot leave the queue once the game has started.");
            case SUCCESS -> Messages.success(player, "You have left the lobby queue.");
        }

        return Command.SINGLE_SUCCESS;
    }
}
