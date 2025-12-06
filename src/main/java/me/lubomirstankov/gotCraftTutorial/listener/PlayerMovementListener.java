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
        // Check if freeze is enabled in config
        if (!configManager.shouldFreezePlayers()) {
            return;
        }

        // Check if player has active tutorial session
        if (!tutorialManager.hasActiveSession(event.getPlayer())) {
            return;
        }

        // Only cancel if the player actually moved (not just head rotation)
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        // Check if X, Y, or Z coordinates changed (actual movement)
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            // Create new location that keeps player in place but allows head rotation
            Location frozenLocation = from.clone();
            frozenLocation.setYaw(to.getYaw());
            frozenLocation.setPitch(to.getPitch());

            // Cancel the movement event
            event.setCancelled(true);

            // Teleport player back to frozen position with updated view direction
            event.getPlayer().teleport(frozenLocation);
        }
    }
}

