package com.github.Glatinis.lZBR.commands;

import com.github.Glatinis.lZBR.commands.subcommands.ArenaCommand;
import com.github.Glatinis.lZBR.commands.subcommands.EndCommand;
import com.github.Glatinis.lZBR.commands.subcommands.JoinCommand;
import com.github.Glatinis.lZBR.commands.subcommands.LeaveCommand;
import com.github.Glatinis.lZBR.commands.subcommands.LootCommand;
import com.github.Glatinis.lZBR.commands.subcommands.StartCommand;
import com.github.Glatinis.lZBR.commands.subcommands.TestCommand;
import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.loot.LootManager;
import com.github.Glatinis.lZBR.world.WorldController;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;

public class LZBRCommand {
    private final List<SubCommand> subCommands;

    public LZBRCommand(GameStateController gameStateController, WorldController worldController, LootManager lootManager) {
        this.subCommands = List.of(
                new StartCommand(gameStateController, worldController),
                new JoinCommand(gameStateController),
                new LeaveCommand(gameStateController),
                new EndCommand(gameStateController),
                new ArenaCommand(gameStateController),
                new LootCommand(lootManager),
                new TestCommand(gameStateController)
        );
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("lzbr");
        subCommands.forEach(sub -> root.then(sub.node()));
        return root.build();
    }
}
