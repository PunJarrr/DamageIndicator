package dev.punjarrr.damageIndicator.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component formatDamage(double damage, double current, double max) {
        return format(
                Config.getString("settings.text-format.damage", "<red>-{damage}❤<aqua>[{current}❤]"),
                damage, 0, current, max
        );
    }

    public static Component formatHeal(double heal, double current, double max) {
        return format(
                Config.getString("settings.text-format.heal", "<green>+{heal}❤<aqua>[{current}❤]"),
                0, heal, current, max
        );
    }

    private static Component format(String raw, double damage, double heal, double current, double max) {
        String text = raw
                .replace("{damage}", formatNumber(damage))
                .replace("{heal}", formatNumber(heal))
                .replace("{current}", formatNumber(current))
                .replace("{max}", formatNumber(max));
        return MINI_MESSAGE.deserialize(text);
    }

    private static String formatNumber(double value) {
        String format = Config.getString("settings.number-format", "int");
        if ("double".equalsIgnoreCase(format)) {
            return String.valueOf(value);
        }
        int intVal = (int) value;
        if (intVal == 0 && value > 0) intVal = 1;
        return String.valueOf(intVal);
    }
}
