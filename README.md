# SWhitelist

Plugin simple de whitelist para servidores Minecraft Paper/Spigot 1.21.x con Java 21.

Guardado 100% en YAML (`whitelist.yml` + `config.yml`). Sin base de datos y sin integración con Discord.

## Requisitos

- Minecraft 1.21.x
- Paper o Spigot
- Java 21
- [CommandAPI](https://commandapi.jorel.dev/) instalado en el servidor

## Instalación

1. Descarga el archivo `.jar` del plugin
2. Colócalo en la carpeta `plugins` de tu servidor
3. Reinicia o recarga el servidor
4. El plugin generará automáticamente `config.yml`, `messages.yml` y `whitelist.yml`

## Comandos y Permisos

Todos los permisos por defecto son para OP.

| Comando                                 | Descripción                                 | Permiso               |
| --------------------------------------- | ------------------------------------------- | --------------------- |
| `/swhitelist`                           | Muestra el menú de ayuda                    | `swhitelist.use`      |
| `/swhitelist add <jugador>`             | Agrega un jugador a la whitelist            | `swhitelist.add`      |
| `/swhitelist remove <jugador>`          | Remueve un jugador de la whitelist          | `swhitelist.remove`   |
| `/swhitelist list`                      | Muestra todos los jugadores en la whitelist | `swhitelist.list`     |
| `/swhitelist on`                        | Activa la whitelist                         | `swhitelist.on`       |
| `/swhitelist off`                       | Desactiva la whitelist                      | `swhitelist.off`      |
| `/swhitelist lockdown <tiempo> [razón]` | Activa modo lockdown con countdown          | `swhitelist.lockdown` |
| `/swhitelist reload`                    | Recarga la configuración y mensajes         | `swhitelist.reload`   |

Permiso total: `swhitelist.*` (todos los anteriores).
Permiso de bypass: `swhitelist.bypass` evita el kick de whitelist y de lockdown.

## Guardado de datos

- `whitelist.yml`: lista de nombres (`whitelist: [- jugador1, - jugador2]`). La comparación de nombres ignora mayúsculas/minúsculas.
- `config.yml`: `whitelist.enabled` guarda el estado on/off y se actualiza con `/swhitelist on|off` y durante el lockdown.
- `messages.yml`: todos los textos, en formato MiniMessage.

## Configuración

`config.yml`:

- **Whitelist**: `whitelist.enabled` (estado actual)
- **Lockdown**:
  - `lockdown.kick-message`: mensaje de kick
  - `lockdown.countdown-sound`: sonido del countdown (nombre de `Sound` de Bukkit)
  - `lockdown.kick-mode`: `notlisted`, `everyone`, `nobypass` u `off`
- **Formatos de tiempo**: `formats.now|second|minute|hour`

## Ejemplos de Uso

### Agregar jugadores
```
/swhitelist add Steve
/swhitelist add Alex
```

### Ver lista de jugadores
```
/swhitelist list
```

### Activar/Desactivar whitelist
```
/swhitelist on
/swhitelist off
```

### Modo Lockdown
```
/swhitelist lockdown 5m Mantenimiento programado
/swhitelist lockdown 30s Reinicio rápido
```

Formatos de tiempo válidos:
- `30s` = 30 segundos
- `5m` = 5 minutos
- `1h` = 1 hora

### Recargar configuración
```
/swhitelist reload
```

## Características

- ✅ Whitelist guardada en `whitelist.yml`
- ✅ Sistema de lockdown con countdown
- ✅ Mensajes en formato MiniMessage
- ✅ Sonidos personalizables
- ✅ Múltiples modos de kick
- ✅ Sistema de permisos completo
