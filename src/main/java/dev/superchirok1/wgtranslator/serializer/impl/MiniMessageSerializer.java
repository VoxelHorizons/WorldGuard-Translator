package dev.superchirok1.wgtranslator.serializer.impl;

import dev.superchirok1.wgtranslator.serializer.TextSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessageSerializer implements TextSerializer {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    // WorldGuard's message hooks send strings. Preserve 24-bit colours when
    // serializing components back to legacy section-sign sequences.
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        // PlaceholderAPI can supply § codes while translation templates use & codes.
        // Neither form is valid MiniMessage input until converted to tags.
        Component component = MINI_MESSAGE.deserialize(LegacyMiniMessageConverter.normalize(message));
        return LEGACY.serialize(component);
    }
}
