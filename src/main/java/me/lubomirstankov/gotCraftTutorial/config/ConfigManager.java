package me.lubomirstankov.gotCraftTutorial.config;

import me.lubomirstankov.gotCraftTutorial.GotCraftTutorial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages plugin configuration and provides access to config values.
 */
public class ConfigManager {
    private final GotCraftTutorial plugin;
    private FileConfiguration config;

    public ConfigManager(GotCraftTutorial plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads or reloads the configuration from disk.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Saves the current configuration to disk.
     */
    public void saveConfig() {
        plugin.saveConfig();
    }

    /**
     * Gets the tutorial title from config.
     */
    public String getTutorialTitle() {
        return config.getString("tutorial.title", "&6&lGotCraft Tutorial");
    }

    /**
     * Sets the tutorial title in config.
     */
    public void setTutorialTitle(String title) {
        config.set("tutorial.title", title);
        saveConfig();
    }

    /**
     * Gets all MOTD lines from config.
     */
    public List<String> getMotdLines() {
        return config.getStringList("tutorial.motd");
    }

    /**
     * Sets a specific MOTD line in config.
     */
    public void setMotdLine(int lineNumber, String text) {
        List<String> motdLines = new ArrayList<>(getMotdLines());

        // Expand list if necessary
        while (motdLines.size() <= lineNumber) {
            motdLines.add("");
        }

        motdLines.set(lineNumber, text);
        config.set("tutorial.motd", motdLines);
        saveConfig();
    }

    /**
     * Gets all tutorial points from config.
     */
    public Map<Integer, Location> getTutorialPoints() {
        Map<Integer, Location> points = new HashMap<>();
        ConfigurationSection pointsSection = config.getConfigurationSection("tutorial.points");

        if (pointsSection == null) {
            return points;
        }

        for (String key : pointsSection.getKeys(false)) {
            try {
                int stepNumber = Integer.parseInt(key);
                String worldName = pointsSection.getString(key + ".world");
                double x = pointsSection.getDouble(key + ".x");
                double y = pointsSection.getDouble(key + ".y");
                double z = pointsSection.getDouble(key + ".z");
                float yaw = (float) pointsSection.getDouble(key + ".yaw", 0.0);
                float pitch = (float) pointsSection.getDouble(key + ".pitch", 0.0);

                if (worldName != null && Bukkit.getWorld(worldName) != null) {
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    points.put(stepNumber, location);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid point number in config: " + key);
            }
        }

        return points;
    }

    /**
     * Sets a tutorial point in config.
     */
    public void setTutorialPoint(int stepNumber, Location location) {
        String path = "tutorial.points." + stepNumber;
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        saveConfig();
    }

    /**
     * Gets the step delay in ticks from config.
     */
    public long getStepDelay() {
        return config.getLong("tutorial.step-delay-ticks", 100L);
    }

    /**
     * Gets the cooldown in seconds from config.
     */
    public int getCooldown() {
        return config.getInt("tutorial.cooldown-seconds", 300);
    }

    /**
     * Gets whether to freeze players during tutorial.
     */
    public boolean shouldFreezePlayers() {
        return config.getBoolean("tutorial.freeze-players", true);
    }

    /**
     * Gets whether to block commands during tutorial.
     */
    public boolean shouldBlockCommands() {
        return config.getBoolean("tutorial.block-commands", true);
    }
}

