package com.github.Glatinis.lZBR.commands;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.returncode.StartCode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class LZBRCommand {
    private GameStateController gameStateController;

    public LZBRCommand(GameStateController gameStateController) {
        this.gameStateController = gameStateController;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("lzbr")
                .requires(source -> source.getSender().hasPermission("lzbr.admin"))
                .then(Commands.literal("start")
                        .executes(this::executeStart))
                .build();
    }

    private int executeStart(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        StartCode started = gameStateController.startMatch();

        if (!started.equals(StartCode.SUCCESS)) {
            sender.sendMessage(Component.text("Could not start match — check player count or match state.")
                    .color(NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.text("Match starting...").color(NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }
}