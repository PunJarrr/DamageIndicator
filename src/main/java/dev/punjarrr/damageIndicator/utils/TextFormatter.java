package dev.punjarrr.damageIndicator.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    static final String DEFAULT_DAMAGE = "<red>-{damage}❤ <aqua>[{current}❤]";
    static final String DEFAULT_HEAL = "<green>+{heal}❤ <aqua>[{current}❤]";

    public static Component formatDamage(double damage, double current, double max) {
        return format(
                Config.getString("settings.text-format.damage", DEFAULT_DAMAGE),
                Config.getString("settings.number-format", "int"),
                Math.max(0, Config.get().getInt("settings.decimal-places", 1)),
                damage, 0, current, max
        );
    }

    public static Component formatHeal(double heal, double current, double max) {
        return format(
                Config.getString("settings.text-format.heal", DEFAULT_HEAL),
                Config.getString("settings.number-format", "int"),
                Math.max(0, Config.get().getInt("settings.decimal-places", 1)),
                0, heal, current, max
        );
    }

    static Component format(String raw, String numberFormat, int decimalPlaces, double damage, double heal, double current, double max) {
        String text = raw
                .replace("{damage}", formatNumber(damage, numberFormat, decimalPlaces))
                .replace("{heal}", formatNumber(heal, numberFormat, decimalPlaces))
                .replace("{current}", formatNumber(current, numberFormat, decimalPlaces))
                .replace("{max}", formatNumber(max, numberFormat, decimalPlaces));
        return MINI_MESSAGE.deserialize(text);
    }

    static String formatNumber(double value, String format, int decimalPlaces) {
        if ("double".equalsIgnoreCase(format)) {
            double smallestShown = Math.pow(10, -decimalPlaces);
            if (value > 0 && value < smallestShown) value = smallestShown;
            double scale = Math.pow(10, decimalPlaces);
            double rounded = Math.round(value * scale) / scale;
            if (rounded == (int) rounded) {
                return String.valueOf((int) rounded);
            }
            return String.valueOf(rounded);
        }
        int intVal = (int) value;
        if (intVal == 0 && value > 0) intVal = 1;
        return String.valueOf(intVal);
    }
}
