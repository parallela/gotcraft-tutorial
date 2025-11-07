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
     * Gets all MOTD lines from config (deprecated - use getMotdLinesForPoint).
     * @deprecated Use {@link #getMotdLinesForPoint(int)} instead
     */
    @Deprecated
    public List<String> getMotdLines() {
        return config.getStringList("tutorial.motd");
    }

    /**
     * Gets MOTD lines for a specific point.
     */
    public List<String> getMotdLinesForPoint(int pointNumber) {
        List<String> lines = config.getStringList("tutorial.points." + pointNumber + ".motd");

        // Fallback to global MOTD if point-specific MOTD doesn't exist
        if (lines == null || lines.isEmpty()) {
            lines = config.getStringList("tutorial.motd");
        }

        return lines;
    }

    /**
     * Sets a specific MOTD line for a specific point.
     */
    public void setMotdLine(int pointNumber, int lineNumber, String text) {
        String path = "tutorial.points." + pointNumber + ".motd";
        List<String> motdLines = new ArrayList<>(config.getStringList(path));

        // Expand list if necessary
        while (motdLines.size() <= lineNumber) {
            motdLines.add("");
        }

        motdLines.set(lineNumber, text);
        config.set(path, motdLines);
        saveConfig();
    }

    /**
     * Sets all MOTD lines for a specific point.
     */
    public void setMotdForPoint(int pointNumber, List<String> motdLines) {
        config.set("tutorial.points." + pointNumber + ".motd", motdLines);
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
                ConfigurationSection pointSection = pointsSection.getConfigurationSection(key);

                if (pointSection == null) {
                    continue;
                }

                String worldName = pointSection.getString("world");
                double x = pointSection.getDouble("x");
                double y = pointSection.getDouble("y");
                double z = pointSection.getDouble("z");
                float yaw = (float) pointSection.getDouble("yaw", 0.0);
                float pitch = (float) pointSection.getDouble("pitch", 0.0);

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

    /**
     * Gets a message from config with color code support.
     */
    public String getMessage(String key) {
        return config.getString("messages." + key, "").replace("&", "§");
    }

    /**
     * Gets a message with placeholder replacements.
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    /**
     * Gets the subtitle format from config.
     */
    public String getSubtitleFormat() {
        return config.getString("tutorial.subtitle", "&9Step {current} of {total}");
    }

    /**
     * Gets help menu lines.
     */
    public List<String> getHelpMenu() {
        List<String> menu = new ArrayList<>();
        menu.add(getMessage("help.header"));
        menu.add(getMessage("help.setpoint"));
        menu.add(getMessage("help.settitle"));
        menu.add(getMessage("help.setmotd"));
        menu.add(getMessage("help.reload"));
        menu.add(getMessage("help.info"));
        menu.add(getMessage("help.footer"));
        return menu;
    }

    /**
     * Gets info menu lines with placeholders.
     */
    public List<String> getInfoMenu(int pointCount, int motdCount, long stepDelay, int cooldown, boolean freeze, boolean blockCmd) {
        List<String> menu = new ArrayList<>();
        menu.add(getMessage("info.header"));
        menu.add(getMessage("info.title").replace("{title}", getTutorialTitle().replace("&", "§")));
        menu.add(getMessage("info.points").replace("{count}", String.valueOf(pointCount)));
        menu.add(getMessage("info.motd-lines").replace("{count}", String.valueOf(motdCount)));
        menu.add(getMessage("info.step-delay").replace("{ticks}", String.valueOf(stepDelay)));
        menu.add(getMessage("info.cooldown").replace("{seconds}", String.valueOf(cooldown)));
        menu.add(getMessage("info.freeze-players").replace("{value}", String.valueOf(freeze)));
        menu.add(getMessage("info.block-commands").replace("{value}", String.valueOf(blockCmd)));
        menu.add(getMessage("info.separator"));
        menu.add(getMessage("info.note"));
        menu.add(getMessage("info.usage"));
        menu.add(getMessage("info.footer"));
        return menu;
    }
}

