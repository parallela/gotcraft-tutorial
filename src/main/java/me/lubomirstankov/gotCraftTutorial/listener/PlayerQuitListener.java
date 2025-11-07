package me.lubomirstankov.gotCraftTutorial.listener;

import me.lubomirstankov.gotCraftTutorial.service.TutorialManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles cleanup when players quit during tutorial.
 */
public class PlayerQuitListener implements Listener {
    private final TutorialManager tutorialManager;

    public PlayerQuitListener(TutorialManager tutorialManager) {
        this.tutorialManager = tutorialManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (tutorialManager.hasActiveSession(event.getPlayer())) {
            tutorialManager.stopTutorial(event.getPlayer());
        }
    }
}

