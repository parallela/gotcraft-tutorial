package me.lubomirstankov.gotCraftTutorial.model;

import org.bukkit.Location;

/**
 * Represents a single step in the tutorial sequence.
 */
public class TutorialStep {
    private final int stepNumber;
    private final Location location;

    public TutorialStep(int stepNumber, Location location) {
        this.stepNumber = stepNumber;
        this.location = location;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Location getLocation() {
        return location;
    }
}

