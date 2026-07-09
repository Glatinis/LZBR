package com.github.Glatinis.lZBR.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface SubCommand {
    String ADMIN_PERMISSION = "lzbr.admin";

    LiteralArgumentBuilder<CommandSourceStack> node();
}
