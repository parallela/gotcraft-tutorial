package me.lubomirstankov.gotCraftTutorial.service;

import me.lubomirstankov.gotCraftTutorial.GotCraftTutorial;
import me.lubomirstankov.gotCraftTutorial.command.GuideAdminCommand;
import me.lubomirstankov.gotCraftTutorial.command.TutorialCommand;
import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import org.bukkit.command.PluginCommand;

/**
 * Service responsible for registering all plugin commands.
 */
public class CommandRegistrationService {
    private final GotCraftTutorial plugin;
    private final ConfigManager configManager;
    private final TutorialManager tutorialManager;

    public CommandRegistrationService(GotCraftTutorial plugin, ConfigManager configManager, TutorialManager tutorialManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.tutorialManager = tutorialManager;
    }

    /**
     * Registers all commands with the plugin.
     */
    public void registerCommands() {
        // Register /tutorial and /guide commands
        TutorialCommand tutorialCommand = new TutorialCommand(tutorialManager);

        PluginCommand tutorialCmd = plugin.getCommand("tutorial");
        if (tutorialCmd != null) {
            tutorialCmd.setExecutor(tutorialCommand);
            tutorialCmd.setTabCompleter(tutorialCommand);
        }

        PluginCommand guideCmd = plugin.getCommand("guide");
        if (guideCmd != null) {
            guideCmd.setExecutor(tutorialCommand);
            guideCmd.setTabCompleter(tutorialCommand);
        }

        // Register /guideadmin command
        GuideAdminCommand guideAdminCommand = new GuideAdminCommand(configManager, tutorialManager);

        PluginCommand guideAdminCmd = plugin.getCommand("guideadmin");
        if (guideAdminCmd != null) {
            guideAdminCmd.setExecutor(guideAdminCommand);
            guideAdminCmd.setTabCompleter(guideAdminCommand);
        }
    }
}

