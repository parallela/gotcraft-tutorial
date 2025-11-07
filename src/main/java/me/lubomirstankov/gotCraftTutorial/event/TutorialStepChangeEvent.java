package me.lubomirstankov.gotCraftTutorial.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a player progresses to the next step in the tutorial.
 */
public class TutorialStepChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int previousStep;
    private final int newStep;
    private final int totalSteps;
    private final Location newLocation;

    public TutorialStepChangeEvent(Player player, int previousStep, int newStep, int totalSteps, Location newLocation) {
        this.player = player;
        this.previousStep = previousStep;
        this.newStep = newStep;
        this.totalSteps = totalSteps;
        this.newLocation = newLocation;
    }

    /**
     * Gets the player in the tutorial.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the previous step number.
     * @return Previous step (0-based)
     */
    public int getPreviousStep() {
        return previousStep;
    }

    /**
     * Gets the new step number.
     * @return New step (0-based)
     */
    public int getNewStep() {
        return newStep;
    }

    /**
     * Gets the total number of steps.
     * @return Total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Gets the location for the new step.
     * @return The new location
     */
    public Location getNewLocation() {
        return newLocation;
    }

    /**
     * Gets the progress percentage (0-100).
     * @return Progress percentage
     */
    public double getProgressPercentage() {
        if (totalSteps == 0) return 0.0;
        return ((newStep + 1) / (double) totalSteps) * 100.0;
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

