package me.lubomirstankov.gotCraftTutorial.service;

import me.lubomirstankov.gotCraftTutorial.GotCraftTutorial;
import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.event.TutorialCompleteEvent;
import me.lubomirstankov.gotCraftTutorial.event.TutorialStartEvent;
import me.lubomirstankov.gotCraftTutorial.event.TutorialStepChangeEvent;
import me.lubomirstankov.gotCraftTutorial.event.TutorialStopEvent;
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
            List<String> motdLines = configManager.getMotdLinesForPoint(stepNumber);
            tutorialSteps.add(new TutorialStep(stepNumber, points.get(stepNumber), motdLines));
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
            player.sendMessage(Component.text(configManager.getMessage("tutorial-already-active")));
            return false;
        }

        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", configManager.formatTime(remaining));
            player.sendMessage(Component.text(configManager.getMessage("tutorial-on-cooldown", placeholders)));
            return false;
        }

        if (tutorialSteps.isEmpty()) {
            player.sendMessage(Component.text(configManager.getMessage("tutorial-no-points")));
            return false;
        }

        // Fire TutorialStartEvent - allow other plugins to cancel
        TutorialStartEvent startEvent = new TutorialStartEvent(player);
        Bukkit.getPluginManager().callEvent(startEvent);

        if (startEvent.isCancelled()) {
            if (startEvent.getCancellationMessage() != null) {
                player.sendMessage(Component.text(startEvent.getCancellationMessage()));
            }
            return false;
        }

        TutorialSession session = new TutorialSession(player);
        activeSessions.put(player.getUniqueId(), session);

        player.sendMessage(Component.text(configManager.getMessage("tutorial-starting")));
        showStep(session, 0);

        return true;
    }

    /**
     * Stops a tutorial session for a player.
     */
    public boolean stopTutorial(Player player) {
        TutorialSession session = activeSessions.remove(player.getUniqueId());

        if (session == null) {
            player.sendMessage(Component.text(configManager.getMessage("tutorial-not-active")));
            return false;
        }

        // Cancel any scheduled tasks
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Fire TutorialStopEvent
        TutorialStopEvent stopEvent = new TutorialStopEvent(
            player,
            session.getStartTime(),
            session.getCurrentStep(),
            tutorialSteps.size()
        );
        Bukkit.getPluginManager().callEvent(stopEvent);

        // Set cooldown
        int cooldownSeconds = configManager.getCooldown();
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));

        player.sendMessage(Component.text(configManager.getMessage("tutorial-stopped")));
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

        int previousStep = session.getCurrentStep();
        session.setCurrentStep(stepIndex);
        TutorialStep step = tutorialSteps.get(stepIndex);

        // Fire TutorialStepChangeEvent
        TutorialStepChangeEvent stepChangeEvent = new TutorialStepChangeEvent(
            player,
            previousStep,
            stepIndex,
            tutorialSteps.size(),
            step.getLocation()
        );
        Bukkit.getPluginManager().callEvent(stepChangeEvent);

        // Teleport player
        player.teleport(step.getLocation());

        // Show title only on first step
        if (stepIndex == 0) {
            String titleText = configManager.getTutorialTitle();
            String subtitleFormat = configManager.getSubtitleFormat();
            String subtitle = subtitleFormat
                .replace("{current}", String.valueOf(stepIndex + 1))
                .replace("{total}", String.valueOf(tutorialSteps.size()))
                .replace("&", "§");

            Component titleComponent = Component.text(titleText.replace("&", "§")).decoration(TextDecoration.BOLD, true);
            Component subtitleComponent = Component.text(subtitle);

            Title title = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(500))
            );
            player.showTitle(title);
        } else {
            // Show only subtitle for subsequent steps
            String subtitleFormat = configManager.getSubtitleFormat();
            String subtitle = subtitleFormat
                .replace("{current}", String.valueOf(stepIndex + 1))
                .replace("{total}", String.valueOf(tutorialSteps.size()))
                .replace("&", "§");

            Component subtitleComponent = Component.text(subtitle);

            Title title = Title.title(
                Component.empty(),
                subtitleComponent,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            player.showTitle(title);
        }

        // Show MOTD from step-specific configuration
        List<String> motdLines = step.getMotdLines();
        if (!motdLines.isEmpty()) {
            // Hello MARU
            for (String line : motdLines) {
                if (line != null && !line.isEmpty()) {
                    player.sendMessage(Component.text(line.replace("&", "§")));
                }
            }
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

        // Fire TutorialCompleteEvent
        TutorialCompleteEvent completeEvent = new TutorialCompleteEvent(
            player,
            session.getStartTime(),
            tutorialSteps.size()
        );
        Bukkit.getPluginManager().callEvent(completeEvent);

        player.sendMessage(Component.text(configManager.getMessage("tutorial-complete-title")));
        player.sendMessage(Component.text(configManager.getMessage("tutorial-complete-subtitle")));

        // Execute completion commands
        List<String> completionCommands = configManager.getCompletionCommands();
        if (!completionCommands.isEmpty()) {
            for (String command : completionCommands) {
                // Replace {player} placeholder with actual player name
                String processedCommand = command.replace("{player}", player.getName());

                // Execute command from console
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                });
            }
        }

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

