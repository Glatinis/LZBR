package com.github.Glatinis.lZBR.commands.subcommands;

import com.github.Glatinis.lZBR.commands.Messages;
import com.github.Glatinis.lZBR.commands.SubCommand;
import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.github.Glatinis.lZBR.loot.LootManager;
import com.github.Glatinis.lZBR.mob.MobManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

// /lzbr reload — reloads every LZBR config at once (config.yml, loot.yml, mobs.yml). The per-feature
// reloads (/lzbr loot reload, /lzbr mobs reload) still exist for reloading just one file.
public class ReloadCommand implements SubCommand {
    private final ConfigRepository config;
    private final LootManager lootManager;
    private final MobManager mobManager;

    public ReloadCommand(ConfigRepository config, LootManager lootManager, MobManager mobManager) {
        this.config = config;
        this.lootManager = lootManager;
        this.mobManager = mobManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission(ADMIN_PERMISSION))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        config.reload();
        lootManager.reload();
        mobManager.reload();

        Messages.success(sender, "Reloaded config.yml, loot.yml and mobs.yml — "
                + lootManager.getChests().size() + " loot chests, "
                + mobManager.getSpawnPoints().size() + " mob spawn points.");

        return Command.SINGLE_SUCCESS;
    }
}
