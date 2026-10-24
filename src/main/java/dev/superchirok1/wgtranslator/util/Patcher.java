package dev.superchirok1.wgtranslator.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.listener.RegionProtectionListener;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StringFlag;
import dev.superchirok1.wgtranslator.config.Configuration;
import dev.superchirok1.wgtranslator.serializer.MessageFormatter;
import dev.superchirok1.wgtranslator.translation.TranslationManager;
import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

@UtilityClass
public class Patcher {

    static {
        ByteBuddyAgent.install();
    }

    public void denyMessage(String message) {
        processStringFlag(Flags.DENY_MESSAGE, message);
    }

    public void entryDenyMessage(String message) {
        processStringFlag(Flags.ENTRY_DENY_MESSAGE, message);
    }

    public void exitDenyMessage(String message) {
        processStringFlag(Flags.EXIT_DENY_MESSAGE, message);
    }

    public void patchListenerMethod() {
        new ByteBuddy()
                .redefine(RegionProtectionListener.class)
                .method(ElementMatchers.named("formatAndSendDenyMessage"))
                .intercept(MethodDelegation.to(Class.class))
                .make()
                .load(RegionProtectionListener.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        // WorldGuard's movement flag handlers bypass RegionProtectionListener.
        // Substitute their final print calls without changing flag checks, throttling,
        // or region behaviour. Any unavailable version-specific hook is logged.
        patchMessageCall("com.sk89q.worldguard.session.handler.EntryFlag",
                "onCrossBoundary", "printRaw", "sendTranslatedLocalPlayerMessage", LocalPlayer.class);
        patchMessageCall("com.sk89q.worldguard.session.handler.ExitFlag",
                "sendMessage", "printRaw", "sendTranslatedLocalPlayerMessage", LocalPlayer.class);
        // Entry/exit denial messages on enderpearl and chorus-fruit teleport paths.
        patchMessageCall("com.sk89q.worldguard.bukkit.listener.WorldGuardPlayerListener",
                "onPlayerTeleport", "sendMessage", "sendTranslatedBukkitPlayerMessage", Player.class);
    }

    /** Preserve WorldGuard movement logic while rendering the final text per player. */
    public static void sendTranslatedLocalPlayerMessage(LocalPlayer player, String message) {
        player.printRaw(MessageFormatter.format(BukkitAdapter.adapt(player), message));
    }

    /** WorldGuard's direct Bukkit teleport denial message path. */
    public static void sendTranslatedBukkitPlayerMessage(Player player, String message) {
        player.sendMessage(MessageFormatter.format(player, message));
    }

    private static void patchMessageCall(String className, String inMethod,
                                         String callName, String replacementName, java.lang.Class<?> receiverType) {
        try {
            java.lang.Class<?> target = java.lang.Class.forName(className);
            new ByteBuddy().redefine(target)
                    .visit(MemberSubstitution.relaxed()
                            .method(ElementMatchers.named(callName).and(ElementMatchers.takesArguments(String.class)))
                            .replaceWith(Patcher.class.getMethod(replacementName, receiverType, String.class))
                            .on(ElementMatchers.named(inMethod)))
                    .make().load(target.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        } catch (Exception | LinkageError exception) {
            Logger.warn("Unable to install WorldGuard message hook for " + className + "." + inMethod
                    + ": " + exception.getMessage());
        }
    }

    public class Class {
        @RuntimeType
        public static void formatAndSendDenyMessage(@AllArguments Object[] args) {
            String what = (String) args[0];
            LocalPlayer localPlayer = (LocalPlayer) args[1];
            String message = (String) args[2];

            if (message == null || message.isEmpty()) return;
            message = WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(localPlayer, message);
            what = TranslationManager.getTranslation().components.get(what);
            message = message.replace("%what%", what);
            Player player = BukkitAdapter.adapt(localPlayer);
            message = MessageFormatter.format(player, message);
            if (Configuration.displayDenyMessage == Configuration.DisplayMode.DEFAULT) {
                localPlayer.printRaw(message);
            } else {
                player.sendActionBar(message);
            }
        }
    }

    private void processStringFlag(StringFlag deny, String message) {
        try {
            Field field = StringFlag.class.getDeclaredField("defaultValue");
            field.setAccessible(true);

            field.set(deny, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
