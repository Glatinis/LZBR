package com.github.Glatinis.lZBR.commands;

import com.github.Glatinis.lZBR.gamestate.GameStateController;
import com.github.Glatinis.lZBR.returncode.JoinCode;
import com.github.Glatinis.lZBR.returncode.LeaveCode;
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
                .then(Commands.literal("join")
                        .requires(source -> source.getSender() instanceof Player)
                        .executes(this::executeJoin))
                .then(Commands.literal("leave")
                        .requires(source -> source.getSender() instanceof Player)
                        .executes(this::executeLeave))
                .then(Commands.literal("end")
                        .requires(source -> source.getSender().hasPermission("lzbr.admin"))
                        .executes(this::executeEnd))
                .then(Commands.literal("arena")
                        .requires(source -> source.getSender().hasPermission("lzbr.admin"))
                        .then(Commands.literal("reset")
                                .executes(this::executeArenaReset)))
                .then(Commands.literal("zonetest")
                        .requires(source -> source.getSender().hasPermission("lzbr.admin")
                                && source.getSender() instanceof Player)
                        .executes(this::executeZoneTestStart)
                        .then(Commands.literal("stop")
                                .requires(source -> source.getSender().hasPermission("lzbr.admin"))
                                .executes(this::executeZoneTestStop)))
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

    private int executeLeave(CommandContext<CommandSourceStack> ctx) {
        Player plr = (Player) ctx.getSource().getSender();

        LeaveCode returnCode = gameStateController.leaveLobby(plr);

        if (returnCode.equals(LeaveCode.NOT_IN_LOBBY)) {
            plr.sendMessage(Component.text("You are not in the lobby.")
                    .color(NamedTextColor.YELLOW));
        } else if (returnCode.equals(LeaveCode.GAME_STARTED)) {
            plr.sendMessage(Component.text("You cannot leave the queue once the game has started.")
                    .color(NamedTextColor.RED));
        } else if (returnCode.equals(LeaveCode.SUCCESS)) {
            plr.sendMessage(Component.text("You have left the lobby queue.")
                    .color(NamedTextColor.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeEnd(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        boolean ended = gameStateController.endGame();

        if (ended) {
            sender.sendMessage(Component.text("Match ended — resetting the arena for the next round...")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("There is no match in progress.")
                    .color(NamedTextColor.YELLOW));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeArenaReset(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage(Component.text("Resetting the arena...").color(NamedTextColor.GRAY));
        gameStateController.resetArena(success -> {
            if (success) {
                sender.sendMessage(Component.text("Arena reset complete.").color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Arena reset failed — check the console "
                        + "(FastAsyncWorldEdit installed? schematic present? reset enabled?).")
                        .color(NamedTextColor.RED));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private int executeZoneTestStart(CommandContext<CommandSourceStack> ctx) {
        Player plr = (Player) ctx.getSource().getSender();

        boolean started = gameStateController.startZoneTest(plr);

        if (started) {
            plr.sendMessage(Component.text("Test zone created around you — it will start shrinking in 5 seconds.")
                    .color(NamedTextColor.GREEN));
        } else {
            plr.sendMessage(Component.text("A zone is already active. Stop it first with /lzbr zonetest stop.")
                    .color(NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeZoneTestStop(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        gameStateController.stopZone();
        sender.sendMessage(Component.text("Zone stopped.").color(NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}