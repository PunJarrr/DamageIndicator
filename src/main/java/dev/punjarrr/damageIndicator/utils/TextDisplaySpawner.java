package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class TextDisplaySpawner {

    public static void spawn(Location location, Component text) {
        DamageIndicator plugin = DamageIndicator.getInstance();
        int duration = Config.getInt("settings.display-duration", 10);
        float scale = (float) Config.getDouble("settings.text-display.scale", 1.0);
        Display.Billboard billboard = resolveBillboard(Config.getString("settings.text-display.billboard", "CENTER"));

        // Set up right after spawning, in the same tick, so players never see a half-made display.
        // (Spawning with a setup function would need a different method on old and new servers.)
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class);
        // Never saved with the world, so a stop or chunk unload before removal can't leave text behind
        display.setPersistent(false);
        // Scheduled first, so the text is removed even if a setup step below fails
        plugin.tasks().runLater(display, display::remove, duration);

        plugin.textBridge().setText(display, text);
        display.setBillboard(billboard);
        // Text is centred by default. (Setting it would break on early 1.19.4 builds, where the alignment
        // class was spelled TextAligment.)
        display.setShadowed(true);
        display.setDefaultBackground(false);
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(0, 0, 0, 1)
        ));
    }

    static Display.Billboard resolveBillboard(String value) {
        try {
            return Display.Billboard.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Display.Billboard.CENTER; // Config.load() warns about it
        }
    }
}
