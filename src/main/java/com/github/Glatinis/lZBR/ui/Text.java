package com.github.Glatinis.lZBR.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

public final class Text {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Text() {}

    public static Component mini(String template) {
        return mini(template, Map.of());
    }

    public static Component mini(String template, Map<String, String> placeholders) {
        if (template == null || template.isBlank()) return Component.empty();

        String resolved = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return MINI_MESSAGE.deserialize(resolved);
    }
}
