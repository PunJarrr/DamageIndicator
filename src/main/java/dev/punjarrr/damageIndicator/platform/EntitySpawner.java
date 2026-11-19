package dev.punjarrr.damageIndicator.platform;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Spawns an entity after running a setup function on it, so it's complete before any player is sent it.
 *
 * <p>The method for this changed: 1.19.4 – 1.20.1 take Bukkit's own Consumer type, 1.20.2 and newer take Java's,
 * and the newest servers no longer have the old one. The new one is found by name at runtime, since the 1.19.4
 * API we build against doesn't have it.
 */
public final class EntitySpawner {

    private static final Method SPAWN_WITH_SETUP = find();

    private EntitySpawner() {}

    private static Method find() {
        try {
            return World.class.getMethod("spawn", Location.class, Class.class, Consumer.class);
        } catch (NoSuchMethodException e) {
            return null; // 1.19.4 – 1.20.1
        }
    }

    public static <T extends Entity> T spawn(Location location, Class<T> type, Consumer<T> setup) {
        World world = location.getWorld();
        if (SPAWN_WITH_SETUP == null) {
            return world.spawn(location, type, setup::accept);
        }
        try {
            return type.cast(SPAWN_WITH_SETUP.invoke(world, location, type, setup));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException("Could not spawn " + type.getSimpleName(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not spawn " + type.getSimpleName(), e);
        }
    }
}
