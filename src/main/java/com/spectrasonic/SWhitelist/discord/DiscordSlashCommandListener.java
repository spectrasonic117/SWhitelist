package com.spectrasonic.SWhitelist.discord;

import com.spectrasonic.SWhitelist.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscordSlashCommandListener extends ListenerAdapter {

    private final Main plugin;

    public DiscordSlashCommandListener(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        String channelId = plugin.getConfigManager().getDiscordChannelId();
        if (channelId != null && !channelId.isEmpty() && !event.getChannel().getId().equals(channelId)) {
            String msg = plugin.getMessageManager().getDiscordMessage("error-wrong-channel");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(msg, plugin))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Permission check: admin roles get all commands, user roles get only "add"
        boolean isAdmin = isAdmin(member);
        boolean isUser = isUser(member);

        if (!isAdmin && !isUser) {
            String msg = plugin.getMessageManager().getDiscordMessage("error-no-permission");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(msg, plugin))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Non-admin users can only use "add"
        if (!isAdmin && !"add".equals(subcommand)) {
            String msg = plugin.getMessageManager().getDiscordMessage("error-admin-only");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(msg, plugin))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        switch (subcommand) {
            case "add" -> handleAdd(event);
            case "remove" -> handleRemove(event);
            case "list" -> handleList(event);
            case "status" -> handleStatus(event);
        }
    }

    private void handleAdd(SlashCommandInteractionEvent event) {
        String player = event.getOption("player", null, option -> option.getAsString().trim());
        if (player == null || player.isEmpty())
            return;
        if (player.length() < 3) {
            String msg = plugin.getMessageManager().getDiscordMessage("error-player-name-short");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(msg, plugin))
                    .setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue(hook -> {
            try {
                if (plugin.getDatabaseManager().doesPlayerExist(player)) {
                    String msg = plugin.getMessageManager().getDiscordMessage("error-player-exists");
                    hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
                    return;
                }
                plugin.getDatabaseManager().addPlayer(player);
                String actor = event.getUser().getName();
                hook.sendMessageEmbeds(EmbedUtils.createAddEmbed(player, actor, plugin)).queue();
                plugin.getLogger().info("Agregado a whitelist: " + player);

                // Asignar rol de whitelist al miembro que ejecutó el comando
                Member member = event.getMember();
                if (member != null) {
                    plugin.getDiscordManager().assignWhitelistedRole(member);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Discord add error: " + e.getMessage());
                String msg = plugin.getMessageManager().getDiscordMessage("error-database");
                hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
            }
        });
    }

    private void handleRemove(SlashCommandInteractionEvent event) {
        String player = event.getOption("player", null, option -> option.getAsString().trim());
        if (player == null || player.isEmpty())
            return;
        if (player.length() < 3) {
            String msg = plugin.getMessageManager().getDiscordMessage("error-player-name-short");
            event.replyEmbeds(EmbedUtils.createErrorEmbed(msg, plugin))
                    .setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue(hook -> {
            try {
                if (!plugin.getDatabaseManager().isWhitelisted(player)) {
                    String msg = plugin.getMessageManager().getDiscordMessage("error-not-in-whitelist");
                    hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
                    return;
                }
                plugin.getDatabaseManager().removePlayer(player);
                String actor = event.getUser().getName();
                hook.sendMessageEmbeds(EmbedUtils.createRemoveEmbed(player, actor, plugin)).queue();
                plugin.getLogger().info("Removido de whitelist: " + player);
            } catch (SQLException e) {
                plugin.getLogger().severe("Discord remove error: " + e.getMessage());
                String msg = plugin.getMessageManager().getDiscordMessage("error-database");
                hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
            }
        });
    }

    private void handleList(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue(hook -> {
            try {
                List<String> allPlayers = plugin.getDatabaseManager().getAllPlayers();
                if (allPlayers.isEmpty()) {
                    hook.sendMessageEmbeds(EmbedUtils.createListEmbed(allPlayers, 1, 1, plugin)).queue();
                } else {
                    List<MessageEmbed> pages = paginatePlayers(allPlayers, 25);
                    hook.sendMessageEmbeds(pages.get(0)).queue();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Discord list error: " + e.getMessage());
                String msg = plugin.getMessageManager().getDiscordMessage("error-database");
                hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
            }
        });
    }

    private void handleStatus(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue(hook -> {
            try {
                boolean enabled = plugin.getDatabaseManager().isWhitelistEnabled();
                boolean lockdown = plugin.isLockdownActive();
                int count = plugin.getDatabaseManager().getAllPlayers().size();
                hook.sendMessageEmbeds(EmbedUtils.createStatusEmbed(enabled, lockdown, count, plugin)).queue();
            } catch (SQLException e) {
                plugin.getLogger().severe("Discord status error: " + e.getMessage());
                String msg = plugin.getMessageManager().getDiscordMessage("error-database");
                hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed(msg, plugin)).queue();
            }
        });
    }

    private boolean isAdmin(Member member) {
        List<String> adminRoleIds = plugin.getConfigManager().getDiscordAdminRoles();
        if (adminRoleIds.isEmpty())
            return false;
        return hasMatchingRole(member, adminRoleIds);
    }

    private boolean isUser(Member member) {
        List<String> userRoleIds = plugin.getConfigManager().getDiscordUserRoles();
        if (userRoleIds.isEmpty())
            return false;
        return hasMatchingRole(member, userRoleIds);
    }

    private boolean hasMatchingRole(Member member, List<String> roleIds) {
        for (Role role : member.getRoles()) {
            if (roleIds.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }

    private List<MessageEmbed> paginatePlayers(List<String> players, int perPage) {
        List<MessageEmbed> pages = new ArrayList<>();
        int totalPages = (int) Math.ceil((double) players.size() / perPage);
        for (int i = 0; i < totalPages; i++) {
            int from = i * perPage;
            int to = Math.min(from + perPage, players.size());
            List<String> page = players.subList(from, to);
            pages.add(EmbedUtils.createListEmbed(page, i + 1, totalPages, plugin));
        }
        return pages;
    }
}
