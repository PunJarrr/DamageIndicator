package dev.punjarrr.damageIndicator.placeholders;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlaceholdersTest {

    private static final Map<String, String> VALUES = Map.of(
            "%player_name%", "Steve",
            "%nick%", "<click:run_command:/op me>Evil</click>",
            "%rank%", "§c§lAdmin§r",
            "%hex%", "§x§1§2§a§b§c§dTeal",
            "%empty%", "");

    private static String replace(String text, boolean emptyIfUnresolved) {
        return Placeholders.replace(text, placeholder -> VALUES.getOrDefault(placeholder, placeholder), emptyIfUnresolved);
    }

    private static Component render(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void placeholdersAreFilledInAndTheRestOfTheTextIsKept() {
        assertEquals("<red>-3❤ by Steve", replace("<red>-3❤ by %player_name%", false));
        assertEquals("no placeholders here: 50% off", replace("no placeholders here: 50% off", false));
    }

    @Test
    void unknownPlaceholdersAreLeftAsWrittenOrLeftOutWithoutAPlayer() {
        assertEquals("hit by %unknown_thing%", replace("hit by %unknown_thing%", false));
        assertEquals("hit by ", replace("hit by %unknown_thing%", true));
        assertEquals("[]", replace("[%empty%]", false));
    }

    @Test
    void valuesCantAddMiniMessageTags() {
        Component text = render(replace("<red>%nick%", false));
        assertEquals("<click:run_command:/op me>Evil</click>", plain(text), "shown as typed, not as a clickable command");
        assertNull(text.clickEvent());
        text.children().forEach(child -> assertNull(child.clickEvent()));
    }

    @Test
    void colourCodesInValuesBecomeColours() {
        Component text = render(replace("[%rank%] hit", false));
        assertEquals("[Admin] hit", plain(text));
        Component admin = findText(text, "Admin");
        assertEquals(NamedTextColor.RED, admin.color());
        assertEquals(TextDecoration.State.TRUE, admin.decoration(TextDecoration.BOLD));

        Component teal = findText(render(replace("%hex%", false)), "Teal");
        assertEquals(TextColor.fromHexString("#12abcd"), teal.color());
    }

    @Test
    void legacyCodesAreTranslated() {
        assertEquals("<reset><green>ok", Placeholders.legacyToMiniMessage("§aok"));
        assertEquals("<bold><italic>x<reset>", Placeholders.legacyToMiniMessage("§l§ox§r"));
        assertEquals("<reset><#abcdef>x", Placeholders.legacyToMiniMessage("§x§a§b§c§d§e§fx"));
        assertEquals("plain", Placeholders.legacyToMiniMessage("plain"));
        assertEquals("end§", Placeholders.legacyToMiniMessage("end§"), "a § at the very end is kept");
        assertEquals("x", Placeholders.legacyToMiniMessage("§zx"), "unknown codes are dropped, as in Minecraft");
    }

    @Test
    void withoutPlaceholderApiTextIsUnchanged() {
        assertEquals("%player_name% hit", Placeholders.apply(null, "%player_name% hit"));
    }

    /** The innermost component whose own text is {@code wanted}, with the style it inherits from its parents. */
    private static Component findText(Component root, String wanted) {
        Component found = find(root, wanted, root.style());
        if (found == null) throw new AssertionError("no '" + wanted + "' in " + root);
        return found;
    }

    private static Component find(Component component, String wanted, net.kyori.adventure.text.format.Style inherited) {
        net.kyori.adventure.text.format.Style style = inherited.merge(component.style());
        if (component instanceof net.kyori.adventure.text.TextComponent text && text.content().equals(wanted)) {
            return Component.text(wanted, style);
        }
        for (Component child : component.children()) {
            Component found = find(child, wanted, style);
            if (found != null) return found;
        }
        return null;
    }
}
