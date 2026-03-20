package com.spectrasonic.SWhitelist.managers;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.SWhitelist.events.WhitelistEvent;
import lombok.Getter;

@Getter
public class EventManager {

    private final Main plugin;

    public EventManager(Main plugin) {
        this.plugin = plugin;
        registerEvents();
    }

    // Registrar todos los eventos del plugin
    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(
                new WhitelistEvent(plugin),
                plugin);
    }
}
