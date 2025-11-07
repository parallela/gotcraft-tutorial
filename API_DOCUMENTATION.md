# GotCraftTutorial API Documentation

## Event System

GotCraftTutorial provides a comprehensive event system that other plugins can hook into to perform custom actions during the tutorial lifecycle.

### Available Events

All events are located in the `me.lubomirstankov.gotCraftTutorial.event` package.

#### 1. TutorialStartEvent (Cancellable)
Fired when a player starts the tutorial.

**Usage Example:**
```java
@EventHandler
public void onTutorialStart(TutorialStartEvent event) {
    Player player = event.getPlayer();
    
    // Cancel the tutorial for specific players
    if (!player.hasPermission("custom.tutorial.use")) {
        event.setCancelled(true);
        event.setCancellationMessage("§cYou don't have permission to use the tutorial!");
        return;
    }
    
    // Log the start
    System.out.println(player.getName() + " started the tutorial");
}
```

**Methods:**
- `Player getPlayer()` - Gets the player starting the tutorial
- `boolean isCancelled()` - Checks if the event is cancelled
- `void setCancelled(boolean)` - Cancels the tutorial start
- `String getCancellationMessage()` - Gets the custom cancellation message
- `void setCancellationMessage(String)` - Sets a custom message to send if cancelled

---

#### 2. TutorialCompleteEvent
Fired when a player successfully completes the entire tutorial.

**Usage Example:**
```java
@EventHandler
public void onTutorialComplete(TutorialCompleteEvent event) {
    Player player = event.getPlayer();
    long duration = event.getDuration();
    int steps = event.getStepsCompleted();
    
    // Reward the player
    player.sendMessage("§aYou completed the tutorial in " + (duration / 1000) + " seconds!");
    
    // Give rewards
    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
    
    // Add permission
    // PermissionsAPI.addPermission(player, "tutorial.completed");
    
    // Log completion
    System.out.println(player.getName() + " completed tutorial (" + steps + " steps in " + duration + "ms)");
}
```

**Methods:**
- `Player getPlayer()` - Gets the player who completed the tutorial
- `long getStartTime()` - Gets when the tutorial was started (milliseconds)
- `long getCompletionTime()` - Gets when the tutorial was completed (milliseconds)
- `long getDuration()` - Gets the total duration (milliseconds)
- `int getStepsCompleted()` - Gets the number of steps completed

---

#### 3. TutorialStopEvent
Fired when a player stops the tutorial early (before completion).

**Usage Example:**
```java
@EventHandler
public void onTutorialStop(TutorialStopEvent event) {
    Player player = event.getPlayer();
    int currentStep = event.getCurrentStep();
    int totalSteps = event.getTotalSteps();
    double progress = event.getProgressPercentage();
    
    // Log early exit
    System.out.println(player.getName() + " stopped tutorial at step " + 
                       currentStep + "/" + totalSteps + " (" + progress + "% complete)");
    
    // Send reminder
    player.sendMessage("§eYou can restart the tutorial anytime with /tutorial");
}
```

**Methods:**
- `Player getPlayer()` - Gets the player who stopped the tutorial
- `long getStartTime()` - Gets when the tutorial was started
- `long getStopTime()` - Gets when the tutorial was stopped
- `long getDuration()` - Gets how long before stopping (milliseconds)
- `int getCurrentStep()` - Gets the step they were on when stopping
- `int getTotalSteps()` - Gets the total number of steps
- `double getProgressPercentage()` - Gets progress percentage (0-100)

---

#### 4. TutorialStepChangeEvent
Fired when a player progresses to a new step in the tutorial.

