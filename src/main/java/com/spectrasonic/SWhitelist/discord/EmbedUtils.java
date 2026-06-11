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
        String title = plugin.getMessageManager().getDiscordMessage("embed-title-add");
        String actorLine = plugin.getMessageManager().getDiscordMessage("added-by", "actor", actor);
        String playerLabel = plugin.getMessageManager().getDiscordMessage("embed-field-player");
        String actorLabel = plugin.getMessageManager().getDiscordMessage("embed-field-actor");
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-success"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(playerLabel, player, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createRemoveEmbed(String player, String actor, Main plugin) {
        String title = plugin.getMessageManager().getDiscordMessage("embed-title-remove");
        String actorLine = plugin.getMessageManager().getDiscordMessage("removed-by", "actor", actor);
        String playerLabel = plugin.getMessageManager().getDiscordMessage("embed-field-player");
        String actorLabel = plugin.getMessageManager().getDiscordMessage("embed-field-actor");
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-warning"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(playerLabel, player, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createListEmbed(List<String> players, int page, int totalPages, Main plugin) {
        String title = plugin.getMessageManager().getDiscordMessage("embed-title-list");
        String countLabel = plugin.getMessageManager().getDiscordMessage("embed-field-count");
        String pageLabel = plugin.getMessageManager().getDiscordMessage("embed-field-page");
        String noPlayers = plugin.getMessageManager().getDiscordMessage("status-no-players");
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-info"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
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
        String title = plugin.getMessageManager().getDiscordMessage("embed-title-status");
        String statusLabel = plugin.getMessageManager().getDiscordMessage("embed-field-status");
        String countLabel = plugin.getMessageManager().getDiscordMessage("embed-field-count");
        String enabledMsg = plugin.getMessageManager().getDiscordMessage("status-enabled");
        String disabledMsg = plugin.getMessageManager().getDiscordMessage("status-disabled");
        String lockdownMsg = plugin.getMessageManager().getDiscordMessage("status-lockdown-active");
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
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createToggleEmbed(boolean enabled, String actor, Main plugin) {
        String titleKey = enabled ? "embed-title-on" : "embed-title-off";
        String actorKey = enabled ? "enabled-by" : "disabled-by";
        String title = plugin.getMessageManager().getDiscordMessage(titleKey);
        String actorLine = plugin.getMessageManager().getDiscordMessage(actorKey, "actor", actor);
        String actorLabel = plugin.getMessageManager().getDiscordMessage("embed-field-actor");
        Color color = parseColor(enabled
                ? plugin.getConfigManager().getDiscordEmbedColor("color-success")
                : plugin.getConfigManager().getDiscordEmbedColor("color-warning"));

        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createLockdownEmbed(String duration, String reason, String actor, Main plugin) {
        String title = plugin.getMessageManager().getDiscordMessage("embed-title-lockdown");
        String durationLabel = plugin.getMessageManager().getDiscordMessage("embed-field-duration");
        String reasonLabel = plugin.getMessageManager().getDiscordMessage("embed-field-reason");
        String actorLabel = plugin.getMessageManager().getDiscordMessage("embed-field-actor");
        String actorLine = plugin.getMessageManager().getDiscordMessage("lockdown-by", "actor", actor);
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-lockdown"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .addField(durationLabel, duration, true)
                .addField(actorLabel, actor, true)
                .addField("", actorLine, false)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
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
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed createSuccessEmbed(String message, Main plugin) {
        Color color = parseColor(plugin.getConfigManager().getDiscordEmbedColor("color-success"));

        return new EmbedBuilder()
                .setTitle("Success")
                .setColor(color)
                .setDescription(message)
                .setFooter(plugin.getConfigManager().getDiscordEmbedFooter())
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
