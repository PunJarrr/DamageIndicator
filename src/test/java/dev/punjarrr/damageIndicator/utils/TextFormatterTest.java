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
        assertEquals("-3❤ [7❤]", plain(TextFormatter.format(TextFormatter.DEFAULT_DAMAGE, "int", 1, 3, 0, 7, 10)));
    }

    @Test
    void defaultHealText() {
        assertEquals("+4❤ [10❤]", plain(TextFormatter.format(TextFormatter.DEFAULT_HEAL, "int", 1, 0, 4, 10, 10)));
    }

    @Test
    void maxPlaceholder() {
        assertEquals("7/20", plain(TextFormatter.format("{current}/{max}", "int", 1, 3, 0, 7, 20)));
    }

    @Test
    void intFormatRoundsDownButNeverShowsZeroForATinyHit() {
        assertEquals("5", TextFormatter.formatNumber(5.9, "int", 1));
        assertEquals("1", TextFormatter.formatNumber(0.25, "int", 1));
        assertEquals("0", TextFormatter.formatNumber(0, "int", 1));
    }

    @Test
    void doubleFormatKeepsDecimals() {
        assertEquals("5.25", TextFormatter.formatNumber(5.25, "double", 2));
        assertEquals("5.25", TextFormatter.formatNumber(5.25, "DOUBLE", 2));
    }

    @Test
    void doubleFormatRoundsToConfiguredDecimalPlaces() {
        assertEquals("3.7", TextFormatter.formatNumber(3.69000004, "double", 1));
        assertEquals("6", TextFormatter.formatNumber(5.6, "double", 0));
        assertEquals("0.1", TextFormatter.formatNumber(0.04, "double", 1));
    }

    @Test
    void unknownFormatFallsBackToInt() {
        assertEquals("5", TextFormatter.formatNumber(5.25, "fraction", 2));
    }
}
