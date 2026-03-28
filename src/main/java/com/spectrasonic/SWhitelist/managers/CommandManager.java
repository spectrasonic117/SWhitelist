package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.SWhitelist.commands.SWhitelistCommand;
import lombok.Getter;

@Getter
public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        SWhitelistCommand.register(plugin);
    }
}
