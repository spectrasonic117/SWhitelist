package com.spectrasonic.Utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final String PREFIX = "<dark_gray>[<aqua>"
            + JavaPlugin.getProvidingPlugin(MessageUtils.class).getPluginMeta().getName()
            + "</aqua>]</dark_gray> <green>»</green> ";
    private static final String DIVIDER = "<gray>----------------------------------------</gray>";

    // Prefijos de alerta
    private static final String CLOSE_PREFIX = "</#9e9893>";
    private static final String SUCCESS_PREFIX = "<green><b>[✔]</b><green> <#9e9893>";
    private static final String ALERT_PREFIX = "<yellow><b>[!]</b><yellow> <#9e9893>";
    private static final String DENY_PREFIX = "<red><b>[✖]</b><red> <#9e9893>";

    private MessageUtils() {
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(PREFIX + message));
    }

    public static void RawMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(message));
    }

    public static void successMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(SUCCESS_PREFIX + message + CLOSE_PREFIX));
    }

    public static void alertMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(ALERT_PREFIX + message + CLOSE_PREFIX));
    }

    public static void denyMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(DENY_PREFIX + message + CLOSE_PREFIX));
    }

    public static void BroadcastMessage(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(miniMessage.deserialize(message)));
    }

    public static void sendStartupMessage(JavaPlugin plugin) {
        String[] messages = {
                DIVIDER,
                PREFIX + "<white>" + plugin.getPluginMeta().getName() + "</white> <green>Plugin Enabled!</green>",
                PREFIX + "<light_purple>Version: </light_purple>" + plugin.getPluginMeta().getVersion(),
                PREFIX + "<white>Developed by: </white><red>" + plugin.getPluginMeta().getAuthors() + "</red>",
                DIVIDER
        };

        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize(message));
        }
    }

    public static void sendShutdownMessage(JavaPlugin plugin) {
        String[] messages = {
                DIVIDER,
                PREFIX + "<red>" + plugin.getPluginMeta().getName() + " plugin Disabled!</red>",
                DIVIDER
        };

        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize(message));
        }
    }
}
