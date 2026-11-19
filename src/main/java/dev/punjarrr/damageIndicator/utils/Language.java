package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class Language {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static Properties properties = new Properties();

    public static void init() {
        DamageIndicator plugin = DamageIndicator.getInstance();
        String language = Config.getString("settings.language", "en_US");

        File file = resolveFile(plugin, language);
        if (file == null) {
            plugin.getLogger().warning("Language '" + language + "' not found, falling back to en_US.");
            file = resolveFile(plugin, "en_US");
        }
        if (file != null) load(file, bundled(plugin, language));
    }

    /**
     * The messages that came with this version of the plugin. A language file saved by an older version is kept
     * as the server owner left it, so messages added since come from here.
     */
    private static Properties bundled(DamageIndicator plugin, String language) {
        Properties bundled = new Properties();
        for (String name : new String[] {"en_US", language}) {
            try (InputStream in = plugin.getResource("translations/" + name + ".properties")) {
                if (in != null) bundled.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                // Keep what was read so far
            }
        }
        return bundled;
    }

    public static void reload() {
        init();
    }

    private static File resolveFile(DamageIndicator plugin, String language) {
        String resourcePath = "translations/" + language + ".properties";
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            if (plugin.getResource(resourcePath) == null) return null;
            plugin.saveResource(resourcePath, false);
        }
        return file;
    }

    private static void load(File file, Properties defaults) {
        Properties loaded = new Properties(defaults);
        // UTF-8, so messages can use any language's letters and symbols such as ❤
        try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            loaded.load(in);
            properties = loaded;
        } catch (IOException e) {
            DamageIndicator.getInstance().getLogger().warning("Failed to load language file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return properties.getProperty(key, "<red>Missing lang key: " + key);
    }

    public static Component getComponent(String key) {
        return MM.deserialize(get(key));
    }

    public static Component getComponent(String key, Map<String, String> placeholders) {
        return getComponent(key, placeholders, null);
    }

    /** @param viewer who the message is for, so PlaceholderAPI placeholders in it are filled in for them */
    public static Component getComponent(String key, Map<String, String> placeholders, CommandSender viewer) {
        String raw = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace(entry.getKey(), entry.getValue());
        }
        return MM.deserialize(Placeholders.apply(viewer instanceof Player player ? player : null, raw));
    }
}
