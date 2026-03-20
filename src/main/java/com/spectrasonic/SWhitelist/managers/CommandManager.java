package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.SWhitelist.commands.*;
import com.spectrasonic.Utils.MessageUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TimeArgument;
import lombok.Getter;

@Getter
public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    // Registrar todos los comandos del plugin
    private void registerCommands() {
        // Comando principal /swhitelist
        new CommandAPICommand("swhitelist")
                .withPermission(CommandPermission.OP)
                .withSubcommand(createAddCommand())
                .withSubcommand(createRemoveCommand())
                .withSubcommand(createListCommand())
                .withSubcommand(createOnCommand())
                .withSubcommand(createOffCommand())
                .withSubcommand(createLockdownCommand())
                .withSubcommand(createReloadCommand())
                .executes((sender, args) -> {
                    // Mostrar ayuda si no se especifica subcomando
                    plugin.getMessageManager().getMessageList("help-menu")
                            .forEach(line -> MessageUtils.noPrefixMessage(sender, line));
                })
                .register();
    }

    // Crear subcomando add
    private CommandAPICommand createAddCommand() {
        return new CommandAPICommand("add")
                .withArguments(new StringArgument("player"))
                .executes((sender, args) -> {
                    String player = (String) args.get("player");
                    AddCommand.execute(sender, player, plugin);
                });
    }

    // Crear subcomando remove
    private CommandAPICommand createRemoveCommand() {
        return new CommandAPICommand("remove")
                .withArguments(new StringArgument("player"))
                .executes((sender, args) -> {
                    String player = (String) args.get("player");
                    RemoveCommand.execute(sender, player, plugin);
                });
    }

    // Crear subcomando list
    private CommandAPICommand createListCommand() {
        return new CommandAPICommand("list")
                .withPermission("swhitelist.list")
                .executes((sender, args) -> {
                    ListCommand.execute(sender, plugin);
                });
    }

    // Crear subcomando on
    private CommandAPICommand createOnCommand() {
        return new CommandAPICommand("on")
                .withPermission("swhitelist.on")
                .executes((sender, args) -> {
                    OnCommand.execute(sender, plugin);
                });
    }

    // Crear subcomando off
    private CommandAPICommand createOffCommand() {
        return new CommandAPICommand("off")
                .executes((sender, args) -> {
                    OffCommand.execute(sender, plugin);
                });
    }

    // Crear subcomando lockdown
    private CommandAPICommand createLockdownCommand() {
        return new CommandAPICommand("lockdown")
                .withArguments(new TimeArgument("time"))
                .withOptionalArguments(new StringArgument("reason"))
                .executes((sender, args) -> {
                    String time = (String) args.get("time");
                    String reason = (String) args.getOrDefault("reason", null);
                    LockdownCommand.execute(sender, time, reason, plugin);
                });
    }

    // Crear subcomando reload
    private CommandAPICommand createReloadCommand() {
        return new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    ReloadCommand.execute(sender, plugin);
                });
    }
}
