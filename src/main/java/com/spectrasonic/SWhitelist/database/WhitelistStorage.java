package com.spectrasonic.SWhitelist.database;

import java.util.List;

/**
 * Contrato de persistencia de la whitelist.
 * Las implementaciones solo acceden al almacenamiento;
 * el caché en memoria vive en {@link DatabaseManager}.
 */
public interface WhitelistStorage {

    // Cargar todos los nombres guardados
    List<String> loadPlayers();

    // Guardar el estado enabled/disabled de la whitelist
    void saveWhitelistEnabled(boolean enabled) throws Exception;

    // Leer el estado enabled/disabled persistido
    boolean loadWhitelistEnabled();

    // Insertar un jugador (puede traer discord_id asociado)
    void addPlayer(String username, String discordId) throws Exception;

    // Eliminar un jugador por nombre
    void removePlayer(String username) throws Exception;

    // Verificar si un nombre existe
    boolean doesPlayerExist(String username) throws Exception;

    // Obtener el discord_id asociado a un nombre (null si no tiene)
    String getDiscordId(String username) throws Exception;

    // Liberar recursos (conexión, executor, etc.)
    void close() throws Exception;
}
