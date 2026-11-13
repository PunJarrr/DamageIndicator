package dev.punjarrr.damageIndicator.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigMigratorTest {

    private static final Logger LOG = Logger.getLogger("test");

    private static InputStream bundled() {
        return ConfigMigratorTest.class.getResourceAsStream("/config.yml");
    }

    private static String oldDefaultConfig() throws Exception {
        try (InputStream in = ConfigMigratorTest.class.getResourceAsStream("/config-1.0.0.yml")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static boolean migrate(Path dir) {
        return ConfigMigrator.migrateIfNeeded(dir.toFile(), bundled(), LOG);
    }

    private static YamlConfiguration load(Path file) {
        return YamlConfiguration.loadConfiguration(file.toFile());
    }

    private static File[] backups(Path dir) {
        File[] files = dir.resolve("backup").toFile().listFiles();
        return files == null ? new File[0] : files;
    }

    @Test
    void anUnchanged1_0_0ConfigGetsTheNewDefaultsAndComments(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("config.yml"), oldDefaultConfig());

        assertTrue(migrate(dir));

        YamlConfiguration config = load(dir.resolve("config.yml"));
        assertEquals(ConfigMigrator.CURRENT_VERSION, config.getString("config-version"));
        assertEquals(TextFormatter.DEFAULT_DAMAGE, config.getString("settings.text-format.damage"));
        assertEquals(TextFormatter.DEFAULT_HEAL, config.getString("settings.text-format.heal"));
        assertEquals(10, config.getInt("settings.display-duration"));
        assertEquals(List.of("PLAYER"), config.getStringList("settings.blacklisted-entities"));

        String bundledText;
        try (InputStream in = bundled()) {
            bundledText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertEquals(bundledText, Files.readString(dir.resolve("config.yml")),
                "an untouched old config becomes exactly the new default file, comments and all");

        File[] backups = backups(dir);
        assertEquals(1, backups.length, "the old config is backed up");
        assertEquals(oldDefaultConfig(), Files.readString(backups[0].toPath()));
    }

    @Test
    void theServerOwnersOwnSettingsAreKept(@TempDir Path dir) throws Exception {
        String custom = oldDefaultConfig()
                .replace("damage: \"<red>-{damage}❤<aqua>[{current}❤]\"", "damage: \"<gradient:red:gold>-{damage}</gradient>\"")
                .replace("display-duration: 10", "display-duration: 200")
                .replace("number-format: int", "number-format: double")
                .replace("billboard: CENTER", "billboard: VERTICAL")
                .replace("    - PLAYER", "    - PLAYER\n    - VILLAGER")
                .replace("language: en_US", "language: th_TH");
        Files.writeString(dir.resolve("config.yml"), custom);

        assertTrue(migrate(dir));

        YamlConfiguration config = load(dir.resolve("config.yml"));
        assertEquals("<gradient:red:gold>-{damage}</gradient>", config.getString("settings.text-format.damage"));
        assertEquals(TextFormatter.DEFAULT_HEAL, config.getString("settings.text-format.heal"), "unchanged heal text is updated");
        assertEquals(200, config.getInt("settings.display-duration"));
        assertEquals("double", config.getString("settings.number-format"));
        assertEquals("VERTICAL", config.getString("settings.text-display.billboard"));
        assertEquals(List.of("PLAYER", "VILLAGER"), config.getStringList("settings.blacklisted-entities"));
        assertEquals("th_TH", config.getString("settings.language"));
    }

    @Test
    void aConfigWithoutAVersionIsTreatedAs1_0_0(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("config.yml"), "settings:\n  display-duration: 40\n");

        assertTrue(migrate(dir));

        YamlConfiguration config = load(dir.resolve("config.yml"));
        assertEquals(40, config.getInt("settings.display-duration"));
        assertEquals("CENTER", config.getString("settings.text-display.billboard"), "missing settings are added");
        assertEquals(ConfigMigrator.CURRENT_VERSION, config.getString("config-version"));
    }

    @Test
    void aCurrentConfigIsLeftAlone(@TempDir Path dir) throws Exception {
        String current;
        try (InputStream in = bundled()) {
            current = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("display-duration: 10", "display-duration: 30");
        }
        Files.writeString(dir.resolve("config.yml"), current);

        assertFalse(migrate(dir));

        assertEquals(current, Files.readString(dir.resolve("config.yml")));
        assertEquals(0, backups(dir).length);
    }

    @Test
    void aBrokenConfigIsLeftAlone(@TempDir Path dir) throws Exception {
        String broken = "settings:\n  display-duration: 10\n   bad indent: [\n";
        Files.writeString(dir.resolve("config.yml"), broken);

        assertFalse(migrate(dir));

        assertEquals(broken, Files.readString(dir.resolve("config.yml")));
        assertEquals(0, backups(dir).length);
    }

    @Test
    void aConfigFromANewerVersionIsLeftAlone(@TempDir Path dir) throws Exception {
        String newer = "config-version: 9.0.0\nsettings:\n  display-duration: 10\n";
        Files.writeString(dir.resolve("config.yml"), newer);

        assertFalse(migrate(dir));

        assertEquals(newer, Files.readString(dir.resolve("config.yml")));
    }

    @Test
    void noConfigYetMeansNothingToDo(@TempDir Path dir) {
        assertFalse(migrate(dir));
        assertFalse(dir.resolve("config.yml").toFile().exists());
    }

    @Test
    void versionsCompareNumberByNumber() {
        assertTrue(ConfigMigrator.compareVersions("1.0.0", "1.1.0") < 0);
        assertTrue(ConfigMigrator.compareVersions("1.10.0", "1.9.0") > 0);
        assertEquals(0, ConfigMigrator.compareVersions("1.1", "1.1.0"));
    }

    @Test
    void theBundledConfigMatchesTheBuiltInDefaultsAndHasNoProblems() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        try (InputStream in = bundled()) {
            config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        assertEquals(ConfigMigrator.CURRENT_VERSION, config.getString("config-version"));
        assertEquals(TextFormatter.DEFAULT_DAMAGE, config.getString("settings.text-format.damage"));
        assertEquals(TextFormatter.DEFAULT_HEAL, config.getString("settings.text-format.heal"));
        assertEquals(10, config.getInt("settings.display-duration"));
        assertEquals(List.of("PLAYER"), config.getStringList("settings.blacklisted-entities"));
        assertEquals(List.of(), Config.problems(config));
    }

    @Test
    void typosInSettingsAreReported() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("settings.text-display.billboard", "CENTRE");
        config.set("settings.number-format", "decimal");
        assertEquals(2, Config.problems(config).size());

        config.set("settings.text-display.billboard", "horizontal");
        config.set("settings.number-format", "DOUBLE");
        assertEquals(List.of(), Config.problems(config));
    }

    @Test
    void textWithQuotesAndBackslashesSurvives(@TempDir Path dir) throws Exception {
        String tricky = "<red>\\o/ \"ouch\" -{damage}: yes";
        YamlConfiguration old = new YamlConfiguration();
        old.set("config-version", "1.0.0");
        old.set("settings.text-format.damage", tricky);
        old.set("settings.blacklisted-entities", List.of("PLAYER", "true", "no"));
        old.save(dir.resolve("config.yml").toFile());

        assertTrue(migrate(dir));

        YamlConfiguration config = load(dir.resolve("config.yml"));
        assertEquals(tricky, config.getString("settings.text-format.damage"));
        assertEquals(List.of("PLAYER", "true", "no"), config.getStringList("settings.blacklisted-entities"));
    }
}
