package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.StringArgument;

public class SWhitelistCommand {

    public static void register(Main plugin) {
        new CommandAPICommand("swhitelist")
                .withAliases("sw")
                .withPermission(CommandPermission.OP)
                .withSubcommand(createAddCommand(plugin))
                .withSubcommand(createRemoveCommand(plugin))
                .withSubcommand(createListCommand(plugin))
                .withSubcommand(createOnCommand(plugin))
                .withSubcommand(createOffCommand(plugin))
                .withSubcommand(createLockdownCommand(plugin))
                .withSubcommand(createReloadCommand(plugin))
                .executes((sender, args) -> {
                    plugin.getMessageManager().getMessageList("help-menu")
                            .forEach(line -> MessageUtils.RawMessage(sender, line));
                })
                .register();
    }

    private static CommandAPICommand createAddCommand(Main plugin) {
        return new CommandAPICommand("add")
                .withArguments(new StringArgument("player"))
                .executes((sender, args) -> {
                    String player = (String) args.get("player");
                    AddCommand.execute(sender, player, plugin);
                });
    }

    private static CommandAPICommand createRemoveCommand(Main plugin) {
        return new CommandAPICommand("remove")
                .withArguments(new StringArgument("player"))
                .executes((sender, args) -> {
                    String player = (String) args.get("player");
                    RemoveCommand.execute(sender, player, plugin);
                });
    }

    private static CommandAPICommand createListCommand(Main plugin) {
        return new CommandAPICommand("list")
                .executes((sender, args) -> {
                    ListCommand.execute(sender, plugin);
                });
    }

    private static CommandAPICommand createOnCommand(Main plugin) {
        return new CommandAPICommand("on")
                .executes((sender, args) -> {
                    OnCommand.execute(sender, plugin);
                });
    }

    private static CommandAPICommand createOffCommand(Main plugin) {
        return new CommandAPICommand("off")
                .executes((sender, args) -> {
                    OffCommand.execute(sender, plugin);
                });
    }

    private static CommandAPICommand createLockdownCommand(Main plugin) {
        return new CommandAPICommand("lockdown")
                .withArguments(new StringArgument("time"))
                .withOptionalArguments(new StringArgument("reason"))
                .executes((sender, args) -> {
                    String time = (String) args.get("time");
                    String reason = (String) args.getOrDefault("reason", null);
                    LockdownCommand.execute(sender, time, reason, plugin);
                });
    }

    private static CommandAPICommand createReloadCommand(Main plugin) {
        return new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    ReloadCommand.execute(sender, plugin);
                });
    }
}
