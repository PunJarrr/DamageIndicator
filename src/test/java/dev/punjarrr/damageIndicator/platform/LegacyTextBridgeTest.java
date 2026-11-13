package dev.punjarrr.damageIndicator.platform;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyTextBridgeTest {

    @Test
    void namedColoursUseSectionCodes() {
        assertEquals("§c-3❤§b[7❤]",
                LegacyTextBridge.LEGACY.serialize(MiniMessage.miniMessage().deserialize("<red>-3❤<aqua>[7❤]")));
    }

    @Test
    void hexColoursUseTheFormatSpigotUnderstands() {
        assertEquals("§x§1§2§3§4§5§6hi",
                LegacyTextBridge.LEGACY.serialize(MiniMessage.miniMessage().deserialize("<#123456>hi")));
    }
}
