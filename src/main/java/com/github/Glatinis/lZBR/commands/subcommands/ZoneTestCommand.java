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
import org.bukkit.entity.Player;

// /lzbr zonetest [stop] — spins up (or stops) a small test zone around the player.
public class ZoneTestCommand implements SubCommand {
    private final GameStateController gameState;

    public ZoneTestCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("zonetest")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION)
                        && source.getSender() instanceof Player)
                .executes(this::executeStart)
                .then(Commands.literal("stop")
                        .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                        .executes(this::executeStop));
    }

    private int executeStart(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        if (gameState.startZoneTest(player)) {
            Messages.success(player, "Test zone created around you — it will start shrinking in 5 seconds.");
        } else {
            Messages.error(player, "A zone is already active. Stop it first with /lzbr zonetest stop.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeStop(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        gameState.stopZone();
        Messages.success(sender, "Zone stopped.");

        return Command.SINGLE_SUCCESS;
    }
}
