package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.StringJoiner;

public class ListCommand {

    // Ejecutar comando list — lee de la lista en memoria, sin I/O
    public static void execute(CommandSender sender, Main plugin) {
        // Obtener todos los jugadores en la whitelist
        List<String> players = plugin.getWhitelistManager().getAllPlayers();

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
    }
}
