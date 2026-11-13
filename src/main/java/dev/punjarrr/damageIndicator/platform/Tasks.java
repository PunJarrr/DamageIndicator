package dev.punjarrr.damageIndicator.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Runs a task later for an entity. Folia has no global scheduler, so there each entity's own scheduler is
 * used (found by name at runtime, since the Spigot API we build against doesn't have it).
 */
public interface Tasks {

    void runLater(Entity entity, Runnable task, long delayTicks);

    /** Shown in the startup log, and checked by the smoke test. */
    String description();

    static Tasks detect(Plugin plugin) {
        Tasks folia = FoliaTasks.tryCreate(plugin);
        return folia != null ? folia : new BukkitTasks(plugin);
    }

    final class BukkitTasks implements Tasks {
        private final Plugin plugin;

        BukkitTasks(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void runLater(Entity entity, Runnable task, long delayTicks) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }

        @Override
        public String description() {
            return "the Bukkit scheduler";
        }
    }

    final class FoliaTasks implements Tasks {
        private final Plugin plugin;
        private final Method getScheduler;
        private final Method runDelayed;

        private FoliaTasks(Plugin plugin, Method getScheduler, Method runDelayed) {
            this.plugin = plugin;
            this.getScheduler = getScheduler;
            this.runDelayed = runDelayed;
        }

        static FoliaTasks tryCreate(Plugin plugin) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                Method getScheduler = Entity.class.getMethod("getScheduler");
                Method runDelayed = getScheduler.getReturnType()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
                return new FoliaTasks(plugin, getScheduler, runDelayed);
            } catch (ReflectiveOperationException | LinkageError e) {
                return null; // not Folia
            }
        }

        @Override
        public void runLater(Entity entity, Runnable task, long delayTicks) {
            Consumer<Object> run = scheduledTask -> task.run();
            try {
                // Folia needs a delay of at least 1 tick. The last argument (null) is what to run if the
                // entity is removed first; then there's nothing left to do.
                runDelayed.invoke(getScheduler.invoke(entity), plugin, run, null, Math.max(1L, delayTicks));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not schedule a task with Folia's entity scheduler", e);
            }
        }

        @Override
        public String description() {
            return "Folia's entity scheduler";
        }
    }
}
