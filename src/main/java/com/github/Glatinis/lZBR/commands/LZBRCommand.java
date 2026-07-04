package com.github.Glatinis.lZBR.commands;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.github.Glatinis.lZBR.returncode.StartCode;
import com.github.Glatinis.lZBR.world.WorldController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LZBRCommand {
    private GameStateController gameStateController;
    private WorldController worldController;

    public LZBRCommand(GameStateController gameStateController, WorldController worldController) {
        this.gameStateController = gameStateController;
        this.worldController = worldController;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("lzbr")
                .then(Commands.literal("start")
                    .requires(source -> source.getSender().hasPermission("lzbr.admin"))
                    .executes(this::executeStart))
                .then(Commands.literal("join"))
                    .requires(source -> source.getSender() instanceof Player)
                    .executes(this::executeJoin)
                .build();
    }

    private int executeStart(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        StartCode started = gameStateController.startGame();

        if (started.equals(StartCode.GAME_IN_PROGRESS)) {
            sender.sendMessage(Component.text("The Battle Royale is already in progress.")
                    .color(NamedTextColor.RED));
        }
        else if (started.equals(StartCode.PLAYER_COUNT_INSUFFICIENT)) {
            sender.sendMessage(Component.text("There are not enough players to start the match.")
                    .color(NamedTextColor.RED));
        }
        else if (!worldController.isMultiverseAvailable()) {
            sender.sendMessage(Component.text("Multiverse-Core is not loaded... Cannot start the game without it.")
                    .color(NamedTextColor.RED));
        }
        else if (started.equals(StartCode.SUCCESS)) {
            sender.sendMessage(Component.text("Battle Royale starting...").color(NamedTextColor.GREEN));
            gameStateController.startGame();
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeJoin(CommandContext<CommandSourceStack> ctx) {
        Player plr = (Player) ctx.getSource().getSender();

        JoinCode returnCode = gameStateController.joinLobby(plr);

        if (returnCode.equals(JoinCode.ALREADY_IN_LOBBY)) {
            plr.sendMessage(Component.text("You are already in the lobby.")
                    .color(NamedTextColor.YELLOW));
        }
        else if (returnCode.equals(JoinCode.GAME_STARTED)) {
            plr.sendMessage(Component.text("The game has already started.")
                    .color(NamedTextColor.RED));
        }
        else if (returnCode.equals(JoinCode.LOBBY_FULL)) {
            plr.sendMessage(Component.text("The lobby is full.")
                    .color(NamedTextColor.RED));
        }
        else if (returnCode.equals(JoinCode.SUCCESS)) {
            plr.sendMessage(Component.text("Joining lobby...")
                    .color(NamedTextColor.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }
}