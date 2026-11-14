package dev.punjarrr.damageIndicator.commands;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.utils.BlacklistedEntities;
import dev.punjarrr.damageIndicator.utils.Config;
import dev.punjarrr.damageIndicator.utils.Language;
import net.kyori.adventure.text.Component;
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
            send(sender, Language.getComponent("command.no-permission"));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            send(sender, Language.getComponent("command.unknown-subcommand", Map.of("{command}", label)));
            return true;
        }

        try {
            Config.load();
            Language.reload();
            BlacklistedEntities.reload();
            send(sender, Language.getComponent("command.reload.success"));
        } catch (Exception e) {
            send(sender, Language.getComponent("command.reload.failed", Map.of("{error}", String.valueOf(e.getMessage()))));
        }

        return true;
    }

    private static void send(CommandSender to, Component message) {
        DamageIndicator.getInstance().textBridge().sendMessage(to, message);
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
