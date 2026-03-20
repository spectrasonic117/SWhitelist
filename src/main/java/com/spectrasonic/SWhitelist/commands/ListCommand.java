package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

public class ListCommand {

    // Ejecutar comando list
    public static void execute(CommandSender sender, Main plugin) {
        try {
            // Obtener todos los jugadores en la whitelist
            List<String> players = plugin.getDatabaseManager().getAllPlayers();

            // Verificar si la lista está vacía
            if (players.isEmpty()) {
                MessageUtils.sendMessage(sender, plugin.getMessageManager().getMessage("player-list-empty"));
                return;
            }

            // Crear string con la lista de jugadores
            StringJoiner joiner = new StringJoiner(", ");
            players.forEach(joiner::add);

            // Enviar mensaje con la lista
            String message = plugin.getMessageManager().getMessage("player-list") + joiner.toString();
            MessageUtils.sendMessage(sender, message);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener lista de jugadores: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }
}
