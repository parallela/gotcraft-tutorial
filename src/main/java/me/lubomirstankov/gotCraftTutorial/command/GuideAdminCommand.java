package me.lubomirstankov.gotCraftTutorial.command;

import me.lubomirstankov.gotCraftTutorial.config.ConfigManager;
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
import java.util.Arrays;
import java.util.List;

/**
 * Handles the /guideadmin command for administrators.
 */
public class GuideAdminCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "gotcrafttutorial.admin";

    private final ConfigManager configManager;
    private final TutorialManager tutorialManager;

    public GuideAdminCommand(ConfigManager configManager, TutorialManager tutorialManager) {
        this.configManager = configManager;
        this.tutorialManager = tutorialManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "setpoint" -> {
                return handleSetPoint(sender, args);
            }
            case "settitle" -> {
                return handleSetTitle(sender, args);
            }
            case "setmotd" -> {
                return handleSetMotd(sender, args);
            }
            case "reload" -> {
                return handleReload(sender);
            }
            case "info" -> {
                return handleInfo(sender);
            }
            default -> {
                sendHelp(sender, label);
                return true;
            }
        }
    }

    private boolean handleSetPoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§cThis subcommand can only be used by players!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /guideadmin setpoint <number>"));
            return true;
        }

        try {
            int pointNumber = Integer.parseInt(args[1]);

            if (pointNumber < 0) {
                sender.sendMessage(Component.text("§cPoint number must be positive!"));
                return true;
            }

            configManager.setTutorialPoint(pointNumber, player.getLocation());
            tutorialManager.loadTutorialSteps(); // Reload steps

            sender.sendMessage(Component.text("§aSet tutorial point " + pointNumber + " at your current location!"));
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid number: " + args[1]));
            return true;
        }
    }

    private boolean handleSetTitle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /guideadmin settitle <title>"));
            return true;
        }

        String title = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        configManager.setTutorialTitle(title);

        sender.sendMessage(Component.text("§aSet tutorial title to: " + title.replace("&", "§")));
        return true;
    }

    private boolean handleSetMotd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /guideadmin setmotd <line number> <text>"));
            return true;
        }

        try {
            int lineNumber = Integer.parseInt(args[1]);

            if (lineNumber < 0) {
                sender.sendMessage(Component.text("§cLine number must be positive!"));
                return true;
            }

            String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            configManager.setMotdLine(lineNumber, text);

            sender.sendMessage(Component.text("§aSet MOTD line " + lineNumber + " to: " + text.replace("&", "§")));
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid line number: " + args[1]));
            return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        configManager.loadConfig();
        tutorialManager.loadTutorialSteps();
        sender.sendMessage(Component.text("§aConfiguration reloaded!"));
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        sender.sendMessage(Component.text("§7§m--------------------"));
        sender.sendMessage(Component.text("§6§lTutorial Configuration Info"));
        sender.sendMessage(Component.text("§7Title: §f" + configManager.getTutorialTitle().replace("&", "§")));
        sender.sendMessage(Component.text("§7Tutorial Points: §f" + tutorialManager.getStepCount()));
        sender.sendMessage(Component.text("§7MOTD Lines: §f" + configManager.getMotdLines().size()));
        sender.sendMessage(Component.text("§7Step Delay: §f" + configManager.getStepDelay() + " ticks"));
        sender.sendMessage(Component.text("§7Cooldown: §f" + configManager.getCooldown() + " seconds"));
        sender.sendMessage(Component.text("§7Freeze Players: §f" + configManager.shouldFreezePlayers()));
        sender.sendMessage(Component.text("§7Block Commands: §f" + configManager.shouldBlockCommands()));
        sender.sendMessage(Component.text("§7§m--------------------"));
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("§7§m--------------------"));
        sender.sendMessage(Component.text("§6§lGuide Admin Commands"));
        sender.sendMessage(Component.text("§e/" + label + " setpoint <number> §7- Set a tutorial point"));
        sender.sendMessage(Component.text("§e/" + label + " settitle <title> §7- Set the tutorial title"));
        sender.sendMessage(Component.text("§e/" + label + " setmotd <line> <text> §7- Set a MOTD line"));
        sender.sendMessage(Component.text("§e/" + label + " reload §7- Reload configuration"));
        sender.sendMessage(Component.text("§e/" + label + " info §7- Show configuration info"));
        sender.sendMessage(Component.text("§7§m--------------------"));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission(PERMISSION)) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("setpoint", "settitle", "setmotd", "reload", "info"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setpoint") || args[0].equalsIgnoreCase("setmotd")) {
                completions.add("<number>");
            } else if (args[0].equalsIgnoreCase("settitle")) {
                completions.add("<title>");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setmotd")) {
            completions.add("<text>");
        }

        return completions;
    }
}

