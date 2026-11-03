package com.spectrasonic.SWhitelist;

import com.spectrasonic.SWhitelist.commands.LockdownCommand;
import com.spectrasonic.SWhitelist.managers.CommandManager;
import com.spectrasonic.SWhitelist.managers.ConfigManager;
import com.spectrasonic.SWhitelist.managers.EventManager;
import com.spectrasonic.SWhitelist.managers.MessageManager;
import com.spectrasonic.SWhitelist.managers.WhitelistManager;
import com.spectrasonic.Utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Setter
public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private WhitelistManager whitelistManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private boolean lockdownActive = false;

    @Override
    public void onEnable() {
        // Inicializar managers
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.whitelistManager = new WhitelistManager(this);
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);

        // Mensaje de inicio
        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        // Cancelar countdown de lockdown activo (evita task leak)
        LockdownCommand.cancelActiveCountdown();

        // Mensaje de cierre
        MessageUtils.sendShutdownMessage(this);
        getLogger().info("SWhitelist ha sido deshabilitado.");
    }
}
