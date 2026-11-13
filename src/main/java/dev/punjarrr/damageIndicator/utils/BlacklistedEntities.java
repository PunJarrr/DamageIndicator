package dev.punjarrr.damageIndicator.utils;

import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BlacklistedEntities {

    /**
     * Always skipped. Only living entities get indicators anyway, so this only needs the living entities that
     * aren't really mobs. Names that don't exist on the running Minecraft version are ignored.
     */
    static final List<String> ALWAYS_BLACKLISTED = List.of(
            "ARMOR_STAND",
            "MANNEQUIN" // 1.21.11+
    );

    // Worked out once per load/reload instead of on every hit
    private static volatile Set<EntityType> blacklisted = EnumSet.noneOf(EntityType.class);

    /** Reads the blacklist from config.yml again (startup and /di reload). */
    public static void reload() {
        blacklisted = parse(ALWAYS_BLACKLISTED, Config.get().getStringList("settings.blacklisted-entities"));
    }

    public static boolean isBlacklisted(EntityType type) {
        return blacklisted.contains(type);
    }

    static Set<EntityType> parse(Collection<String> builtIn, Collection<String> configured) {
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
        addKnown(result, builtIn);
        addKnown(result, configured);
        return result;
    }

    private static void addKnown(Set<EntityType> into, Collection<String> names) {
        for (String name : names) {
            try {
                into.add(EntityType.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Not an entity on this Minecraft version
            }
        }
    }
}
