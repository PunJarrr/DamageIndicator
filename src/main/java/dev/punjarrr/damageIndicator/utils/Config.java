package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Config {

    static final List<String> BILLBOARDS = List.of("CENTER", "VERTICAL", "HORIZONTAL", "FIXED");
    static final List<String> NUMBER_FORMATS = List.of("int", "double");

    /** Updates an older config.yml if needed, then loads it (startup and /di reload). */
    public static void load() {
        DamageIndicator plugin = DamageIndicator.getInstance();
        try (InputStream bundled = plugin.getResource("config.yml")) {
            ConfigMigrator.migrateIfNeeded(plugin.getDataFolder(), bundled, plugin.getLogger());
        } catch (IOException ignored) {
            // Only closing the bundled file failed
        }
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        for (String problem : problems(get())) plugin.getLogger().warning(problem);
    }

    /** Settings with a value DamageIndicator doesn't understand, explained for the server log. */
    static List<String> problems(ConfigurationSection config) {
        List<String> problems = new ArrayList<>();
        String billboard = config.getString("settings.text-display.billboard", "CENTER");
        if (!BILLBOARDS.contains(billboard.toUpperCase(Locale.ROOT))) {
            problems.add("settings.text-display.billboard is '" + billboard + "', using CENTER. Options: "
                    + String.join(", ", BILLBOARDS));
        }
        String numberFormat = config.getString("settings.number-format", "int");
        if (!NUMBER_FORMATS.contains(numberFormat.toLowerCase(Locale.ROOT))) {
            problems.add("settings.number-format is '" + numberFormat + "', using int. Options: "
                    + String.join(", ", NUMBER_FORMATS));
        }
        return problems;
    }

    public static ConfigurationSection get() {
        return DamageIndicator.getInstance().getConfig();
    }

    public static String getVersion() {
        return getString("config-version");
    }

    public static int getInt(String path) {
        return get().getInt(path);
    }

    public static int getInt(String path, int def) {
        try {
            return get().getInt(path);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean getBoolean(String path) {
        return get().getBoolean(path);
    }

    public static boolean getBoolean(String path, boolean def) {
        try {
            return get().getBoolean(path);
        } catch (Exception e) {
            return def;
        }
    }

    public static String getString(String path) {
        return get().getString(path);
    }

    public static String getString(String path, String def) {
        try {
            return get().getString(path);
        } catch (Exception e) {
            return def;
        }
    }

    public static double getDouble(String path) {
        return get().getDouble(path);
    }

    public static double getDouble(String path, double def) {
        try {
            return get().getDouble(path);
        } catch (Exception e) {
            return def;
        }
    }
}
