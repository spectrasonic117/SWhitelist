package com.spectrasonic.SWhitelist;

import com.spectrasonic.SWhitelist.database.DatabaseManager;
import com.spectrasonic.SWhitelist.managers.CommandManager;
import com.spectrasonic.SWhitelist.managers.ConfigManager;
import com.spectrasonic.SWhitelist.managers.EventManager;
import com.spectrasonic.SWhitelist.managers.MessageManager;
import com.spectrasonic.Utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Setter
public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private MiniMessage miniMessage;
    private boolean lockdownActive = false;

    @Override
    public void onEnable() {
        // Inicializar MiniMessage
        this.miniMessage = MiniMessage.miniMessage();

        // Inicializar managers
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);

        // Inicializar base de datos
        try {
            this.databaseManager = new DatabaseManager(this);
            getLogger().config("Base de datos SQLite conectada exitosamente.");
        } catch (Exception e) {
            getLogger().severe("Error al conectar con la base de datos: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Inicializar managers de comandos y eventos
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);

        // Mensaje de inicio
        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        // Cerrar conexión a base de datos
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        // Mensaje de cierre
        MessageUtils.sendShutdownMessage(this);
        getLogger().info("SWhitelist ha sido deshabilitado.");
    }
}
