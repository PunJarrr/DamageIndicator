package dev.punjarrr.damageIndicator.listeners;

import dev.punjarrr.damageIndicator.utils.BlacklistedEntities;
import dev.punjarrr.damageIndicator.utils.TextDisplaySpawner;
import dev.punjarrr.damageIndicator.utils.TextFormatter;
import dev.punjarrr.damageIndicator.utils.TextLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.BoundingBox;

public class HealthChangeListener implements Listener {

    // MONITOR runs after every other plugin has had its say, and ignoreCancelled skips damage and healing that
    // another plugin (or spawn protection, WorldGuard, ...) cancelled, so the numbers match what really happened.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (BlacklistedEntities.isBlacklisted(event.getEntityType())) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        double max = maxHealth(livingEntity);
        double current = Math.max(0, livingEntity.getHealth() - damage);

        Location textLocation = TextLocation.getTextLocation(getTopHitbox(livingEntity));
        Component text = TextFormatter.formatDamage(damage, current, max);

        TextDisplaySpawner.spawn(textLocation, text);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (BlacklistedEntities.isBlacklisted(event.getEntityType())) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double heal = event.getAmount();
        if (heal <= 0) return;

        double max = maxHealth(livingEntity);
        double current = Math.min(max, livingEntity.getHealth() + heal);

        Location textLocation = TextLocation.getTextLocation(getTopHitbox(livingEntity));
        Component text = TextFormatter.formatHeal(heal, current, max);

        TextDisplaySpawner.spawn(textLocation, text);
    }

    /**
     * The max health attribute was renamed in 1.21.3 (GENERIC_MAX_HEALTH to MAX_HEALTH), but this method gives
     * the same value on every version.
     */
    @SuppressWarnings("deprecation")
    private static double maxHealth(LivingEntity entity) {
        return entity.getMaxHealth();
    }

    private Location getTopHitbox(Entity entity) {
        BoundingBox box = entity.getBoundingBox();

        double topY = box.getMaxY();
        double centerX = box.getCenterX();
        double centerZ = box.getCenterZ();

        return new Location(entity.getWorld(), centerX, topY, centerZ);
    }
}
