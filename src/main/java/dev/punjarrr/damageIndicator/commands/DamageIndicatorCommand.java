package dev.punjarrr.damageIndicator.commands;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.utils.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public class DamageIndicatorCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("damageindicator.reload")) {
            sender.sendMessage(Language.getComponent("command.no-permission"));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Language.getComponent("command.unknown-subcommand", Map.of("{command}", label)));
            return true;
        }

        try {
            DamageIndicator.getInstance().reloadConfig();
            Language.reload();
            sender.sendMessage(Language.getComponent("command.reload.success"));
        } catch (Exception e) {
            sender.sendMessage(Language.getComponent("command.reload.failed", Map.of("{error}", e.getMessage())));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
