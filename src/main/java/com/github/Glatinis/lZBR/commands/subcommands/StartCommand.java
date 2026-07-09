package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.world.WorldController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class StartCommand implements SubCommand {
    private final GameStateController gameState;
    private final WorldController worldController;

    public StartCommand(GameStateController gameState, WorldController worldController) {
        this.gameState = gameState;
        this.worldController = worldController;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("start")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!worldController.isMultiverseAvailable()) {
            Messages.error(sender, "Multiverse-Core is not loaded... Cannot start the game without it.");
            return Command.SINGLE_SUCCESS;
        }

        switch (gameState.startGame()) {
            case GAME_IN_PROGRESS -> Messages.error(sender, "The Battle Royale is already in progress.");
            case PLAYER_COUNT_INSUFFICIENT -> Messages.error(sender, "There are not enough players to start the match.");
            case SUCCESS -> Messages.success(sender, "Battle Royale starting...");
        }

        return Command.SINGLE_SUCCESS;
    }
}
