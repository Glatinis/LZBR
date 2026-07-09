package com.github.Glatinis.lZBR.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

// A single /lzbr sub-command. Each implementation builds its own branch of the command tree, which
// LZBRCommand grafts onto the root literal.
public interface SubCommand {
    String ADMIN_PERMISSION = "lzbr.admin";

    LiteralArgumentBuilder<CommandSourceStack> node();
}
