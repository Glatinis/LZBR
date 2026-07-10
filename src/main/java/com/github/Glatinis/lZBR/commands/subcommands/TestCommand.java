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

// /lzbr test — admin tools for trying things out without running a full match:
//   /lzbr test zone [stop]   spin up a shrinking test zone around you
//   /lzbr test loot          fill the configured loot chests right now
public class TestCommand implements SubCommand {
    private final GameStateController gameState;

    public TestCommand(GameStateController gameState) {
        this.gameState = gameState;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("test")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("zone")
                        .requires(source -> source.getSender() instanceof Player)
                        .executes(this::executeZoneStart)
                        .then(Commands.literal("stop")
                                .executes(this::executeZoneStop)))
                .then(Commands.literal("loot")
                        .executes(this::executeLoot));
    }

    private int executeZoneStart(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        if (gameState.startZoneTest(player)) {
            Messages.success(player, "Test zone created around you — it will start shrinking in 5 seconds.");
        } else {
            Messages.error(player, "A zone is already active. Stop it first with /lzbr test zone stop.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeZoneStop(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        gameState.stopZone();
        Messages.success(sender, "Zone stopped.");

        return Command.SINGLE_SUCCESS;
    }

    private int executeLoot(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        int filled = gameState.fillLootChests();
        if (filled > 0) {
            Messages.success(sender, "Filled " + filled + " loot chest(s) with fresh loot.");
        } else {
            Messages.warn(sender, "No loot chests were filled — none are configured, or their world isn't loaded.");
        }

        return Command.SINGLE_SUCCESS;
    }
}
