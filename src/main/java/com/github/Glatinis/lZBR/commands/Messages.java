package com.github.Glatinis.lZBR.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

// Small helpers for colour-coded command feedback, so sub-commands read as one line each.
public final class Messages {
    private Messages() {}

    public static void success(CommandSender to, String text) {
        to.sendMessage(Component.text(text, NamedTextColor.GREEN));
    }

    public static void error(CommandSender to, String text) {
        to.sendMessage(Component.text(text, NamedTextColor.RED));
    }

    public static void warn(CommandSender to, String text) {
        to.sendMessage(Component.text(text, NamedTextColor.YELLOW));
    }

    public static void info(CommandSender to, String text) {
        to.sendMessage(Component.text(text, NamedTextColor.GRAY));
    }
}
