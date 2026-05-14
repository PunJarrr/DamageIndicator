package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.List;

public class Config {

    public static void init() {
        DamageIndicator.getInstance().saveDefaultConfig();
        DamageIndicator.getInstance().getConfig().options().copyDefaults(true);
    }

    public static ConfigurationSection get() {
        return DamageIndicator.getInstance().getConfig();
    }

    public static List<EntityType> getBlacklistedEntities() {
        return get().getStringList("settings.blacklisted-entities").stream()
                .flatMap(name -> {
                    try {
                        return java.util.stream.Stream.of(EntityType.valueOf(name));
                    } catch (IllegalArgumentException ignored) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .toList();
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
