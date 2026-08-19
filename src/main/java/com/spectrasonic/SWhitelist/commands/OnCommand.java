package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class OnCommand {

    // Ejecutar comando on
    public static void execute(CommandSender sender, Main plugin) {
        // Verificar si la whitelist ya está habilitada (lectura de caché)
        if (plugin.getDatabaseManager().isWhitelistEnabled()) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("whitelist-already-enabled"));
            return;
        }

        // Habilitar whitelist (caché se actualiza inmediatamente, DB se escribe async)
        plugin.getDatabaseManager().setWhitelistEnabledAsync(true);
        MessageUtils.successMessage(sender, plugin.getMessageManager().getMessage("success-whitelist-on"));

        // Notificar a Discord
        if (plugin.getDiscordManager() != null) {
            plugin.getDiscordManager().notifyWhitelistToggled(true, sender.getName());
        }
    }
}
