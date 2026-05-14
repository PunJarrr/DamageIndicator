package dev.punjarrr.damageIndicator.utils;

import org.bukkit.Location;

public class TextLocation {

    public static Location getTextLocation(Location location) {
        double angle = Math.random() * 2 * Math.PI;
        double offsetX = Math.cos(angle) * Config.getDouble("settings.text-location.radius", 0.5);
        double offsetZ = Math.sin(angle) * Config.getDouble("settings.text-location.radius", 0.5);

        return location.clone().add(offsetX, Config.getDouble("settings.text-location.vertical-offset", 0.5), offsetZ);
    }
}
