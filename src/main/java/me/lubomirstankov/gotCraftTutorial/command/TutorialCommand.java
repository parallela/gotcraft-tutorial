package me.lubomirstankov.gotCraftTutorial.command;

import me.lubomirstankov.gotCraftTutorial.service.TutorialManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /tutorial command for players.
 */
public class TutorialCommand implements CommandExecutor, TabCompleter {
    private final TutorialManager tutorialManager;

    public TutorialCommand(TutorialManager tutorialManager) {
        this.tutorialManager = tutorialManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§cThis command can only be used by players!"));
            return true;
        }

        if (args.length == 0) {
            // Start tutorial
            tutorialManager.startTutorial(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            tutorialManager.stopTutorial(player);
            return true;
        }

        // Invalid subcommand
        player.sendMessage(Component.text("§cUsage: /" + label + " [stop]"));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("stop");
        }

        return completions;
    }
}

