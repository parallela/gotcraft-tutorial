package me.lubomirstankov.gotCraftTutorial.listener;

import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.service.TutorialManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Blocks commands during tutorial sessions.
 */
public class CommandBlockListener implements Listener {
    private final TutorialManager tutorialManager;
    private final ConfigManager configManager;

    public CommandBlockListener(TutorialManager tutorialManager, ConfigManager configManager) {
        this.tutorialManager = tutorialManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!configManager.shouldBlockCommands()) {
            return;
        }

        if (!tutorialManager.hasActiveSession(event.getPlayer())) {
            return;
        }

        String command = event.getMessage().toLowerCase();

        // Allow tutorial commands
        if (command.startsWith("/tutorial") || command.startsWith("/guide")) {
            return;
        }

        // Block all other commands
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text(configManager.getMessage("command-blocked")));
    }
}

