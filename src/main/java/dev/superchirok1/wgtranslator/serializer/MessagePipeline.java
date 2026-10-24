package dev.superchirok1.wgtranslator.serializer;

import dev.superchirok1.wgtranslator.serializer.impl.LegacyAmpersandSerializer;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Formats a complete message after the caller has applied its own macros.
 * Placeholder expansion precedes color conversion, including color codes returned by expansions.
 */
public final class MessagePipeline {
    private static final LegacyAmpersandSerializer LEGACY = new LegacyAmpersandSerializer();

    private MessagePipeline() {
    }

    public static String render(String message, UnaryOperator<String> placeholderResolver, TextSerializer serializer) {
        if (message == null || message.isEmpty()) return message;
        Objects.requireNonNull(placeholderResolver, "placeholderResolver");
        Objects.requireNonNull(serializer, "serializer");
        String expanded = placeholderResolver.apply(message);
        if (expanded == null) expanded = message;
        // The selected serializer can handle MiniMessage markup, while the final legacy
        // pass also handles &f, &c, &l, &#RRGGBB and &<#RRGGBB> in any inserted component.
        return LEGACY.colorize(serializer.colorize(expanded));
    }
}
