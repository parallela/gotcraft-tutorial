package me.lubomirstankov.gotCraftTutorial.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a player starts the tutorial.
 * This event is cancellable - if cancelled, the tutorial will not start.
 */
public class TutorialStartEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private boolean cancelled = false;
    private String cancellationMessage;

    public TutorialStartEvent(Player player) {
        this.player = player;
    }

    /**
     * Gets the player who is starting the tutorial.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Gets the custom cancellation message to send to the player.
     * @return Cancellation message or null
     */
    public String getCancellationMessage() {
        return cancellationMessage;
    }

    /**
     * Sets a custom message to send to the player if the event is cancelled.
     * @param message The message to send
     */
    public void setCancellationMessage(String message) {
        this.cancellationMessage = message;
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

