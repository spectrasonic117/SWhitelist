package com.spectrasonic.SWhitelist.discord;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class DiscordManager {

    private final Main plugin;
    private JDA jda;
    private boolean connected = false;

    public DiscordManager(Main plugin) {
        this.plugin = plugin;
        init();
    }

    private static final Set<CacheFlag> DISABLED_CACHE_FLAGS = EnumSet.of(
            CacheFlag.VOICE_STATE,
            CacheFlag.EMOJI,
            CacheFlag.STICKER,
            CacheFlag.SCHEDULED_EVENTS);

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
            jda = JDABuilder.createDefault(token, EnumSet.of(
                    GatewayIntent.GUILD_MEMBERS))
                    .disableCache(DISABLED_CACHE_FLAGS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
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
                                    new SubcommandData("status", "Show whitelist status"),
                                    new SubcommandData("check", "Check if a player is whitelisted")
                                            .addOption(OptionType.STRING, "player", "Minecraft username", true)))
                    .queue(
                            success -> plugin.getLogger().info("Discord slash commands registered."),
                            error -> plugin.getLogger()
                                    .severe("Failed to register Discord slash commands: " + error.getMessage()));
        });
    }

    public void shutdown() {
        if (jda != null) {
            try {
                jda.shutdownNow();
                jda.awaitShutdown(java.time.Duration.ofSeconds(5));
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

    public void assignWhitelistedRole(Member member) {
        if (!connected)
            return;

        String roleId = plugin.getConfigManager().getDiscordWhitelistedRoleId();
        if (roleId == null || roleId.isEmpty()) {
            plugin.getLogger().warning("whitelisted-rol no configurado en config.yml");
            return;
        }

        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.isEmpty()) {
            plugin.getLogger().warning("Discord guild-id no está configurado.");
            return;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            plugin.getLogger().warning("Guild no encontrada con id: " + guildId);
            return;
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            plugin.getLogger().warning("Rol no encontrado con id: " + roleId);
            return;
        }

        guild.addRoleToMember(member, role)
                .queue(
                        success -> plugin.getLogger()
                                .info("Rol " + role.getName() + " asignado a " + member.getUser().getName()),
                        error -> plugin.getLogger().warning("Error al asignar rol: " + error.getMessage()));
    }

    public void removeWhitelistedRole(Member member) {
        if (!connected)
            return;

        String roleId = plugin.getConfigManager().getDiscordWhitelistedRoleId();
        if (roleId == null || roleId.isEmpty()) {
            return;
        }

        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.isEmpty()) {
            return;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return;
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            return;
        }

        guild.removeRoleFromMember(member, role)
                .queue(
                        success -> plugin.getLogger()
                                .info("Rol " + role.getName() + " removido de " + member.getUser().getName()),
                        error -> plugin.getLogger().warning("Error al remover rol: " + error.getMessage()));
    }

    public void removeWhitelistedRoleByDiscordId(String discordId) {
        if (!connected || discordId == null || discordId.isEmpty())
            return;

        String guildId = plugin.getConfigManager().getDiscordGuildId();
        if (guildId == null || guildId.isEmpty()) {
            return;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return;
        }

        String roleId = plugin.getConfigManager().getDiscordWhitelistedRoleId();
        if (roleId == null || roleId.isEmpty()) {
            return;
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            return;
        }

        guild.retrieveMemberById(discordId).queue(member -> {
            guild.removeRoleFromMember(member, role)
                    .queue(
                            success -> plugin.getLogger()
                                    .info("Rol " + role.getName() + " removido de " + member.getUser().getName()),
                            error -> plugin.getLogger().warning("Error al remover rol: " + error.getMessage()));
        }, error -> plugin.getLogger().warning("No se pudo obtener el miembro con Discord ID: " + discordId));
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
