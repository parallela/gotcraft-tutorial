package me.lubomirstankov.gotCraftTutorial.service;

import me.lubomirstankov.gotCraftTutorial.GotCraftTutorial;
import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.model.TutorialSession;
import me.lubomirstankov.gotCraftTutorial.model.TutorialStep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages tutorial sessions for players.
 */
public class TutorialManager {
    private final GotCraftTutorial plugin;
    private final ConfigManager configManager;
    private final Map<UUID, TutorialSession> activeSessions;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, BukkitTask> activeTasks;
    private List<TutorialStep> tutorialSteps;

    public TutorialManager(GotCraftTutorial plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.activeSessions = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
        loadTutorialSteps();
    }

    /**
     * Loads tutorial steps from configuration.
     */
    public void loadTutorialSteps() {
        tutorialSteps = new ArrayList<>();
        Map<Integer, Location> points = configManager.getTutorialPoints();

        List<Integer> sortedKeys = new ArrayList<>(points.keySet());
        Collections.sort(sortedKeys);

        for (Integer stepNumber : sortedKeys) {
            tutorialSteps.add(new TutorialStep(stepNumber, points.get(stepNumber)));
        }
    }

    /**
     * Checks if a player has an active tutorial session.
     */
    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Checks if a player is on cooldown.
     */
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }

        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    /**
     * Gets remaining cooldown time in seconds.
     */
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Starts a tutorial session for a player.
     */
    public boolean startTutorial(Player player) {
        if (hasActiveSession(player)) {
            player.sendMessage(Component.text("§cYou are already in a tutorial!"));
            return false;
        }

        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage(Component.text("§cYou must wait " + remaining + " seconds before starting another tutorial!"));
            return false;
        }

        if (tutorialSteps.isEmpty()) {
            player.sendMessage(Component.text("§cNo tutorial points have been configured yet!"));
            return false;
        }

        TutorialSession session = new TutorialSession(player);
        activeSessions.put(player.getUniqueId(), session);

        player.sendMessage(Component.text("§aStarting tutorial... Use /tutorial stop to exit."));
        showStep(session, 0);

        return true;
    }

    /**
     * Stops a tutorial session for a player.
     */
    public boolean stopTutorial(Player player) {
        TutorialSession session = activeSessions.remove(player.getUniqueId());

        if (session == null) {
            player.sendMessage(Component.text("§cYou are not in a tutorial!"));
            return false;
        }

        // Cancel any scheduled tasks
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Set cooldown
        int cooldownSeconds = configManager.getCooldown();
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));

        player.sendMessage(Component.text("§aTutorial stopped."));
        player.clearTitle();

        return true;
    }

    /**
     * Shows a specific tutorial step to the player.
     */
    private void showStep(TutorialSession session, int stepIndex) {
        if (stepIndex < 0 || stepIndex >= tutorialSteps.size()) {
            // Tutorial complete
            completeTutorial(session);
            return;
        }

        Player player = session.getPlayer();
        if (!player.isOnline()) {
            stopTutorial(player);
            return;
        }

        session.setCurrentStep(stepIndex);
        TutorialStep step = tutorialSteps.get(stepIndex);

        // Teleport player
        player.teleport(step.getLocation());

        // Show title
        String titleText = configManager.getTutorialTitle();
        String subtitle = "§7Step " + (stepIndex + 1) + " of " + tutorialSteps.size();

        Component titleComponent = Component.text(titleText.replace("&", "§")).decoration(TextDecoration.BOLD, true);
        Component subtitleComponent = Component.text(subtitle);

        Title title = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(500))
        );
        player.showTitle(title);

        // Show MOTD
        List<String> motdLines = configManager.getMotdLines();
        if (!motdLines.isEmpty()) {
            player.sendMessage(Component.text("§7§m--------------------"));
            for (String line : motdLines) {
                if (line != null && !line.isEmpty()) {
                    player.sendMessage(Component.text(line.replace("&", "§")));
                }
            }
            player.sendMessage(Component.text("§7§m--------------------"));
        }

        // Schedule next step
        long delay = configManager.getStepDelay();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (hasActiveSession(player)) {
                showStep(session, stepIndex + 1);
            }
        }, delay);

        activeTasks.put(player.getUniqueId(), task);
    }

    /**
     * Completes the tutorial for a player.
     */
    private void completeTutorial(TutorialSession session) {
        Player player = session.getPlayer();
        activeSessions.remove(player.getUniqueId());
        activeTasks.remove(player.getUniqueId());

        player.sendMessage(Component.text("§a§l✔ Tutorial Complete!"));
        player.sendMessage(Component.text("§7Thank you for completing the tutorial!"));

        // Set cooldown
        int cooldownSeconds = configManager.getCooldown();
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));

        player.clearTitle();
    }

    /**
     * Cleans up all active sessions (for plugin disable).
     */
    public void cleanup() {
        for (UUID uuid : new HashSet<>(activeSessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                stopTutorial(player);
            }
        }

        for (BukkitTask task : activeTasks.values()) {
            task.cancel();
        }

        activeSessions.clear();
        activeTasks.clear();
    }

    /**
     * Gets the current tutorial session for a player.
     */
    public TutorialSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * Gets the number of tutorial steps configured.
     */
    public int getStepCount() {
        return tutorialSteps.size();
    }
}

