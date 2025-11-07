package me.lubomirstankov.gotCraftTutorial;

import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.service.CommandRegistrationService;
import me.lubomirstankov.gotCraftTutorial.service.ListenerRegistrationService;
import me.lubomirstankov.gotCraftTutorial.service.TutorialManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for GotCraftTutorial.
 * Delegates all logic to service classes following clean architecture principles.
 */
public final class GotCraftTutorial extends JavaPlugin {

    private ConfigManager configManager;
    private TutorialManager tutorialManager;
    private CommandRegistrationService commandRegistrationService;
    private ListenerRegistrationService listenerRegistrationService;

    @Override
    public void onEnable() {
        // Initialize services
        initializeServices();

        // Register commands and listeners
        commandRegistrationService.registerCommands();
        listenerRegistrationService.registerListeners();

        getLogger().info("GotCraftTutorial has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cleanup active tutorial sessions
        if (tutorialManager != null) {
            tutorialManager.cleanup();
        }

        getLogger().info("GotCraftTutorial has been disabled!");
    }

    /**
     * Initializes all service classes with proper dependency injection.
     */
    private void initializeServices() {
        this.configManager = new ConfigManager(this);
        this.tutorialManager = new TutorialManager(this, configManager);
        this.commandRegistrationService = new CommandRegistrationService(this, configManager, tutorialManager);
        this.listenerRegistrationService = new ListenerRegistrationService(this, configManager, tutorialManager);
    }

    /**
     * Gets the config manager instance.
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the tutorial manager instance.
     * @return TutorialManager instance
     */
    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }
}
