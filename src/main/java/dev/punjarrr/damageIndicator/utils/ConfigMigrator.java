package dev.punjarrr.damageIndicator.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Updates a config.yml from an older DamageIndicator to the current layout, so server owners get new options
 * and comments without losing their settings.
 *
 * <p>The new file is the bundled config.yml exactly as written (with its comments), with every setting from the
 * old file put in place of the default. A setting the server owner never changed, whose default has since been improved, gets the new
 * default instead (see {@link #CHANGED_DEFAULTS}). The old file is kept in the backup folder.
 */
public final class ConfigMigrator {

    public static final String CURRENT_VERSION = "1.1.0";

    /** Config files without a config-version are treated as this, the first released layout. */
    static final String OLDEST_VERSION = "1.0.0";

    /** A default that changed in {@code version}: configs older than that still holding {@code oldDefault} get the new one. */
    record ChangedDefault(String version, String path, Object oldDefault) {}

    static final List<ChangedDefault> CHANGED_DEFAULTS = List.of(
            // 1.1.0: a space between the amount and the health left
            new ChangedDefault("1.1.0", "settings.text-format.damage", "<red>-{damage}❤<aqua>[{current}❤]"),
            new ChangedDefault("1.1.0", "settings.text-format.heal", "<green>+{heal}❤<aqua>[{current}❤]")
    );

    private ConfigMigrator() {}

    /**
     * Updates {@code dataFolder/config.yml} if it was written by an older version. Does nothing when there is no
     * config yet, when it's already current, or when it can't be read (the server owner's file is never replaced
     * by one we couldn't understand).
     *
     * @return true if the file was updated
     */
    public static boolean migrateIfNeeded(File dataFolder, InputStream bundledConfig, Logger log) {
        File file = new File(dataFolder, "config.yml");
        if (!file.isFile() || bundledConfig == null) return false;

        YamlConfiguration old = new YamlConfiguration();
        try {
            old.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            log.warning("Could not read config.yml to check whether it needs updating, so it was left as it is: "
                    + e.getMessage());
            return false;
        }

        String version = old.getString("config-version", OLDEST_VERSION);
        int comparison = compareVersions(version, CURRENT_VERSION);
        if (comparison == 0) return false;
        if (comparison > 0) {
            log.warning("config.yml is from a newer DamageIndicator (config-version " + version
                    + "). It was left as it is; some settings may not work with this version.");
            return false;
        }

        try {
            String template = new String(bundledConfig.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.loadFromString(template);

            Map<String, Object> values = new LinkedHashMap<>();
            for (String path : old.getKeys(true)) {
                if (old.isConfigurationSection(path) || path.equals("config-version")) continue;
                Object value = old.get(path);
                if (!defaults.contains(path) || defaults.isConfigurationSection(path)) {
                    log.warning("config.yml had a setting DamageIndicator doesn't use, so it was left out: " + path
                            + " (it's still in the backup)");
                    continue;
                }
                if (isUnchangedOldDefault(version, path, value)) continue;
                if (sameValue(value, defaults.get(path))) continue; // already the default, keep the template's line
                values.put(path, value);
            }

            String updated = render(template, values);
            // Check the result before replacing anything: it must read back with exactly the kept values
            YamlConfiguration check = new YamlConfiguration();
            check.loadFromString(updated);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (!sameValue(entry.getValue(), check.get(entry.getKey()))) {
                    throw new IOException("the updated file didn't keep " + entry.getKey());
                }
            }

            File backup = backup(dataFolder, file, version);
            Files.writeString(file.toPath(), updated, StandardCharsets.UTF_8);
            log.info("Updated config.yml from " + version + " to " + CURRENT_VERSION
                    + ". Your settings were kept; the old file is saved as backup/" + backup.getName() + ".");
            return true;
        } catch (IOException | InvalidConfigurationException | RuntimeException e) {
            log.warning("Could not update config.yml, so it was left as it is: " + e.getMessage());
            return false;
        }
    }

    private static final Pattern KEY_LINE = Pattern.compile("^( *)([A-Za-z0-9_-]+):(.*)$");
    private static final Pattern PLAIN_WORD = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    // Words YAML reads as true/false/null instead of text
    private static final List<String> YAML_WORDS = List.of("true", "false", "yes", "no", "on", "off", "y", "n", "null");
    private static final Pattern LIST_ITEM = Pattern.compile("^( *)- .*$");

    /**
     * The bundled config.yml, exactly as written (comments, spacing, quotes), with the values in {@code values}
     * put in place of its defaults. Only "key: value" lines and "- item" lists are changed.
     */
    static String render(String template, Map<String, Object> values) {
        List<String> lines = List.of(template.split("\n", -1));
        List<String> out = new ArrayList<>();
        List<String> path = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher key = KEY_LINE.matcher(line);
            if (line.trim().startsWith("#") || !key.matches()) {
                out.add(line);
                continue;
            }
            int depth = key.group(1).length() / 2;
            while (path.size() > depth) path.remove(path.size() - 1);
            path.add(key.group(2));
            String fullPath = String.join(".", path);
            String rest = key.group(3).trim();

            if (!rest.isEmpty() && !rest.startsWith("#")) {
                // key: value
                out.add(values.containsKey(fullPath)
                        ? key.group(1) + key.group(2) + ": " + scalar(values.get(fullPath))
                        : line);
                path.remove(path.size() - 1);
                continue;
            }

            out.add(line);
            // key: followed by "- item" lines is a list; anything else is a section
            int next = i + 1;
            boolean isList = next < lines.size() && LIST_ITEM.matcher(lines.get(next)).matches();
            if (isList) {
                String itemIndent = key.group(1) + "  ";
                Matcher firstItem = LIST_ITEM.matcher(lines.get(next));
                if (firstItem.matches()) itemIndent = firstItem.group(1);
                while (next < lines.size() && LIST_ITEM.matcher(lines.get(next)).matches()) next++;
                if (values.containsKey(fullPath)) {
                    Object value = values.get(fullPath);
                    for (Object item : value instanceof List<?> list ? list : List.of(value)) {
                        out.add(itemIndent + "- " + scalar(item));
                    }
                } else {
                    out.addAll(lines.subList(i + 1, next));
                }
                i = next - 1;
                path.remove(path.size() - 1);
            }
        }
        return String.join("\n", out);
    }

    /** A YAML value: numbers and true/false as they are, everything else as a double-quoted string. */
    private static String scalar(Object value) {
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        String text = String.valueOf(value);
        if (PLAIN_WORD.matcher(text).matches() && !YAML_WORDS.contains(text.toLowerCase(java.util.Locale.ROOT))) {
            return text; // like CENTER, PIG or en_US
        }
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static boolean sameValue(Object expected, Object actual) {
        if (expected instanceof Number a && actual instanceof Number b) return a.doubleValue() == b.doubleValue();
        if (expected instanceof List<?> a && actual instanceof List<?> b) {
            if (a.size() != b.size()) return false;
            for (int i = 0; i < a.size(); i++) if (!sameValue(a.get(i), b.get(i))) return false;
            return true;
        }
        if (expected instanceof List<?> || actual instanceof List<?>) {
            return String.valueOf(expected).equals(String.valueOf(actual));
        }
        return Objects.equals(String.valueOf(expected), String.valueOf(actual));
    }

    static boolean isUnchangedOldDefault(String version, String path, Object value) {
        for (ChangedDefault changed : CHANGED_DEFAULTS) {
            if (changed.path().equals(path) && compareVersions(version, changed.version()) < 0
                    && Objects.equals(changed.oldDefault(), value)) {
                return true;
            }
        }
        return false;
    }

    private static File backup(File dataFolder, File file, String version) throws IOException {
        File backupFolder = new File(dataFolder, "backup");
        Files.createDirectories(backupFolder.toPath());
        String time = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File backup = new File(backupFolder, "config-" + version + "-" + time + ".yml");
        Files.copy(file.toPath(), backup.toPath());
        return backup;
    }

    /** Compares versions like 1.0.0 and 1.10.2 number by number. Anything that isn't a number counts as 0. */
    static int compareVersions(String a, String b) {
        String[] partsA = a.trim().split("\\.");
        String[] partsB = b.trim().split("\\.");
        for (int i = 0; i < Math.max(partsA.length, partsB.length); i++) {
            int numberA = i < partsA.length ? parse(partsA[i]) : 0;
            int numberB = i < partsB.length ? parse(partsB[i]) : 0;
            if (numberA != numberB) return Integer.compare(numberA, numberB);
        }
        return 0;
    }

    private static int parse(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
