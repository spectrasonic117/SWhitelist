package com.spectrasonic.SWhitelist.discord;

import com.spectrasonic.SWhitelist.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public final class EmbedUtils {

    private EmbedUtils() {
    }

    public static MessageEmbed createAddEmbed(String player, String actor, Main plugin) {
        String title = "✅ Jugador agregado a la Whitelist";
        String actorLine = "Agregado por " + actor;
        String playerLabel = "Player";
        String actorLabel = "By";
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-success"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(playerLabel, player, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createRemoveEmbed(String player, String actor, Main plugin) {
        String title = "🗑️ Jugador eliminado de la Whitelist";
        String actorLine = "Removido por " + actor;
        String playerLabel = "Player";
        String actorLabel = "Por";
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-warning"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(playerLabel, player, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createListEmbed(List<String> players, int page, int totalPages, Main plugin) {
        String title = "📋 Jugadores en la Whitelist";
        String countLabel = "Jugadores Totales";
        String pageLabel = "Página";
        String noPlayers = "No hay jugadores en la whitelist.";
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-info"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now());

        if (players.isEmpty()) {
            embed.setDescription(noPlayers);
        } else {
            StringBuilder list = new StringBuilder();
            for (String player : players) {
                list.append("• ").append(player).append("\n");
            }
            embed.setDescription(list.toString().trim());
            embed.addField(countLabel, String.valueOf(players.size()), true);

            if (totalPages > 1) {
                embed.addField(pageLabel, page + "/" + totalPages, true);
            }
        }

        return embed.build();
    }

    public static MessageEmbed createStatusEmbed(boolean enabled, boolean lockdown, int playerCount, Main plugin) {
        String title = "📊 Estado de la Whitelist";
        String statusLabel = "Estado";
        String countLabel = "Jugadores Totales";
        String enabledMsg = "Habilitada";
        String disabledMsg = "deshabilitada";
        String lockdownMsg = "Lockdown está activa";
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-info"));

        StringBuilder status = new StringBuilder(enabled ? enabledMsg : disabledMsg);
        if (lockdown) {
            status.append("\n").append(lockdownMsg);
        }

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(statusLabel, status.toString(), false)
                .addField(countLabel, String.valueOf(playerCount), true)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createToggleEmbed(boolean enabled, String actor, Main plugin) {
        String title = enabled ? "🟢 Whitelist Habilitada" : "🔴 Whitelist Deshabilitada";
        String actorLine = enabled ? "Habilitado por " + actor : "Deshabilitado por " + actor;
        String actorLabel = "Por";
        Color color = parseColor(enabled
                ? plugin.getConfigManager().getDiscordEmbedColor("color-success")
                : plugin.getConfigManager().getDiscordEmbedColor("color-warning"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createLockdownEmbed(String duration, String reason, String actor, Main plugin) {
        String title = "🔒 Lockdown Iniciado";
        String durationLabel = "Duración";
        String reasonLabel = "Razón";
        String actorLabel = "Por";
        String actorLine = "Lockdown por " + actor;
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-lockdown"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(durationLabel, duration, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now());

        if (reason != null && !reason.isEmpty()) {
            embed.addField(reasonLabel, reason, false);
        }

        return embed.build();
    }

    public static MessageEmbed createErrorEmbed(String message, Main plugin) {
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-error"));

        return new EmbedBuilder()
                .setTitle("Error")
                .setColor(color)
                .setDescription(message)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createSuccessEmbed(String message, Main plugin) {
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-success"));

        return new EmbedBuilder()
                .setTitle("Éxito")
                .setColor(color)
                .setDescription(message)
                .setFooter("SWhitelist")
                .setTimestamp(Instant.now())
                .build();
    }

    private static Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.decode("#22d2d4");
        }
    }
}
