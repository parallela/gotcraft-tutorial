package me.lubomirstankov.gotCraftTutorial.model;

import org.bukkit.Location;
import java.util.List;

/**
 * Represents a single step in the tutorial sequence.
 */
public class TutorialStep {
    private final int stepNumber;
    private final Location location;
    private final List<String> motdLines;

    public TutorialStep(int stepNumber, Location location, List<String> motdLines) {
        this.stepNumber = stepNumber;
        this.location = location;
        this.motdLines = motdLines;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Location getLocation() {
        return location;
    }

    public List<String> getMotdLines() {
        return motdLines;
    }
}

