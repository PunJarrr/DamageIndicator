package dev.punjarrr.damageIndicator.commands;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.utils.BlacklistedEntities;
import dev.punjarrr.damageIndicator.utils.Config;
import dev.punjarrr.damageIndicator.utils.Language;
import dev.punjarrr.damageIndicator.utils.PlayerToggle;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /di (or /di toggle): turns the numbers on or off for the player who runs it.
 * /di reload: reloads config.yml and the messages.
 */
public class DamageIndicatorCommand implements CommandExecutor, TabCompleter {

    static final String TOGGLE_PERMISSION = "damageindicator.toggle";
    static final String RELOAD_PERMISSION = "damageindicator.reload";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subcommand = args.length == 0 ? "toggle" : args[0].toLowerCase(Locale.ROOT);
        Map<String, String> placeholders = Map.of("{command}", label);
        switch (subcommand) {
            case "toggle" -> toggle(sender, placeholders);
            case "reload" -> reload(sender);
            default -> send(sender, Language.getComponent("command.usage", placeholders, sender));
        }
        return true;
    }

    private static void toggle(CommandSender sender, Map<String, String> placeholders) {
        if (!(sender instanceof Player player)) {
            send(sender, Language.getComponent("command.players-only", placeholders, sender));
            return;
        }
        if (!sender.hasPermission(TOGGLE_PERMISSION)) {
            send(sender, Language.getComponent("command.no-permission", Map.of(), sender));
            return;
        }
        boolean nowSees = PlayerToggle.toggle(player);
        send(sender, Language.getComponent(nowSees ? "command.toggle.on" : "command.toggle.off", placeholders, sender));
    }

    private static void reload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            send(sender, Language.getComponent("command.no-permission", Map.of(), sender));
            return;
        }
        try {
            Config.load();
            Language.reload();
            BlacklistedEntities.reload();
            PlayerToggle.reload();
            send(sender, Language.getComponent("command.reload.success", Map.of(), sender));
        } catch (Exception e) {
            send(sender, Language.getComponent("command.reload.failed", Map.of("{error}", String.valueOf(e.getMessage())), sender));
        }
    }

    private static void send(CommandSender to, Component message) {
        DamageIndicator.getInstance().textBridge().sendMessage(to, message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return List.of();
        List<String> options = new ArrayList<>();
        if (sender instanceof Player && sender.hasPermission(TOGGLE_PERMISSION)) options.add("toggle");
        if (sender.hasPermission(RELOAD_PERMISSION)) options.add("reload");
        String typed = args[0].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(typed)).toList();
    }
}
