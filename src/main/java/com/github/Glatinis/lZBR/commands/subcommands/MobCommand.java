package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.mob.MobManager;
import com.github.Glatinis.lZBR.mob.spawn.SpawnPoint;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

// /lzbr mobs — manage arena mob spawning: add/remove/list spawn points and reload mobs.yml.
// Spawning a mob for a test is handled by /lzbr test mob.
public class MobCommand implements SubCommand {
    private static final double REMOVE_RANGE = 4.0;

    private final MobManager mobManager;

    public MobCommand(MobManager mobManager) {
        this.mobManager = mobManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("mobs")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .then(Commands.literal("spawn")
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

        mobManager.addSpawnPoint(player);
        Messages.success(player, "Mob spawn point added at your position and saved.");

        return Command.SINGLE_SUCCESS;
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();

        SpawnPoint removed = mobManager.removeNearestSpawnPoint(player, REMOVE_RANGE);
        if (removed != null) {
            Messages.success(player, "Removed the mob spawn point at "
                    + removed.x() + ", " + removed.y() + ", " + removed.z() + ".");
        } else {
            Messages.error(player, "No mob spawn point within " + (int) REMOVE_RANGE + " blocks of you.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeList(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        List<SpawnPoint> points = mobManager.getSpawnPoints();
        if (points.isEmpty()) {
            Messages.info(sender, "No mob spawn points configured. Stand somewhere and run /lzbr mobs spawn add.");
            return Command.SINGLE_SUCCESS;
        }

        Messages.info(sender, "Mob spawn points (" + points.size() + "):");
        for (SpawnPoint point : points) {
            Messages.info(sender, " - " + point.world() + " " + point.x() + ", " + point.y() + ", " + point.z());
        }

        return Command.SINGLE_SUCCESS;
    }

    private int executeReload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        mobManager.reload();
        Messages.success(sender, "Reloaded mobs.yml — "
                + mobManager.getMobTypeCount() + " mob types, "
                + mobManager.getSpawnPoints().size() + " spawn points.");

        return Command.SINGLE_SUCCESS;
    }
}
