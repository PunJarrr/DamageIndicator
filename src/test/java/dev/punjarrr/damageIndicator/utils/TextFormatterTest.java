package dev.punjarrr.damageIndicator.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFormatterTest {

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void defaultDamageText() {
        assertEquals("-3❤[7❤]", plain(TextFormatter.format(TextFormatter.DEFAULT_DAMAGE, "int", 3, 0, 7, 10)));
    }

    @Test
    void defaultHealText() {
        assertEquals("+4❤[10❤]", plain(TextFormatter.format(TextFormatter.DEFAULT_HEAL, "int", 0, 4, 10, 10)));
    }

    @Test
    void maxPlaceholder() {
        assertEquals("7/20", plain(TextFormatter.format("{current}/{max}", "int", 3, 0, 7, 20)));
    }

    @Test
    void intFormatRoundsDownButNeverShowsZeroForATinyHit() {
        assertEquals("5", TextFormatter.formatNumber(5.9, "int"));
        assertEquals("1", TextFormatter.formatNumber(0.25, "int"));
        assertEquals("0", TextFormatter.formatNumber(0, "int"));
    }

    @Test
    void doubleFormatKeepsDecimals() {
        assertEquals("5.25", TextFormatter.formatNumber(5.25, "double"));
        assertEquals("5.25", TextFormatter.formatNumber(5.25, "DOUBLE"));
    }

    @Test
    void unknownFormatFallsBackToInt() {
        assertEquals("5", TextFormatter.formatNumber(5.25, "fraction"));
    }
}
