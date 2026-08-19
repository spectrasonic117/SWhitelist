package com.spectrasonic.SWhitelist.events;

import com.spectrasonic.SWhitelist.Main;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

@Getter
public class WhitelistEvent implements Listener {

    private final Main plugin;
    private final MiniMessage miniMessage;

    public WhitelistEvent(Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    // Verificar whitelist al intentar login — todo se lee del caché en memoria (sin I/O)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Verificar si el jugador tiene permiso de bypass
        if (player.hasPermission("swhitelist.bypass")) {
            return;
        }

        // Verificar si la whitelist está habilitada (lectura de caché, sin DB)
        if (!plugin.getDatabaseManager().isWhitelistEnabled()) {
            return;
        }

        // Verificar si el jugador está en la whitelist (lectura de caché, sin DB)
        if (!plugin.getDatabaseManager().isWhitelisted(playerName)) {
            // Seleccionar mensaje de kick según modo lockdown
            String kickMessage;
            if (plugin.isLockdownActive()) {
                kickMessage = plugin.getConfigManager().getLockdownKickMessage();
            } else {
                kickMessage = plugin.getMessageManager().getMessage("whitelist-kick");
            }
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                    miniMessage.deserialize(kickMessage));
        }
    }
}
