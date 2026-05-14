package dev.punjarrr.damageIndicator.listeners;

import dev.punjarrr.damageIndicator.utils.BlacklistedEntities;
import dev.punjarrr.damageIndicator.utils.TextDisplaySpawner;
import dev.punjarrr.damageIndicator.utils.TextFormatter;
import dev.punjarrr.damageIndicator.utils.TextLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.BoundingBox;

public class HealthChangeListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (BlacklistedEntities.getBlacklistedEntities().contains(event.getEntityType())) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        double max = livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
        double current = Math.max(0, livingEntity.getHealth() - damage);

        Location textLocation = TextLocation.getTextLocation(getTopHitbox(livingEntity));
        Component text = TextFormatter.formatDamage(damage, current, max);

        TextDisplaySpawner.spawn(textLocation, text);
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (BlacklistedEntities.getBlacklistedEntities().contains(event.getEntityType())) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double heal = event.getAmount();
        if (heal <= 0) return;

        double max = livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
        double current = Math.min(max, livingEntity.getHealth() + heal);

        Location textLocation = TextLocation.getTextLocation(getTopHitbox(livingEntity));
        Component text = TextFormatter.formatHeal(heal, current, max);

        TextDisplaySpawner.spawn(textLocation, text);
    }

    private Location getTopHitbox(Entity entity) {
        BoundingBox box = entity.getBoundingBox();

        double topY = box.getMaxY();
        double centerX = box.getCenterX();
        double centerZ = box.getCenterZ();

        return new Location(entity.getWorld(), centerX, topY, centerZ);
    }
}
