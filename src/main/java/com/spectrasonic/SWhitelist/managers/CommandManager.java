package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;

@Getter
public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        // Register commands here
    }
}