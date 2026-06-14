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

## Integración con Discord

SWhitelist incluye integración con Discord a través de un bot que permite gestionar la whitelist directamente desde el servidor de Discord.

### 1. Crear el Bot de Discord

1. Ve a [Discord Developer Portal](https://discord.com/developers/applications)
2. Haz clic en **"New Application"** y dale un nombre (ej: `SWhitelist Bot`)
3. Ve a la pestaña **"Bot"** y haz clic en **"Add Bot"**
4. Copia el **Token** (haz clic en "Copy") ⚠️ Guarda este token, solo se muestra una vez
5. Activa los siguientes **Privileged Gateway Intents**:
   - ✅ **Server Members Intent** (necesario para verificar roles)
6. Ve a la pestaña **"OAuth2" → "URL Generator"**
7. Selecciona los scopes: `bot` y `applications.commands`
8. Selecciona los permisos:
   - `Send Messages`
   - `Use Slash Commands`
   - `Embed Links`
9. Copia la URL generada y ábrela en tu navegador para agregar el bot a tu servidor

### 2. Obtener IDs Necesarios

Necesitas 4 IDs para configurar el plugin:

| ID | Cómo obtenerlo |
|----|----------------|
| **Bot Token** | Developer Portal → Tu bot → Bot → Token |
| **Guild ID (Server ID)** | En Discord, activa "Modo Desarrollador" (Ajustes → Avanzado) → Botón derecho en el servidor → "Copiar ID del servidor" |
| **Channel ID** | Botón derecho en el canal de whitelist → "Copiar ID del canal" |
| **Role IDs** | Botón derecho en cada rol → "Copiar ID del rol" |

### 3. Configurar el `config.yml`

Edita el archivo `plugins/SWhitelist/config.yml` con la sección de Discord:

```yaml
discord:
  # Habilitar la integración con Discord
  enabled: true

  # Token del bot de Discord
  bot-token: "TU_TOKEN_AQUI"

  # ID del servidor (guild) de Discord
  guild-id: "123456789012345678"

  # ID del canal donde se aceptarán comandos y se enviarán notificaciones
  channel-id: "123456789012345678"

  # ID del rol que se asignará al usuario cuando se agrega a la whitelist
  whitelisted-rol: "123456789012345678"

  # Roles de Discord por ID
  roles:
    # Roles con acceso total (add, remove, list, status, check)
    admin:
      - "123456789012345678"
    # Roles con acceso limitado (add y check solamente)
    user:
      - "123456789012345678"

  # Notificaciones: qué eventos se notifican en Discord
  notifications:
    whitelist-add: true
    whitelist-remove: true
    whitelist-on: true
    whitelist-off: true
    lockdown: true

  # Colores de los embeds
  embed:
    color-success: "#22d2d4"
    color-error: "#FF5555"
    color-info: "#3ac990"
    color-warning: "#FFAA00"
    color-lockdown: "#FF0000"
    footer-text: "SWhitelist"
```

### 4. Comandos de Discord

Una vez configurado, el bot registrará automáticamente estos comandos slash en tu servidor:

| Comando | Descripción | Roles permitidos |
|---------|-------------|------------------|
| `/whitelist add <player>` | Agrega un jugador a la whitelist | Admin, User |
| `/whitelist remove <player>` | Remueve un jugador de la whitelist | Admin |
| `/whitelist list` | Lista todos los jugadores en la whitelist | Admin |
| `/whitelist status` | Muestra el estado de la whitelist (activa/desactivada, lockdown) | Admin |
| `/whitelist check <player>` | Verifica si un jugador está en la whitelist | Admin, User |

### 5. Flujo de Permisos

```
┌─────────────────────────────────────────────────────┐
│                    ROL: ADMIN                       │
│  ✅ /whitelist add <player>                         │
│  ✅ /whitelist remove <player>                      │
│  ✅ /whitelist list                                 │
│  ✅ /whitelist status                               │
│  ✅ /whitelist check <player>                       │
├─────────────────────────────────────────────────────┤
│                    ROL: USER                        │
│  ✅ /whitelist add <player>                         │
│  ✅ /whitelist check <player>                       │
│  ❌ /whitelist remove (requiere admin)              │
│  ❌ /whitelist list (requiere admin)                │
│  ❌ /whitelist status (requiere admin)              │
├─────────────────────────────────────────────────────┤
│                  SIN ROL ASIGNADO                   │
│  ❌ No puede usar ningún comando                    │
└─────────────────────────────────────────────────────┘
```

### 6. Notificaciones Automáticas

Cuando se realizan acciones en el servidor (vía comandos Minecraft), el bot envía automáticamente notificaciones al canal de Discord configurado:

| Evento | Descripción |
|--------|-------------|
| `whitelist-add` | Se notifica cuando se agrega un jugador |
| `whitelist-remove` | Se notifica cuando se remueve un jugador |
| `whitelist-on` | Se notifica cuando se activa la whitelist |
| `whitelist-off` | Se notifica cuando se desactiva la whitelist |
| `lockdown` | Se notifica cuando se activa lockdown |

Puedes activar/desactivar cada notificación individualmente en `config.yml`.

### 7. Asignación de Rol

Cuando un usuario se agrega a la whitelist (ya sea desde Discord o desde Minecraft), el bot puede asignar automáticamente un rol de Discord al usuario. Para habilitar esto:

1. Configura el ID del rol en `whitelisted-rol` dentro de `config.yml`
2. Asegúrate de que el bot tenga permiso para gestionar roles
3. El rol se asignará automáticamente al usuario que ejecuta el comando `/whitelist add`

### 8. Restricción de Canal

Si configuras `channel-id`, el bot solo responderá a comandos en ese canal específico. Si un usuario intenta usar un comando en otro canal, recibirá un error indicando que debe usar el canal designado.

## Ejemplos de Uso

### Desde Discord

```
/whitelist add Steve           → Agrega a Steve a la whitelist
/whitelist remove Steve        → Remueve a Steve de la whitelist
/whitelist list                → Lista todos los jugadores
/whitelist status              → Muestra estado (activa/desactivada)
/whitelist check Steve         → Verifica si Steve está en la whitelist
```

### Desde Minecraft

```
/swhitelist add Steve          → Agrega a Steve a la whitelist
/swhitelist remove Steve       → Remueve a Steve de la whitelist
/swhitelist list               → Lista todos los jugadores
/swhitelist on                 → Activa la whitelist
/swhitelist off                → Desactiva la whitelist
/swhitelist lockdown 5m        → Activa lockdown por 5 minutos
/swhitelist reload             → Recarga la configuración
```

## Características

- ✅ Base de datos SQLite integrada
- ✅ Sistema de lockdown con countdown
- ✅ Mensajes en formato MiniMessage
- ✅ Sonidos personalizables
- ✅ Múltiples modos de kick
- ✅ Sistema de permisos completo
- ✅ Integración con Discord vía slash commands
- ✅ Notificaciones automáticas en Discord
- ✅ Asignación automática de roles
- ✅ Restricción de canal para comandos