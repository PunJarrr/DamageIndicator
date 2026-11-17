package dev.punjarrr.damageIndicator.placeholders;

import dev.punjarrr.damageIndicator.DamageIndicator;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlaceholderAPI support, when the server has it: DamageIndicator's own placeholders (see
 * {@link DamageIndicatorExpansion}), and other plugins' placeholders in the damage and heal text and messages.
 *
 * <p>Nothing here touches PlaceholderAPI's classes directly, so the plugin runs fine without it. Those are only
 * in {@link PlaceholderApiHook}, which is loaded only when PlaceholderAPI is installed.
 */
public final class Placeholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("%[^%\\s]+%");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static volatile PlaceholderApiHook hook;

    private Placeholders() {}

    /** Startup: hooks into PlaceholderAPI if it's installed. */
    public static void init(DamageIndicator plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return;
        try {
            PlaceholderApiHook found = new PlaceholderApiHook(plugin);
            hook = found;
            plugin.getLogger().info("Hooked into PlaceholderAPI: added %damageindicator_shown% and "
                    + "%damageindicator_status%, and placeholders work in the text and messages");
        } catch (LinkageError | RuntimeException e) {
            plugin.getLogger().warning("PlaceholderAPI is installed, but DamageIndicator couldn't hook into it: " + e);
        }
    }

    /** On disable (for /reload): removes DamageIndicator's placeholders again. */
    public static void shutdown() {
        PlaceholderApiHook current = hook;
        hook = null;
        if (current != null) current.unregister();
    }

    /**
     * Fills in PlaceholderAPI placeholders in MiniMessage text, for {@code player} (who hit the mob, or who ran
     * the command). Without a player, placeholders that need one are left empty. Without PlaceholderAPI, the
     * text is returned as it is.
     */
    public static String apply(OfflinePlayer player, String text) {
        PlaceholderApiHook current = hook;
        if (current == null || text.indexOf('%') < 0) return text;
        return replace(text, placeholder -> current.resolve(player, placeholder), player == null);
    }

    /**
     * Replaces each %placeholder% with what {@code resolve} gives for it. The values are made safe for
     * MiniMessage (so a nickname can't add clickable text or change the rest of the line) and their colour codes
     * become MiniMessage colours. A placeholder that resolves to nothing, or to itself, is left as written, or
     * left out if {@code emptyIfUnresolved}.
     */
    static String replace(String text, UnaryOperator<String> resolve, boolean emptyIfUnresolved) {
        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String placeholder = matcher.group();
            String value = resolve.apply(placeholder);
            String replacement;
            if (value == null || value.equals(placeholder)) {
                replacement = emptyIfUnresolved ? "" : placeholder;
            } else {
                replacement = legacyToMiniMessage(MINI_MESSAGE.escapeTags(value));
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static final Map<Character, String> LEGACY = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"), Map.entry('9', "blue"),
            Map.entry('a', "green"), Map.entry('b', "aqua"), Map.entry('c', "red"), Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"), Map.entry('f', "white"),
            Map.entry('k', "obfuscated"), Map.entry('l', "bold"), Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"), Map.entry('o', "italic"));

    /**
     * Placeholder values use the old § colour codes, which MiniMessage doesn't read. Turns them into MiniMessage
     * tags: §a into green, §l into bold, §x§1§2§3§4§5§6 into #123456, §r into reset. As in Minecraft, a colour
     * also ends bold, italic and so on.
     */
    static String legacyToMiniMessage(String text) {
        if (text.indexOf('§') < 0) return text;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '§' || i + 1 >= text.length()) {
                out.append(c);
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            if (code == 'x' && isHexSequence(text, i + 2)) {
                StringBuilder hex = new StringBuilder();
                for (int j = 0; j < 6; j++) hex.append(text.charAt(i + 3 + j * 2));
                out.append("<reset><#").append(hex.toString().toLowerCase(Locale.ROOT)).append('>');
                i += 13;
            } else if (code == 'r') {
                out.append("<reset>");
                i++;
            } else if (LEGACY.containsKey(code)) {
                String name = LEGACY.get(code);
                boolean colour = Character.isDigit(code) || (code >= 'a' && code <= 'f');
                out.append(colour ? "<reset><" + name + ">" : "<" + name + ">");
                i++;
            } else {
                i++; // not a code Minecraft knows; drop it, as Minecraft does
            }
        }
        return out.toString();
    }

    /** §x is followed by six §-and-hex-digit pairs. */
    private static boolean isHexSequence(String text, int start) {
        if (start + 12 > text.length()) return false;
        for (int j = 0; j < 6; j++) {
            if (text.charAt(start + j * 2) != '§') return false;
            if (Character.digit(text.charAt(start + j * 2 + 1), 16) < 0) return false;
        }
        return true;
    }
}
