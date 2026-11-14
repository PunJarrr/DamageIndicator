package dev.punjarrr.damageIndicator.utils;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlacklistedEntitiesTest {

    @Test
    void armorStandsAreAlwaysBlacklisted() {
        // MANNEQUIN doesn't exist in the 1.19.4 API we test against, and is skipped
        assertEquals(Set.of(EntityType.ARMOR_STAND),
                BlacklistedEntities.parse(BlacklistedEntities.ALWAYS_BLACKLISTED, List.of()));
    }

    @Test
    void configuredNamesAreAddedAndUnknownOnesIgnored() {
        assertEquals(Set.of(EntityType.ARMOR_STAND, EntityType.PLAYER, EntityType.PIG),
                BlacklistedEntities.parse(BlacklistedEntities.ALWAYS_BLACKLISTED, List.of("PLAYER", " pig ", "NOT_A_MOB")));
    }

    @Test
    void unknownNamesAreReported() {
        assertEquals(List.of("NOT_A_MOB"), BlacklistedEntities.unknown(List.of("PLAYER", "pig", "NOT_A_MOB")));
    }
}
