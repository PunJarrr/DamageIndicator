package dev.punjarrr.damageIndicator.utils;

import org.junit.jupiter.api.Test;

import static dev.punjarrr.damageIndicator.utils.PlayerToggle.Choice.NOT_CHOSEN;
import static dev.punjarrr.damageIndicator.utils.PlayerToggle.Choice.OFF;
import static dev.punjarrr.damageIndicator.utils.PlayerToggle.Choice.ON;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerToggleTest {

    @Test
    void aPlayerWhoHasNotChosenGetsTheServerDefault() {
        assertTrue(PlayerToggle.sees(NOT_CHOSEN, true));
        assertFalse(PlayerToggle.sees(NOT_CHOSEN, false));
    }

    @Test
    void aPlayersOwnChoiceWinsOverTheDefault() {
        assertTrue(PlayerToggle.sees(ON, false));
        assertTrue(PlayerToggle.sees(ON, true));
        assertFalse(PlayerToggle.sees(OFF, true));
        assertFalse(PlayerToggle.sees(OFF, false));
    }
}
