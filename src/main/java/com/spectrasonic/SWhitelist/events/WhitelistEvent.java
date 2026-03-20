package com.spectrasonic.SWhitelist.events;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.SQLException;

@Getter
public class WhitelistEvent implements Listener {

    private final Main plugin;
    private final MiniMessage miniMessage;

    public WhitelistEvent(Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    // Verificar whitelist al intentar login
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Verificar si el jugador tiene permiso de bypass
        if (player.hasPermission("swhitelist.bypass")) {
            return;
        }

        try {
            // Verificar si la whitelist está habilitada
            if (!plugin.getDatabaseManager().isWhitelistEnabled()) {
                return;
            }

            // Verificar si el jugador está en la whitelist
            if (!plugin.getDatabaseManager().isWhitelisted(playerName)) {
                // Kickear al jugador con mensaje personalizado
                String kickMessage = plugin.getConfigManager().getLockdownKickMessage();
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, 
                        miniMessage.deserialize(kickMessage));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al verificar whitelist para " + playerName + ": " + e.getMessage());
            // En caso de error, permitir el acceso (fail-safe)
        }
    }
}
