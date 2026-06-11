package com.spectrasonic.SWhitelist.discord;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

@Getter
public class DiscordManager {

    private final Main plugin;
    private JDA jda;
    private boolean connected = false;

    public DiscordManager(Main plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        if (!plugin.getConfigManager().isDiscordEnabled()) {
            plugin.getLogger().info("Discord integration is disabled in config.");
            return;
        }

        String token = plugin.getConfigManager().getDiscordBotToken();
        if (token == null || token.isEmpty()) {
            plugin.getLogger().warning("Discord bot token is empty. Discord integration disabled.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token, EnumSet.noneOf(GatewayIntent.class))
                    .addEventListeners(new DiscordSlashCommandListener(plugin))
                    .build();

            jda.awaitReady();
            registerSlashCommands();
            connected = true;
            plugin.getLogger().info("Discord bot connected successfully as " + jda.getSelfUser().getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to Discord: " + e.getMessage());
            connected = false;
        }
    }

    private void registerSlashCommands() {
        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.isEmpty()) {
            plugin.getLogger().warning("Discord guild-id is not set. Slash commands not registered.");
            return;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            plugin.getLogger().warning("Discord guild not found with id: " + guildId);
            return;
        }

        // Clean existing commands first, then register fresh
        guild.retrieveCommands().queue(existing -> {
            for (Command cmd : existing) {
                if (cmd.getName().equals("whitelist")) {
                    cmd.delete().queue();
                }
            }
            guild.upsertCommand(
                    Commands.slash("whitelist", "Manage the server whitelist")
                            .addSubcommands(
                                    new SubcommandData("add", "Add a player to the whitelist")
                                            .addOption(OptionType.STRING, "player", "Minecraft username", true),
                                    new SubcommandData("remove", "Remove a player from the whitelist")
                                            .addOption(OptionType.STRING, "player", "Minecraft username", true),
                                    new SubcommandData("list", "List all whitelisted players"),
                                    new SubcommandData("status", "Show whitelist status")))
                    .queue(
                            success -> plugin.getLogger().info("Discord slash commands registered."),
                            error -> plugin.getLogger()
                                    .severe("Failed to register Discord slash commands: " + error.getMessage()));
        });
    }

    public void shutdown() {
        if (jda != null) {
            try {
                jda.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Error shutting down Discord bot: " + e.getMessage());
            }
            connected = false;
        }
    }

    public void reload() {
        shutdown();
        init();
    }

    public void notifyPlayerAdded(String player, String actor) {
        if (!canNotify("whitelist-add"))
            return;
        MessageEmbed embed = EmbedUtils.createAddEmbed(player, actor, plugin);
        sendNotification(embed);
    }

    public void notifyPlayerRemoved(String player, String actor) {
        if (!canNotify("whitelist-remove"))
            return;
        MessageEmbed embed = EmbedUtils.createRemoveEmbed(player, actor, plugin);
        sendNotification(embed);
    }

    public void notifyWhitelistToggled(boolean enabled, String actor) {
        String key = enabled ? "whitelist-on" : "whitelist-off";
        if (!canNotify(key))
            return;
        MessageEmbed embed = EmbedUtils.createToggleEmbed(enabled, actor, plugin);
        sendNotification(embed);
    }

    public void notifyLockdown(String duration, String reason, String actor) {
        if (!canNotify("lockdown"))
            return;
        MessageEmbed embed = EmbedUtils.createLockdownEmbed(duration, reason, actor, plugin);
        sendNotification(embed);
    }

    private boolean canNotify(String notificationKey) {
        if (!connected)
            return false;
        return plugin.getConfigManager().isDiscordNotificationEnabled(notificationKey);
    }

    private void sendNotification(MessageEmbed embed) {
        if (jda == null)
            return;
        String channelId = plugin.getConfigManager().getDiscordChannelId();
        if (channelId == null || channelId.isEmpty())
            return;

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            plugin.getLogger().warning("Discord channel not found: " + channelId);
            return;
        }

        channel.sendMessageEmbeds(embed).queue(
                success -> {
                },
                error -> plugin.getLogger().warning("Failed to send Discord notification: " + error.getMessage()));
    }
}
