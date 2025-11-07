package me.lubomirstankov.gotCraftTutorial.model;

import org.bukkit.entity.Player;

/**
 * Represents an active tutorial session for a player.
 */
public class TutorialSession {
    private final Player player;
    private int currentStep;
    private final long startTime;

    public TutorialSession(Player player) {
        this.player = player;
        this.currentStep = 0;
        this.startTime = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void nextStep() {
        this.currentStep++;
    }

    public void previousStep() {
        if (this.currentStep > 0) {
            this.currentStep--;
        }
    }

    public long getStartTime() {
        return startTime;
    }
}

