package dev.punjarrr.damageIndicator.listeners;

import dev.punjarrr.damageIndicator.utils.PlayerToggle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Keeps each online player's "show numbers" choice in memory (see {@link PlayerToggle}). */
public class PlayerConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        PlayerToggle.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        PlayerToggle.forget(event.getPlayer());
    }
}
