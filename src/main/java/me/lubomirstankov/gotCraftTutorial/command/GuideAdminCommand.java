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

import java.util.*;

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
            sender.sendMessage(Component.text(configManager.getMessage("command-no-permission")));
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
            sender.sendMessage(Component.text(configManager.getMessage("command-player-only")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /guideadmin setpoint <number>"));
            return true;
        }

        try {
            int pointNumber = Integer.parseInt(args[1]);

            if (pointNumber < 0) {
                sender.sendMessage(Component.text(configManager.getMessage("admin-point-invalid")));
                return true;
            }

            configManager.setTutorialPoint(pointNumber, player.getLocation());
            tutorialManager.loadTutorialSteps(); // Reload steps

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("number", String.valueOf(pointNumber));
            sender.sendMessage(Component.text(configManager.getMessage("admin-point-set", placeholders)));
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(configManager.getMessage("admin-invalid-number")));
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

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("title", title.replace("&", "§"));
        sender.sendMessage(Component.text(configManager.getMessage("admin-title-set", placeholders)));
        return true;
    }

    private boolean handleSetMotd(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("§cUsage: /guideadmin setmotd <point number> <line number> <text>"));
            return true;
        }

        try {
            int pointNumber = Integer.parseInt(args[1]);
            int lineNumber = Integer.parseInt(args[2]);

            if (pointNumber < 0 || lineNumber < 0) {
                sender.sendMessage(Component.text(configManager.getMessage("admin-point-invalid")));
                return true;
            }

            String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            configManager.setMotdLine(pointNumber, lineNumber, text);
            tutorialManager.loadTutorialSteps(); // Reload steps

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("point", String.valueOf(pointNumber));
            placeholders.put("line", String.valueOf(lineNumber));
            placeholders.put("text", text.replace("&", "§"));
            sender.sendMessage(Component.text(configManager.getMessage("admin-motd-set", placeholders)));
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(configManager.getMessage("admin-invalid-number")));
            return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        configManager.loadConfig();
        tutorialManager.loadTutorialSteps();
        sender.sendMessage(Component.text(configManager.getMessage("admin-config-reloaded")));
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        List<String> infoLines = configManager.getInfoMenu(
            tutorialManager.getStepCount(),
            configManager.getMotdLines().size(),
            configManager.getStepDelay(),
            configManager.getCooldown(),
            configManager.shouldFreezePlayers(),
            configManager.shouldBlockCommands()
        );

        for (String line : infoLines) {
            sender.sendMessage(Component.text(line));
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        List<String> helpLines = configManager.getHelpMenu();
        for (String line : helpLines) {
            sender.sendMessage(Component.text(line));
        }
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
            if (args[0].equalsIgnoreCase("setpoint")) {
                completions.add("<number>");
            } else if (args[0].equalsIgnoreCase("settitle")) {
                completions.add("<title>");
            } else if (args[0].equalsIgnoreCase("setmotd")) {
                completions.add("<point>");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setmotd")) {
            completions.add("<line>");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("setmotd")) {
            completions.add("<text>");
        }

        return completions;
    }
}

