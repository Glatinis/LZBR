package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand {
    private final GameStateController gameState;

    public JoinCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("join")
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        switch (gameState.joinLobby(player)) {
            case ALREADY_IN_LOBBY -> Messages.warn(player, "You are already in the lobby.");
            case GAME_STARTED -> Messages.error(player, "The game has already started.");
            case LOBBY_FULL -> Messages.error(player, "The lobby is full.");
            case SUCCESS -> Messages.success(player, "Joining lobby...");
        }

        return Command.SINGLE_SUCCESS;
    }
}
