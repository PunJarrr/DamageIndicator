package dev.punjarrr.damageIndicator.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.TextDisplay;

/**
 * Hands text to the server. The plugin builds every text with its own (bundled) MiniMessage, then a
 * TextBridge passes it on in whatever form the server understands.
 */
public interface TextBridge {

    void setText(TextDisplay display, Component text);

    void sendMessage(CommandSender to, Component message);

    /** Shown in the startup log, and checked by the smoke test. */
    String description();

    /** Paper, Purpur and Folia have Adventure text built in; plain Spigot only has colour-coded text. */
    static TextBridge detect() {
        TextBridge nativeBridge = NativeTextBridge.tryCreate();
        return nativeBridge != null ? nativeBridge : new LegacyTextBridge();
    }
}
