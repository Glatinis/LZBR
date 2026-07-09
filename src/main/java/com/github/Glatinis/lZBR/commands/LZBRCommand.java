package com.github.Glatinis.lZBR.commands;

import com.github.Glatinis.lZBR.commands.subcommands.ArenaCommand;
import com.github.Glatinis.lZBR.commands.subcommands.EndCommand;
import com.github.Glatinis.lZBR.commands.subcommands.JoinCommand;
import com.github.Glatinis.lZBR.commands.subcommands.LeaveCommand;
import com.github.Glatinis.lZBR.commands.subcommands.StartCommand;
import com.github.Glatinis.lZBR.commands.subcommands.ZoneTestCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.world.WorldController;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;

// Root /lzbr command. Owns nothing but the list of sub-commands and grafts each one onto the root.
public class LZBRCommand {
    private final List<SubCommand> subCommands;

    public LZBRCommand(GameStateController gameStateController, WorldController worldController) {
        this.subCommands = List.of(
                new StartCommand(gameStateController, worldController),
                new JoinCommand(gameStateController),
                new LeaveCommand(gameStateController),
                new EndCommand(gameStateController),
                new ArenaCommand(gameStateController),
                new ZoneTestCommand(gameStateController)
        );
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("lzbr");
        subCommands.forEach(sub -> root.then(sub.node()));
        return root.build();
    }
}
