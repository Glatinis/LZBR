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

// /lzbr arena reset — manually re-pastes the arena schematic (admin/testing).
public class ArenaCommand implements SubCommand {
    private final GameStateController gameState;

    public ArenaCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("arena")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("reset")
                        .executes(this::executeReset));
    }

    private int executeReset(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Messages.info(sender, "Resetting the arena...");
        gameState.resetArena(success -> {
            if (success) {
                Messages.success(sender, "Arena reset complete.");
            } else {
                Messages.error(sender, "Arena reset failed — check the console "
                        + "(FastAsyncWorldEdit installed? schematic present? reset enabled?).");
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
