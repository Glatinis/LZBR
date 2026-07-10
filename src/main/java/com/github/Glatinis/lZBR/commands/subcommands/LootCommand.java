package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.loot.ChestLocation;
import com.github.Glatinis.lZBR.loot.LootManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

// /lzbr loot — manage the chest-loot configuration: add/remove/list chest positions and reload
// loot.yml. Actually filling chests for a test is handled by /lzbr test loot.
public class LootCommand implements SubCommand {
    private final LootManager lootManager;

    public LootCommand(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("loot")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("chest")
                        .then(Commands.literal("add")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(this::executeAdd))
                        .then(Commands.literal("remove")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(this::executeRemove))
                        .then(Commands.literal("list")
                                .executes(this::executeList)))
                .then(Commands.literal("reload")
                        .executes(this::executeReload));
    }

    private int executeAdd(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        switch (lootManager.addTargetedChest(player)) {
            case ADDED -> Messages.success(player, "Loot chest added and saved.");
            case ALREADY_ADDED -> Messages.warn(player, "That chest is already a loot chest.");
            case NOT_A_CHEST -> Messages.error(player, "Look at a chest within range to add it as a loot chest.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        if (lootManager.removeTargetedChest(player)) {
            Messages.success(player, "Loot chest removed.");
        } else {
            Messages.error(player, "You are not looking at a registered loot chest.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeList(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        List<ChestLocation> chests = lootManager.getChests();
        if (chests.isEmpty()) {
            Messages.info(sender, "No loot chests configured. Look at a chest and run /lzbr loot chest add.");
            return Command.SINGLE_SUCCESS;
        }

        Messages.info(sender, "Loot chests (" + chests.size() + "):");
        for (ChestLocation chest : chests) {
            Messages.info(sender, " - " + LootManager.describe(chest));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeReload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        lootManager.reload();
        Messages.success(sender, "Reloaded loot.yml — "
                + lootManager.getLootItemCount() + " loot items, "
                + lootManager.getChests().size() + " chests.");

        return Command.SINGLE_SUCCESS;
    }
}
