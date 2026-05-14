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
        int duration = Config.getInt("settings.display-duration", 10);
        float scale = (float) Config.getDouble("settings.text-display.scale", 1.0);
        Display.Billboard billboard = resolveBillboard(Config.getString("settings.text-display.billboard", "CENTER"));

        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.text(text);
            entity.setBillboard(billboard);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setShadowed(true);
            entity.setDefaultBackground(false);
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 0, 1)
            ));
        });

        DamageIndicator.getInstance().getServer().getScheduler().runTaskLater(
                DamageIndicator.getInstance(),
                display::remove,
                duration
        );
    }

    private static Display.Billboard resolveBillboard(String value) {
        if ("VERTICAL".equalsIgnoreCase(value)) return Display.Billboard.VERTICAL;
        return Display.Billboard.CENTER;
    }
}
