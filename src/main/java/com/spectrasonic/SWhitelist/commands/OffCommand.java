package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class OffCommand {

    // Ejecutar comando off
    public static void execute(CommandSender sender, Main plugin) {
        // Verificar si la whitelist ya está deshabilitada
        if (!plugin.getWhitelistManager().isWhitelistEnabled()) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("whitelist-already-disabled"));
            return;
        }

        // Deshabilitar whitelist
        plugin.getWhitelistManager().setWhitelistEnabled(false);
        plugin.setLockdownActive(false);
        MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("success-whitelist-off"));
    }
}
