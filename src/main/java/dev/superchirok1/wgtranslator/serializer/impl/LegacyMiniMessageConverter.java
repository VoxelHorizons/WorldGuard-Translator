package dev.superchirok1.wgtranslator.serializer.impl;

/**
 * Normalizes Minecraft legacy colour codes into MiniMessage tags before parsing.
 *
 * MiniMessage rejects section-sign codes (including codes returned by PlaceholderAPI),
 * and leaving ampersand codes until after deserialization loses their position relative
 * to MiniMessage formatting. Translating both formats first keeps a single rendering
 * pass and prevents placeholder-supplied formatting from crashing deny events.
 */
final class LegacyMiniMessageConverter {
    private static final String[] COLOURS = {
            "black", "dark_blue", "dark_green", "dark_aqua",
            "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white"
    };

    private LegacyMiniMessageConverter() {
    }

    static String normalize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        StringBuilder result = new StringBuilder(message.length() + 32);

        for (int index = 0; index < message.length();) {
            char current = message.charAt(index);

            // Do not rewrite ampersands inside a MiniMessage tag's arguments
            // (e.g. <click:run_command:'/warp &f'>).
            if (current == '<') {
                int tagEnd = findTagEnd(message, index);
                if (tagEnd >= 0) {
                    result.append(message, index, tagEnd + 1);
                    index = tagEnd + 1;
                    continue;
                }
            }

            if (current == '&' || current == '§') {
                if (current == '&') {
                    int hexStart = -1;
                    int consumed = 0;
                    if (hasHexAt(message, index + 2) && message.charAt(index + 1) == '#') {
                        hexStart = index + 2;
                        consumed = 8; // &#RRGGBB
                    } else if (index + 10 <= message.length()
                            && message.startsWith("<#", index + 1)
                            && hasHexAt(message, index + 3)
                            && message.charAt(index + 9) == '>') {
                        hexStart = index + 3;
                        consumed = 10; // &<#RRGGBB>
                    }
                    if (hexStart >= 0) {
                        appendHexTag(result, message, hexStart);
                        index += consumed;
                        continue;
                    }
                }

                // Bukkit/Adventure RGB legacy format: §x§R§R§G§G§B§B
                // (also accept the ampersand form).
                if (index + 1 < message.length()
                        && Character.toLowerCase(message.charAt(index + 1)) == 'x') {
                    if (hasExpandedHexAt(message, index, current)) {
                        StringBuilder hex = new StringBuilder(6);
                        for (int n = 0; n < 6; n++) {
                            hex.append(message.charAt(index + 3 + (n * 2)));
                        }
                        result.append("<reset><#").append(hex).append('>');
                        index += 14;
                    } else {
                        // An incomplete RGB introducer cannot be represented by
                        // MiniMessage; treat it as a reset instead of throwing.
                        result.append("<reset>");
                        index += 2;
                    }
                    continue;
                }

                if (index + 1 < message.length()) {
                    char code = Character.toLowerCase(message.charAt(index + 1));
                    int colour = Character.digit(code, 16);
                    if (colour >= 0 && colour < 16) {
                        // A Minecraft colour code clears prior decorations.
                        result.append("<reset><").append(COLOURS[colour]).append('>');
                        index += 2;
                        continue;
                    }
                    String decoration = switch (code) {
                        case 'k' -> "obfuscated";
                        case 'l' -> "bold";
                        case 'm' -> "strikethrough";
                        case 'n' -> "underlined";
                        case 'o' -> "italic";
                        case 'r' -> "reset";
                        default -> null;
                    };
                    if (decoration != null) {
                        result.append('<').append(decoration).append('>');
                        index += 2;
                        continue;
                    }
                }
            }

            result.append(current);
            index++;
        }

        return result.toString();
    }

    private static void appendHexTag(StringBuilder result, String message, int hexStart) {
        result.append("<reset><#").append(message, hexStart, hexStart + 6).append('>');
    }

    private static boolean hasHexAt(String text, int start) {
        if (start < 0 || start + 6 > text.length()) return false;
        for (int i = start; i < start + 6; i++) {
            if (Character.digit(text.charAt(i), 16) < 0) return false;
        }
        return true;
    }

    private static boolean hasExpandedHexAt(String text, int start, char delimiter) {
        if (start + 14 > text.length()) return false;
        for (int i = 0; i < 6; i++) {
            if (text.charAt(start + 2 + (i * 2)) != delimiter
                    || Character.digit(text.charAt(start + 3 + (i * 2)), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static int findTagEnd(String text, int start) {
        if (start + 1 >= text.length()) return -1;
        char next = text.charAt(start + 1);
        if (!Character.isLetter(next) && next != '/' && next != '#' && next != '!') {
            return -1;
        }
        char quote = 0;
        boolean escaped = false;
        for (int i = start + 1; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (quote != 0) {
                if (ch == quote) quote = 0;
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            } else if (ch == '>') {
                return i;
            } else if (ch == '<') {
                return -1;
            }
        }
        return -1;
    }
}
