package me.lubomirstankov.gotCraftTutorial.service;

import me.lubomirstankov.gotCraftTutorial.GotCraftTutorial;
import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
import me.lubomirstankov.gotCraftTutorial.listener.CommandBlockListener;
import me.lubomirstankov.gotCraftTutorial.listener.PlayerMovementListener;
import me.lubomirstankov.gotCraftTutorial.listener.PlayerQuitListener;
import org.bukkit.Bukkit;

/**
 * Service responsible for registering all event listeners.
 */
public class ListenerRegistrationService {
    private final GotCraftTutorial plugin;
    private final ConfigManager configManager;
    private final TutorialManager tutorialManager;

    public ListenerRegistrationService(GotCraftTutorial plugin, ConfigManager configManager, TutorialManager tutorialManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.tutorialManager = tutorialManager;
    }

    /**
     * Registers all event listeners with the plugin.
     */
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerMovementListener(tutorialManager, configManager), plugin);
        Bukkit.getPluginManager().registerEvents(new CommandBlockListener(tutorialManager, configManager), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(tutorialManager), plugin);
    }
}

