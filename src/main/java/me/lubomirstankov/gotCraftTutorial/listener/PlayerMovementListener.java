package me.lubomirstankov.gotCraftTutorial.listener;

import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.service.TutorialManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles player movement to prevent manual movement during tutorial.
 */
public class PlayerMovementListener implements Listener {
    private final TutorialManager tutorialManager;
    private final ConfigManager configManager;

    public PlayerMovementListener(TutorialManager tutorialManager, ConfigManager configManager) {
        this.tutorialManager = tutorialManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!configManager.shouldFreezePlayers()) {
            return;
        }

        if (!tutorialManager.hasActiveSession(event.getPlayer())) {
            return;
        }

        // Only cancel if the player actually moved (not just head rotation)
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            // Cancel movement but allow head rotation
            Location newLoc = from.clone();
            newLoc.setYaw(to.getYaw());
            newLoc.setPitch(to.getPitch());
            event.setTo(newLoc);
        }
    }
}

