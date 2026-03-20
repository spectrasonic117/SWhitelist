# PaperMC Plugin Agent

## Purpose
This file guides code agents in creating and maintaining Minecraft Java 21 plugins under the project's conventions: package architecture, centralized registration, code style, messaging with MiniMessage, and use of existing utilities. It keeps instructions for agents separate from the README, in line with the emerging AGENTS.md standard.

## Project Scope
- Objective: Plugins for Spigot/Paper compatible servers on Minecraft 1.21.x, compiled with Java 21.
- Target Platform: Paper/Spigot API, with Spigot-style listeners and compatibility on Paper.
- Language: Java 21 with Lombok to reduce boilerplate.

## Code Style Rules

- Main.java always be Main file of the project
- Use CommandAPI to create all plugin commands, without exception.
- Use the MessageUtils class and its methods for all messages; the format will be MiniMessage. Avoid direct calls to Adventure APIs outside of this utility.
- Build items exclusively with ItemBuilder.
- Play sounds using the SoundUtils class.
- Use Lombok annotations where appropriate (@Getter, @Setter, @Builder, @RequiredArgsConstructor, etc.) for readability and consistency.
- Java 21 as the minimum version to compile and run.

## Architecture and Organization

## File Tree Project
```
src/main/
    │
    ├── java/com/spectrasonic/
    │       │
    │       ├── {ProjectName}/
    │       │       │
    │       │       ├── Main.java 
    │       │       │
    │       │       ├── commands/
    │       │       │
    │       │       ├── listeners/                 
    │       │       │
    │       │       ├── managers/
    │       │       │       ├── CommandManager.java
    │       │       │       ├── EventManager.java
    │       │       │       ├── MessagesManager.java
    │       │       │       └── ConfigManager.java
    │       │       │
    │       │       ├── enums/
    │       │       │
    │       │       └── config/
    │       │               └── ConfigLoader.java
    │       │
    │       └── Utils
    │               ├── CommandUtils.java
    │               ├── ItemBuilder.java
    │               ├── MessageUtils.java
    │               └── SoundUtils.java             
    └── resources/   
            ├── config.yml
            └── messages.yml     
```

### Commands

- Register all commands in CommandManager.
- Implement each command in the Command package and maintain one class per command.
- Declare CommandAPI literals and arguments in these classes; avoid ad-hoc registrations.

### Events

- Register all events in EventManager.
- Implement each event/handler in the Event package.

### Listeners

- Register all listeners in ListenerManager.
- Implement listeners with the Spigot API (compatible with Paper).

### Configuration

- Register all configurations in ConfigManager.
- Implement configuration classes in the Config package.

### Messages

- All strings in messages.yml; access via MessageManager using keys like "message.messagename", rendered with MiniMessage.

## Message Conventions (MiniMessage)

- Format: MiniMessage for chat, action bars, and titles; use MessageUtils/MessageManager to parse and send.
- YML keys: "message.<name>", e.g., "message.no_permission".
- Use MiniMessage features where applicable: HEX colors, gradients, hover/click events; verify support based on context (e.g., titles have limited interactivity).
- Useful references: MiniMessage documentation and viewer to validate templates.

## Limitations and Protected Utilities
- Do not modify utilities under com.spectrasonic.utils.* or com.spectrasonic.Utils.*; they are considered correct and stable.
- Reuse existing utilities (MessageUtils, ItemBuilder, SoundUtils) instead of duplicating them.

## Research and Documentation
To research dependencies, use “Context7” with official sources:

- SpigotMC/Paper: requirements, startup, and API.
- MiniMessage (Adventure): format, tags, and viewer.
- CommandAPI: check official docs before defining arguments/brigadier.
- Java 21: installation/use in local environments.

## Implementation Best Practices
### Commands:

- Define structure with CommandAPI: root, subcommands, permissions, and typed arguments.
- Encapsulate response messages in MessageUtils; do not concatenate strings manually.

### Events and Listeners:

- Keep classes small and specific to their responsibility; register them in their corresponding Manager.

### Configuration:

- Load by keys; validate input and fallback to default values; do not hardcode strings.

### Manual Testing:

- Verify MiniMessage messages in the viewer before integrating them; validate placeholders.

## “Never Do” Rules
- Do not delete the .git directory.
- Do not print or modify pom.xml unless explicitly requested.
- Do not use block comments /* */; use brief and clear line comments //.
- Do not write Javadocs.
- Do not modify utilities under com.spectrasonic.utils.* or com.spectrasonic.Utils.*.
- Do not compile the project or request to compile it; the user will compile it manually.

## Templates and Quick Examples
- messages.yml key:
    - ```yml
        message:
            key1: "<green>Message 1 String</green>"
            key2: "<yellow>Message 2 String</yellow>"
            key3: "<red>Message 3 String</red>"
      ```

- Sending a message:
    ```java
        MessageUtils.sendMessage(player, messagesManager, "message.no_permission");
    ```

- Command registration in CommandManager:
    - Register during onEnable, delegating to classes in the Command package using CommandAPI.

## Platform Compatibility
- Paper is a drop-in replacement for Spigot; develop against compatible APIs and test on Paper with Java 21.
- For server runtime and local environments, follow Java 21 installation/update guides.

## Recommended Repo Structure
- src/main/java/com/spectrasonic/... with Command, Event, Listener, Config, and message subpackages.
- resources/messages.yml for all user-facing strings.
- Centralized Managers (CommandManager, EventManager, ListenerManager, ConfigManager, MessageManager) registered in the plugin's lifecycle.
- All Enum class will be in Enums package