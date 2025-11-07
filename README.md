# GotCraftTutorial Plugin

A comprehensive, production-ready tutorial system for Minecraft Paper 1.20+ servers with clean architecture and SOLID principles.

## Features

### Event System (API for Other Plugins)
- **TutorialStartEvent** - Cancellable event when a player starts the tutorial
- **TutorialCompleteEvent** - Fired when a player completes the entire tutorial
- **TutorialStopEvent** - Fired when a player stops the tutorial early
- **TutorialStepChangeEvent** - Fired when a player progresses to a new step

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for detailed usage examples.

### Player Features
- **Interactive Tutorial System**: Players are automatically teleported through predefined points
- **Movement Lock**: Players cannot move manually during the tutorial (configurable)
- **Command Protection**: All commands except `/tutorial stop` are blocked during tutorials
- **Cooldown System**: Prevents tutorial spam with configurable cooldown periods
- **Multi-player Support**: Multiple players can run tutorials simultaneously
- **Rich Visuals**: Displays titles and MOTD messages at each step

### Admin Features
- **Dynamic Point Management**: Set tutorial waypoints anywhere in the world
- **Customizable Content**: Configure titles and MOTD messages in-game
- **Live Reload**: Update configuration without server restart
- **Information Dashboard**: View current tutorial configuration

## Commands

### Player Commands
- `/tutorial` - Start the tutorial
- `/tutorial stop` - Stop the tutorial early
- `/guide` - Alias for `/tutorial`

### Admin Commands (Permission: `gotcrafttutorial.admin`)
- `/guideadmin setpoint <number>` - Set a tutorial waypoint at your current location
- `/guideadmin settitle <title>` - Set the tutorial title (supports color codes with &)
- `/guideadmin setmotd <point> <line> <text>` - Set a specific MOTD line for a specific point
- `/guideadmin reload` - Reload configuration from disk
- `/guideadmin info` - Display current tutorial configuration

## Permissions

- `gotcrafttutorial.admin` - Access to all admin commands (default: op)

## Configuration

The `config.yml` file includes:

```yaml
tutorial:
  title: "&6&lWelcome to GotCraft"
  
  motd:
    - "&7Welcome to our server!"
    - "&eFollow the tutorial to get started."
    - "&aUse &f/tutorial stop &ato exit at any time."
  
  points: {}  # Set via /guideadmin setpoint
  # Each point can have its own MOTD:
  # 0:
  #   world: world
  #   x: 0.0
  #   y: 64.0
  #   z: 0.0
  #   yaw: 0.0
  #   pitch: 0.0
  #   motd:
  #     - "&6Welcome to spawn!"
  #     - "&7This is where it all begins."
  
  step-delay-ticks: 100  # 5 seconds between steps
  cooldown-seconds: 300  # 5 minute cooldown
  freeze-players: true   # Prevent manual movement
  block-commands: true   # Block other commands during tutorial
```

## Architecture

This plugin follows clean code and SOLID principles with complete separation of concerns:

### Package Structure
```
me.lubomirstankov.gotCraftTutorial/
├── GotCraftTutorial.java          # Main plugin class (minimal logic)
├── command/                        # Command executors
│   ├── TutorialCommand.java
│   └── GuideAdminCommand.java
├── config/                         # Configuration management
│   └── ConfigManager.java
├── listener/                       # Event listeners
│   ├── CommandBlockListener.java
│   ├── PlayerMovementListener.java
│   └── PlayerQuitListener.java
├── model/                          # Data models
│   ├── TutorialSession.java
│   └── TutorialStep.java
└── service/                        # Business logic
    ├── CommandRegistrationService.java
    ├── ListenerRegistrationService.java
    └── TutorialManager.java
```

### Design Principles Applied

1. **Single Responsibility Principle**: Each class has one clear responsibility
2. **Open/Closed Principle**: Extensible design without modifying core classes
3. **Dependency Injection**: Services receive dependencies through constructors
4. **Separation of Concerns**: Logic, data, and configuration are cleanly separated
5. **Clean Main Class**: No direct logic in `onEnable()` - all delegated to services

## Installation

1. Download or compile the plugin JAR
2. Place in your server's `plugins/` folder
3. Start/restart your server
4. Configure tutorial points using `/guideadmin setpoint <number>`
5. Customize title and MOTD using `/guideadmin` commands

## Building

Requirements:
- Java 21+
- Maven 3.6+

Build command:
```bash
mvn clean package
```

The compiled JAR will be in `target/` directory.

## Usage Example

### Setting Up a Tutorial

1. **Create Tutorial Points**:
   ```
   /guideadmin setpoint 0  (at spawn)
   /guideadmin setpoint 1  (at first location)
   /guideadmin setpoint 2  (at second location)
   /guideadmin setpoint 3  (at final location)
   ```

2. **Customize Messages**:
   ```
   /guideadmin settitle &6&lWelcome to Our Server!
   /guideadmin setmotd 0 &7Thank you for joining!
   /guideadmin setmotd 1 &eThis tutorial will show you around.
   /guideadmin setmotd 2 &aEnjoy your stay!
   ```

3. **Test**:
   ```
   /tutorial
   ```

### For Players

Simply type `/tutorial` to begin. The tutorial will:
- Lock your movement (you can still look around)
- Teleport you through all configured points
- Display information at each step
- Block other commands during the tutorial
- Allow you to exit early with `/tutorial stop`

## Technical Details

- **Thread-Safe**: Uses ConcurrentHashMap for session management
- **Memory Efficient**: Automatic cleanup on player disconnect
- **Scheduler-Based**: Uses Bukkit scheduler for step progression
- **Cooldown Management**: Prevents abuse with configurable cooldowns
- **World-Safe**: Properly stores world information with locations

## Support

For issues, questions, or contributions, please contact the plugin author.

## License

This plugin is provided as-is for use on Paper/Spigot Minecraft servers.

