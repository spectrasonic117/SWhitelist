package com.spectrasonic.SWhitelist;

import com.spectrasonic.SWhitelist.managers.CommandManager;
import com.spectrasonic.SWhitelist.managers.ConfigManager;
import com.spectrasonic.SWhitelist.managers.EventManager;

import lombok.Getter;

import com.spectrasonic.Utils.CommandUtils;
import com.spectrasonic.Utils.MessageUtils;

import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private CommandManager commandManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);

        CommandUtils.setPlugin(this);
        MessageUtils.sendStartupMessage(this);

    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

}