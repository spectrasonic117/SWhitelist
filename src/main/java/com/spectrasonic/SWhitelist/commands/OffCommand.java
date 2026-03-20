package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class OffCommand {

    // Ejecutar comando off
    public static void execute(CommandSender sender, Main plugin) {
        try {
            // Verificar si la whitelist ya está deshabilitada
            if (!plugin.getDatabaseManager().isWhitelistEnabled()) {
                MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("whitelist-already-disabled"));
                return;
            }

            // Deshabilitar whitelist
            plugin.getDatabaseManager().disableWhitelist();
            MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("success-whitelist-off"));

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al deshabilitar whitelist: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}
