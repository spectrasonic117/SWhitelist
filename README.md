# SWhitelist

Plugin moderno de whitelist para servidores Minecraft Paper/Spigot 1.21.x con Java 21.

## Requisitos

- Minecraft 1.21.x
- Paper o Spigot
- Java 21
- CommandAPI (dependencia)

## Instalación

1. Descarga el archivo `.jar` del plugin
2. Colócalo en la carpeta `plugins` de tu servidor
3. Reinicia o recarga el servidor
4. El plugin generará automáticamente los archivos de configuración

## Comandos y Permisos

Todos los permisos por defecto son para OP.

### Comando Principal

| Comando       | Descripción       | Permiso          |
| ------------- | ----------------- | ---------------- |
| `/swhitelist` | Comando principal | `swhitelist.use` |

### Subcomandos

| Comando                                 | Descripción                                 | Permiso               |
| --------------------------------------- | ------------------------------------------- | --------------------- |
| `/swhitelist add <jugador>`             | Agrega un jugador a la whitelist            | `swhitelist.add`      |
| `/swhitelist remove <jugador>`          | Remueve un jugador de la whitelist          | `swhitelist.remove`   |
| `/swhitelist list`                      | Muestra todos los jugadores en la whitelist | `swhitelist.list`     |
| `/swhitelist on`                        | Activa la whitelist                         | `swhitelist.on`       |
| `/swhitelist off`                       | Desactiva la whitelist                      | `swhitelist.off`      |
| `/swhitelist lockdown <tiempo> [razón]` | Activa modo lockdown con countdown          | `swhitelist.lockdown` |
| `/swhitelist reload`                    | Recarga la configuración y mensajes         | `swhitelist.reload`   |

### Permiso Total

| Permiso        | Descripción                          |
| -------------- | ------------------------------------ |
| `swhitelist.*` | Otorga todos los permisos del plugin |

## Configuración

El archivo `config.yml` permite personalizar:

- **Prefijo del plugin**: Formato MiniMessage para mensajes
- **Base de datos**: Archivo SQLite (por defecto `database.db`)
- **Lockdown**:
  - Mensaje de kick
  - Sonido de countdown
  - Modo de kick (`notlisted`, `everyone`, `nobypass`, `off`)
- **Formatos de tiempo**: Personalización para segundos, minutos y horas

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

- ✅ Base de datos SQLite integrada
- ✅ Sistema de lockdown con countdown
- ✅ Mensajes en formato MiniMessage
- ✅ Sonidos personalizables
- ✅ Múltiples modos de kick
- ✅ Sistema de permisos completo