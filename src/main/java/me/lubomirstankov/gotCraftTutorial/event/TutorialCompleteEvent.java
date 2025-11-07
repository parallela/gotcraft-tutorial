package me.lubomirstankov.gotCraftTutorial.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a player completes the tutorial.
 * Other plugins can listen to this event to perform actions when a player finishes the tutorial.
 */
public class TutorialCompleteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final long startTime;
    private final long completionTime;
    private final int stepsCompleted;

    public TutorialCompleteEvent(Player player, long startTime, int stepsCompleted) {
        this.player = player;
        this.startTime = startTime;
        this.completionTime = System.currentTimeMillis();
        this.stepsCompleted = stepsCompleted;
    }

    /**
     * Gets the player who completed the tutorial.
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
     * Gets the timestamp when the tutorial was completed.
     * @return Completion time in milliseconds
     */
    public long getCompletionTime() {
        return completionTime;
    }

    /**
     * Gets the duration of the tutorial in milliseconds.
     * @return Duration in milliseconds
     */
    public long getDuration() {
        return completionTime - startTime;
    }

    /**
     * Gets the number of steps completed.
     * @return Number of steps
     */
    public int getStepsCompleted() {
        return stepsCompleted;
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

