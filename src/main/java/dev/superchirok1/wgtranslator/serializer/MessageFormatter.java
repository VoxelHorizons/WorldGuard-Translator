package dev.superchirok1.wgtranslator.serializer;

import dev.superchirok1.wgtranslator.config.Configuration;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Player-aware message rendering with a completely optional PlaceholderAPI integration.
 * Only load the nested PlaceholderApiBridge when PlaceholderAPI is enabled.
 */
public final class MessageFormatter {
    private static volatile boolean warned;

    private MessageFormatter() {
    }

    public static String format(CommandSender recipient, String message) {
        return MessagePipeline.render(message, text -> replacePlaceholders(recipient, text), Text.get);
    }

    private static String replacePlaceholders(CommandSender recipient, String message) {
        if (!Configuration.placeholderApiEnabled || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return message;
        }
        try {
            return PlaceholderApiBridge.parse(recipient instanceof Player ? (Player) recipient : null, message);
        } catch (LinkageError | RuntimeException e) {
            if (!warned) {
                synchronized (MessageFormatter.class) {
                    if (!warned) {
                        warned = true;
                        Bukkit.getLogger().warning("[WorldGuard-Translator] PlaceholderAPI expansion failed; " +
                                "displaying untranslated placeholder text: " + e);
                    }
                }
            }
            return message;
        }
    }

    private static final class PlaceholderApiBridge {
        private static String parse(OfflinePlayer player, String message) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
    }
}
