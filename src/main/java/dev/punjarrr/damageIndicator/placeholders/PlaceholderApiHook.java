package dev.punjarrr.damageIndicator.placeholders;

import dev.punjarrr.damageIndicator.DamageIndicator;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

/** The only class (with {@link DamageIndicatorExpansion}) that uses PlaceholderAPI's own classes. */
final class PlaceholderApiHook {

    private final DamageIndicatorExpansion expansion;

    PlaceholderApiHook(DamageIndicator plugin) {
        expansion = new DamageIndicatorExpansion(plugin);
        if (!expansion.register()) {
            plugin.getLogger().warning("PlaceholderAPI didn't accept DamageIndicator's placeholders "
                    + "(is another expansion called 'damageindicator' installed?)");
        }
    }

    String resolve(OfflinePlayer player, String placeholder) {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }

    void unregister() {
        expansion.unregister();
    }
}
