package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class OnCommand {

    // Ejecutar comando on
    public static void execute(CommandSender sender, Main plugin) {
        try {
            // Verificar si la whitelist ya está habilitada
            if (plugin.getDatabaseManager().isWhitelistEnabled()) {
                MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("whitelist-already-enabled"));
                return;
            }

            // Habilitar whitelist
            plugin.getDatabaseManager().enableWhitelist();
            MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("success-whitelist-on"));

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al habilitar whitelist: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}
