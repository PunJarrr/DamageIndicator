package dev.punjarrr.damageIndicator.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.TextDisplay;

/**
 * Plain Spigot: turns the text into colour-coded text (hex colours included, so gradients still work), which
 * Spigot's text display and chat methods understand.
 */
final class LegacyTextBridge implements TextBridge {

    static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat() // the §x§r§r§g§g§b§b form Spigot understands
            .build();

    @Override
    public void setText(TextDisplay display, Component text) {
        display.setText(LEGACY.serialize(text));
    }

    @Override
    public void sendMessage(CommandSender to, Component message) {
        to.sendMessage(LEGACY.serialize(message));
    }

    @Override
    public String description() {
        return "colour-coded text (Spigot)";
    }
}
