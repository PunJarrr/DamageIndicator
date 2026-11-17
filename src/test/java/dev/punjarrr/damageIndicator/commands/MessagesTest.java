package dev.punjarrr.damageIndicator.commands;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessagesTest {

    @Test
    void theBundledEnglishFileHasEveryMessageTheCommandUses() throws Exception {
        Properties messages = new Properties();
        try (InputStream in = MessagesTest.class.getResourceAsStream("/translations/en_US.properties")) {
            messages.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        for (String key : List.of("command.no-permission", "command.usage", "command.players-only",
                "command.toggle.on", "command.toggle.off", "command.reload.success", "command.reload.failed")) {
            assertNotNull(messages.getProperty(key), key);
        }
    }
}
