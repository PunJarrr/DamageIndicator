package dev.punjarrr.damageIndicator.placeholders;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.utils.Language;
import dev.punjarrr.damageIndicator.utils.PlayerToggle;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

/**
 * DamageIndicator's placeholders, for scoreboards, tab lists, menus and so on:
 * <ul>
 *   <li>{@code %damageindicator_shown%}: true or false, whether the player sees the numbers</li>
 *   <li>{@code %damageindicator_status%}: "ON" or "OFF" (from the messages file, so it can be changed)</li>
 * </ul>
 * Both are empty for players who aren't online.
 */
final class DamageIndicatorExpansion extends PlaceholderExpansion {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§').hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private final DamageIndicator plugin;

    DamageIndicatorExpansion(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "damageindicator";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // stay registered through /papi reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String name = params.toLowerCase(Locale.ROOT);
        if (!name.equals("shown") && !name.equals("status")) return null; // not ours: left as written
        Boolean sees = player == null ? null : PlayerToggle.sees(player.getUniqueId());
        if (sees == null) return "";
        if (name.equals("shown")) return String.valueOf(sees);
        // Messages are MiniMessage; placeholders are read by other plugins, which expect § colour codes
        return LEGACY.serialize(MiniMessage.miniMessage().deserialize(
                Language.get(sees ? "placeholder.status.on" : "placeholder.status.off")));
    }
}
