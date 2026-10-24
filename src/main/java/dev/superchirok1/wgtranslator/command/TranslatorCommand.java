package dev.superchirok1.wgtranslator.command;

import dev.superchirok1.wgtranslator.config.Configuration;
import dev.superchirok1.wgtranslator.WorldGuardTranslator;
import dev.superchirok1.wgtranslator.serializer.Text;
import dev.superchirok1.wgtranslator.serializer.MessageFormatter;
import dev.superchirok1.wgtranslator.serializer.impl.LegacyAmpersandSerializer;
import dev.superchirok1.wgtranslator.translation.Translation;
import dev.superchirok1.wgtranslator.translation.TranslationManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TranslatorCommand implements CommandExecutor, TabCompleter {

    private final WorldGuardTranslator plugin;
    private final LegacyAmpersandSerializer text = Text.getLegacyAmpersandSerializer();

    private static final List<String> TAB_1 = Arrays.asList("reload", "restart", "component", "help", "denyMessage", "dm");
    private static final List<String> TAB_COMPONENT = Arrays.asList("add", "set", "remove", "list");
    private static final List<String> TAB_DM = Arrays.asList("set", "setTemp");

    public TranslatorCommand(WorldGuardTranslator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean permission = sender.hasPermission(Configuration.permissionAdmin);
        if (!permission || args.length == 0) {
            return defaultMessage(sender, permission, label);
        }

        Translation.Messages messages = TranslationManager.getTranslation().messages;

        switch (args[0].toLowerCase()) {
            case "reload", "restart", "r": {
                plugin.reload(sender.getName(), (time) -> {
                    send(sender, String.format(messages.reloaded(), time));
                });
                return true;
            }

            case "denymessage", "dm": {
                if (args.length < 3) {
                    send(sender, messages.help().replace("%cmd%", label));
                    return true;
                }

                String msg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                switch (args[1].toLowerCase()) {
                    case "set" -> {
                        TranslationManager.setDenyMessage(msg);
                        send(sender, messages.denyMessageInstalled());
                    }
                    case "settemp" -> {
                        TranslationManager.setMemoryDenyMessage(msg);
                        send(sender, messages.denyMessageInstalledTemp());
                    }
                    default -> send(sender, messages.help().replace("%cmd%", label));
                }
                return true;
            }

            case "component": {
                if (args.length < 2) {
                    send(sender, messages.help().replace("%cmd%", label));
                    return true;
                }

                Translation.Components components = TranslationManager.getTranslation().components;

                switch (args[1].toLowerCase()) {
                    case "add", "set" -> {
                        if (args.length < 3) {
                            send(sender, messages.componentUsage());
                            return true;
                        }

                        String msg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        if (msg.indexOf(';') == -1) {
                            send(sender, messages.componentUsage());
                            return true;
                        }

                        String[] strings = msg.split(";", 2);
                        if (strings.length < 2) {
                            send(sender, messages.componentUsage());
                            return true;
                        }

                        String key = strings[0].trim();
                        String value = strings[1].trim();

                        components.set(key, value);
                        send(sender, String.format(messages.componentAdded(), key, value));
                        return true;
                    }

                    case "remove" -> {
                        if (args.length < 3) {
                            send(sender, messages.help().replace("%cmd%", label));
                            return true;
                        }

                        String key = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        if (!components.getMap().containsKey(key)) {
                            send(sender, String.format(messages.componentRemoveIsNotContains(), key));
                            return true;
                        }

                        String value = components.get(key);
                        components.remove(key);
                        send(sender, String.format(messages.componentRemoveRemoved(), key, value));
                        return true;
                    }

                    case "list" -> {
                        send(sender, text.colorize(messages.componentListHeader()));
                        for (Map.Entry<String, String> map : components.getMap().entrySet()) {
                            send(sender, 
                                    messages.componentListFormat()
                                            .replace("{0}", map.getKey())
                                            .replace("{1}", map.getValue())
                            );
                        }
                        return true;
                    }

                    default -> {
                        send(sender, messages.help().replace("%cmd%", label));
                        return true;
                    }
                }
            }

            case "help":
                send(sender, messages.help().replace("%cmd%", label));
                return true;

            default:
                return defaultMessage(sender, true, label);
        }
    }


    private boolean defaultMessage(CommandSender sender, boolean permission, String command) {
        send(sender, " ");
        send(sender, text.colorize("  &3&lWorldGuard-Translator &8• &7v" + plugin.getDescription().getVersion()));
        send(sender, text.colorize("  &8• &fContributors: &b" + String.join(", ", plugin.getDescription().getAuthors())));
        send(sender, text.colorize("  &8• &fTranslators: &b" + String.join(", ", plugin.getTranslators())));
        TextComponent component = new TextComponent(text.colorize("  &8• &fRepository: "));
        TextComponent link = new TextComponent(text.colorize("&b&nGitHub (Click for open)&r"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/SuperCHIROK1/WorldGuard-Translator"));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(text.colorize("&3» &bhttps://github.com/SuperCHIROK1/WorldGuard-Translator"))}));
        component.addExtra(link);
        sender.spigot().sendMessage(component);
        send(sender, " ");
        if (permission) {
            send(sender, text.colorize("  &8• &fHelp: &e/" + command + " help"));
            send(sender, " ");
        }
        return true;
    }

    /** Render every configurable command response with the recipient's placeholders and colours. */
    private void send(CommandSender sender, String message) {
        sender.sendMessage(MessageFormatter.format(sender, message));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(Configuration.permissionAdmin)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return tabFilter(TAB_1, args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "component" -> tabFilter(TAB_COMPONENT, args[1]);
                case "denymessage", "dm" -> tabFilter(TAB_DM, args[1]);
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    private List<String> tabFilter(List<String> list, String arg) {
        return StringUtil.copyPartialMatches(arg, list, new ArrayList<>());
    }
}