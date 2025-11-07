package me.lubomirstankov.gotCraftTutorial.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a player stops the tutorial early (before completion).
 */
public class TutorialStopEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final long startTime;
    private final long stopTime;
    private final int currentStep;
    private final int totalSteps;

    public TutorialStopEvent(Player player, long startTime, int currentStep, int totalSteps) {
        this.player = player;
        this.startTime = startTime;
        this.stopTime = System.currentTimeMillis();
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    /**
     * Gets the player who stopped the tutorial.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the timestamp when the tutorial was started.
     * @return Start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the timestamp when the tutorial was stopped.
     * @return Stop time in milliseconds
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * Gets the duration before stopping in milliseconds.
     * @return Duration in milliseconds
     */
    public long getDuration() {
        return stopTime - startTime;
    }

    /**
     * Gets the step the player was on when they stopped.
     * @return Current step number
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Gets the total number of steps in the tutorial.
     * @return Total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Gets the progress percentage (0-100).
     * @return Progress percentage
     */
    public double getProgressPercentage() {
        if (totalSteps == 0) return 0.0;
        return (currentStep / (double) totalSteps) * 100.0;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