**Usage Example:**
```java
@EventHandler
public void onTutorialStepChange(TutorialStepChangeEvent event) {
    Player player = event.getPlayer();
    int newStep = event.getNewStep();
    int totalSteps = event.getTotalSteps();
    Location location = event.getNewLocation();
    double progress = event.getProgressPercentage();
    
    // Show progress in action bar
    player.sendActionBar(Component.text("§bProgress: §f" + 
                         String.format("%.1f%%", progress)));
    
    // Perform custom actions at specific steps
    if (newStep == 2) {
        player.sendMessage("§6Special hint: Look at the map!");
    }
    
    // Log step changes
    System.out.println(player.getName() + " reached step " + (newStep + 1) + "/" + totalSteps);
}
```

**Methods:**
- `Player getPlayer()` - Gets the player in the tutorial
- `int getPreviousStep()` - Gets the previous step number (0-based)
- `int getNewStep()` - Gets the new step number (0-based)
- `int getTotalSteps()` - Gets the total number of steps
- `Location getNewLocation()` - Gets the location for the new step
- `double getProgressPercentage()` - Gets progress percentage (0-100)

---

### Complete Plugin Example

Here's a complete example plugin that hooks into all GotCraftTutorial events:

```java
package com.example.tutorialhook;

import me.lubomirstankov.gotCraftTutorial.event.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TutorialHookPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Tutorial Hook Plugin enabled!");
    }

    @EventHandler
    public void onTutorialStart(TutorialStartEvent event) {
        Player player = event.getPlayer();
        
        // Prevent players without permission
        if (!player.hasPermission("custom.tutorial.use")) {
            event.setCancelled(true);
            event.setCancellationMessage("§cYou need permission to access the tutorial!");
            return;
        }
        
        player.sendMessage("§aGood luck with the tutorial!");
    }

    @EventHandler
    public void onTutorialComplete(TutorialCompleteEvent event) {
        Player player = event.getPlayer();
        long seconds = event.getDuration() / 1000;
        
        // Reward completion
        player.getInventory().addItem(
            new ItemStack(Material.DIAMOND, 5),
            new ItemStack(Material.GOLD_INGOT, 10)
        );
        
        player.sendMessage("§6§lRewards: §e5 Diamonds, 10 Gold Ingots");
        player.sendMessage("§7Completion time: " + seconds + " seconds");
        
        // Add to database, give rank, etc.
        getLogger().info(player.getName() + " completed tutorial in " + seconds + "s");
    }

    @EventHandler
    public void onTutorialStop(TutorialStopEvent event) {
        Player player = event.getPlayer();
        double progress = event.getProgressPercentage();
        
        if (progress > 50) {
            player.sendMessage("§eYou were over halfway done! Try finishing next time.");
        }
    }

    @EventHandler
    public void onTutorialStepChange(TutorialStepChangeEvent event) {
        Player player = event.getPlayer();
        int step = event.getNewStep() + 1; // Convert to 1-based
        int total = event.getTotalSteps();
        
        // Send progress
        player.sendActionBar(Component.text(
            "§bStep " + step + " of " + total
        ));
        
        // Special actions at certain steps
        switch (step) {
            case 1 -> player.sendMessage("§6Welcome to the tutorial!");
            case 3 -> player.sendMessage("§6Halfway there!");
            case 5 -> player.sendMessage("§6Almost done!");
        }
    }
}
```

### Maven Dependency

To use the GotCraftTutorial API in your plugin, add it as a dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>me.lubomirstankov</groupId>
        <artifactId>GotCraftTutorial</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

And add to your `plugin.yml`:

```yaml
depend: [GotCraftTutorial]
# or
softdepend: [GotCraftTutorial]
```

---

## Best Practices

1. **Always check if the event is cancelled** (for cancellable events) before performing actions
2. **Use `@EventHandler(priority = EventPriority.MONITOR)` for logging** - this ensures your listener runs last and only if not cancelled
3. **Keep event handlers lightweight** - avoid heavy database operations in event handlers
4. **Handle exceptions** - wrap your code in try-catch to prevent breaking other plugins
5. **Check for null values** - always validate player, location, etc.

---

## Support

For questions about the API or bug reports, please contact the plugin author or open an issue on the project repository.

