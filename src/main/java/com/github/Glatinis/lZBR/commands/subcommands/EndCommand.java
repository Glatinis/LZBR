package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class EndCommand implements SubCommand {
    private final GameStateController gameState;

    public EndCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("end")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (gameState.endGame()) {
            Messages.success(sender, "Match ended — resetting the arena for the next round...");
        } else {
            Messages.warn(sender, "There is no match in progress.");
        }

        return Command.SINGLE_SUCCESS;
    }
}
