package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class RemoveCommand {

    // Ejecutar comando remove
    public static void execute(CommandSender sender, String player, Main plugin) {
        // Validar longitud del nombre del jugador
        if (player.length() < 3) {
            MessageUtils.alertMessage(sender, plugin.getMessageManager().getMessage("error-limit"));
            return;
        }

        try {
            // Verificar si el jugador existe en la whitelist
            if (!plugin.getWhitelistManager().isWhitelisted(player)) {
                String message = plugin.getMessageManager().getMessage("not-in-whitelist", "player", player);
                MessageUtils.denyMessage(sender, message);
                return;
            }

            // Remover jugador de la whitelist
            plugin.getWhitelistManager().removePlayer(player);
            String message = plugin.getMessageManager().getMessage("success-removed", "player", player);
            MessageUtils.successMessage(sender, message);

        } catch (Exception e) {
            plugin.getLogger().severe("Error al remover jugador de la whitelist: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-save"));
        }
    }
}
