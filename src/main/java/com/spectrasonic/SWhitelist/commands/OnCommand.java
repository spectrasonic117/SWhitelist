package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class OnCommand {

    // Ejecutar comando on
    public static void execute(CommandSender sender, Main plugin) {
        // Verificar si la whitelist ya está habilitada
        if (plugin.getWhitelistManager().isWhitelistEnabled()) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("whitelist-already-enabled"));
            return;
        }

        // Habilitar whitelist
        plugin.getWhitelistManager().setWhitelistEnabled(true);
        MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("success-whitelist-on"));
    }
}
