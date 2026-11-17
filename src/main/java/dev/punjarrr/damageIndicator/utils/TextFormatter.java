package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    static final String DEFAULT_DAMAGE = "<red>-{damage}❤ <aqua>[{current}❤]";
    static final String DEFAULT_HEAL = "<green>+{heal}❤ <aqua>[{current}❤]";

    /** @param attacker the player who caused the damage, for PlaceholderAPI placeholders, or null */
    public static Component formatDamage(double damage, double current, double max, Player attacker) {
        return format(
                Config.getString("settings.text-format.damage", DEFAULT_DAMAGE),
                Config.getString("settings.number-format", "int"),
                damage, 0, current, max, attacker
        );
    }

    public static Component formatHeal(double heal, double current, double max) {
        return format(
                Config.getString("settings.text-format.heal", DEFAULT_HEAL),
                Config.getString("settings.number-format", "int"),
                0, heal, current, max, null
        );
    }

    static Component format(String raw, String numberFormat, double damage, double heal, double current, double max) {
        return format(raw, numberFormat, damage, heal, current, max, null);
    }

    static Component format(String raw, String numberFormat, double damage, double heal, double current, double max,
                            Player player) {
        String text = raw
                .replace("{damage}", formatNumber(damage, numberFormat))
                .replace("{heal}", formatNumber(heal, numberFormat))
                .replace("{current}", formatNumber(current, numberFormat))
                .replace("{max}", formatNumber(max, numberFormat));
        return MINI_MESSAGE.deserialize(Placeholders.apply(player, text));
    }

    static String formatNumber(double value, String format) {
        if ("double".equalsIgnoreCase(format)) {
            return String.valueOf(value);
        }
        int intVal = (int) value;
        if (intVal == 0 && value > 0) intVal = 1;
        return String.valueOf(intVal);
    }
}
