package com.spectrasonic.SWhitelist.commands;

import com.spectrasonic.SWhitelist.Main;
import com.spectrasonic.SWhitelist.utils.TimeUtils;
import com.spectrasonic.Utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class LockdownCommand {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Ejecutar comando lockdown
    public static void execute(CommandSender sender, String timeString, String reason, Main plugin) {
        // Validar formato de tiempo
        if (!TimeUtils.isValidTimeFormat(timeString)) {
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("lockdown-invalid-format"));
            return;
        }

        // Parsear tiempo
        long delayMillis = TimeUtils.parseTime(timeString);
        if (delayMillis <= 0) {
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("lockdown-invalid-format"));
            return;
        }

        try {
            // Deshabilitar whitelist temporalmente
            plugin.getDatabaseManager().disableWhitelist();

            // Formatear duración para el mensaje
            String duration = TimeUtils.formatDuration(
                    delayMillis,
                    plugin.getConfigManager().getTimeFormat("now"),
                    plugin.getConfigManager().getTimeFormat("second"),
                    plugin.getConfigManager().getTimeFormat("minute"),
                    plugin.getConfigManager().getTimeFormat("hour")
            );

            // Anunciar lockdown
            String message = plugin.getMessageManager().getMessage("lockdown-announce", "duration", duration);
            MessageUtils.alertMessage(sender, message);

            // Programar countdown
            scheduleCountdown(sender, delayMillis, reason, plugin);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al iniciar lockdown: " + e.getMessage());
            MessageUtils.denyMessage(sender, plugin.getMessageManager().getMessage("error-database"));
        }
    }

    // Programar countdown de lockdown
    private static void scheduleCountdown(CommandSender sender, long delayMillis, String reason, Main plugin) {
        long startTime = System.currentTimeMillis();
        AtomicBoolean lockdownExecuted = new AtomicBoolean(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = Math.max(0, (delayMillis - elapsed + 999) / 1000);

                // Anunciar countdown en intervalos específicos
                if (remaining > 0 && (remaining == 30 || remaining == 15 || (remaining <= 5 && remaining > 0))) {
                    String countdownMessage = plugin.getMessageManager().getMessage("lockdown-countdown", 
                            "time", String.valueOf(remaining));
                    MessageUtils.BroadcastMessage(countdownMessage);

                    // Reproducir sonido
                    String soundName = plugin.getConfigManager().getLockdownCountdownSound();
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Sonido inválido en configuración: " + soundName);
                    }
                }

                // Ejecutar lockdown cuando el tiempo se agote
                if (elapsed >= delayMillis && !lockdownExecuted.get()) {
                    executeLockdown(reason, plugin);
                    lockdownExecuted.set(true);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada segundo
    }

    // Ejecutar lockdown
    private static void executeLockdown(String reason, Main plugin) {
        try {
            // Habilitar whitelist
            plugin.getDatabaseManager().enableWhitelist();

            // Activar flag de lockdown para mensajes de kick
            plugin.setLockdownActive(true);

            // Anunciar lockdown exitoso
            MessageUtils.BroadcastMessage(plugin.getMessageManager().getMessage("lockdown-success"));

            // Kickear jugadores según configuración
            String kickMode = plugin.getConfigManager().getLockdownKickMode();
            String kickMessage = plugin.getConfigManager().getLockdownKickMessage();

            switch (kickMode.toLowerCase()) {
                case "notlisted" -> kickNotListedPlayers(kickMessage, plugin);
                case "everyone" -> kickEveryone(kickMessage, plugin);
                case "nobypass" -> kickNoBypassPlayers(kickMessage, plugin);
                case "off" -> plugin.getLogger().info("Lockdown ejecutado sin kickear jugadores.");
                default -> plugin.getLogger().warning("Modo de kick inválido: " + kickMode);
            }

            // Desactivar flag de lockdown después de kickear
            plugin.setLockdownActive(false);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al ejecutar lockdown: " + e.getMessage());
        }
    }

    // Kickear jugadores no en la whitelist
    private static void kickNotListedPlayers(String kickMessage, Main plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (!player.hasPermission("swhitelist.bypass") && 
                    !plugin.getDatabaseManager().isWhitelisted(player.getName())) {
                    player.kick(miniMessage.deserialize(kickMessage));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error al verificar jugador " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    // Kickear a todos los jugadores
    private static void kickEveryone(String kickMessage, Main plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("swhitelist.bypass")) {
                player.kick(miniMessage.deserialize(kickMessage));
            }
        }
    }

    // Kickear jugadores sin permiso de bypass
    private static void kickNoBypassPlayers(String kickMessage, Main plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("swhitelist.bypass")) {
                player.kick(miniMessage.deserialize(kickMessage));
            }
        }
    }
}
