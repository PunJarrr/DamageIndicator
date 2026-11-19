package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import dev.punjarrr.damageIndicator.platform.EntitySpawner;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class TextDisplaySpawner {

    public static void spawn(Location location, Component text, Entity damagedEntity) {
        DamageIndicator plugin = DamageIndicator.getInstance();
        int duration = Config.getInt("settings.display-duration", 10);
        float scale = (float) Config.getDouble("settings.text-display.scale", 1.0);
        Display.Billboard billboard = resolveBillboard(Config.getString("settings.text-display.billboard", "CENTER"));

        // Everything is set up before the text is added to the world, so players are sent it complete, and
        // players who turned numbers off (/di), or whose own number it is (hide-own-indicators), are never sent
        // it. If a step fails, the text isn't added at all.
        List<Player> hiddenFrom = new ArrayList<>();
        // hide-own-indicators: a player doesn't see the numbers for their own damage and healing
        Player hideFromOwner = damagedEntity instanceof Player owner
                && Config.get().getBoolean("settings.hide-own-indicators", true) ? owner : null;
        TextDisplay display = EntitySpawner.spawn(location, TextDisplay.class, setup -> {
            // Never saved with the world, so a stop or chunk unload before removal can't leave text behind
            setup.setPersistent(false);
            plugin.textBridge().setText(setup, text);
            setup.setBillboard(billboard);
            // Text is centred by default. (Setting it would break on early 1.19.4 builds, where the alignment
            // class was spelled TextAligment.)
            setup.setShadowed(true);
            setup.setDefaultBackground(false);
            setup.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 0, 1)
            ));
            hiddenFrom.addAll(PlayerToggle.hideFromPlayersWhoTurnedItOff(setup));
            if (hideFromOwner != null && !hiddenFrom.contains(hideFromOwner)) {
                hideFromOwner.hideEntity(plugin, setup); // the damaged entity: always this region's own
                hiddenFrom.add(hideFromOwner);
            }
        });

        plugin.tasks().runLater(display, () -> {
            display.remove();
            PlayerToggle.forgetHidden(display, hiddenFrom);
        }, duration);
    }

    static Display.Billboard resolveBillboard(String value) {
        try {
            return Display.Billboard.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Display.Billboard.CENTER; // Config.load() warns about it
        }
    }
}
