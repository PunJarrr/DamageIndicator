package dev.punjarrr.damageIndicator.utils;

import dev.punjarrr.damageIndicator.DamageIndicator;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lets each player turn the numbers on or off for themselves (/di). The choice is saved in the player's own data
 * (their file in the world's playerdata folder), so it stays across logins and restarts with no extra files.
 *
 * <p>Numbers are hidden per player, before they're added to the world, so a player who turned them off is never
 * sent them. Everyone else sees them as usual.
 */
public final class PlayerToggle {

    enum Choice { ON, OFF, NOT_CHOSEN }

    // Online players' choices, read once when they join, so spawning a number never reads player data
    private static final Map<UUID, Choice> ONLINE = new ConcurrentHashMap<>();
    private static volatile boolean showByDefault = true;
    private static NamespacedKey key;

    private PlayerToggle() {}

    /** Startup: reads the setting, and the choices of anyone already online (after /reload). */
    public static void init(DamageIndicator plugin) {
        key = new NamespacedKey(plugin, "show-numbers");
        reload();
        for (Player player : Bukkit.getOnlinePlayers()) load(player);
    }

    /** Reads settings.show-numbers-by-default again (startup and /di reload). Players' own choices are kept. */
    public static void reload() {
        showByDefault = Config.get().getBoolean("settings.show-numbers-by-default", true);
    }

    public static void load(Player player) {
        Byte stored = player.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        ONLINE.put(player.getUniqueId(), stored == null ? Choice.NOT_CHOSEN : stored != 0 ? Choice.ON : Choice.OFF);
    }

    public static void forget(Player player) {
        ONLINE.remove(player.getUniqueId());
    }

    public static boolean sees(Player player) {
        return sees(ONLINE.getOrDefault(player.getUniqueId(), Choice.NOT_CHOSEN), showByDefault);
    }

    static boolean sees(Choice choice, boolean byDefault) {
        return switch (choice) {
            case ON -> true;
            case OFF -> false;
            case NOT_CHOSEN -> byDefault;
        };
    }

    /**
     * Turns the numbers on or off for this player and saves it. The choice is saved even when it matches the
     * default, so a later change to show-numbers-by-default doesn't undo it.
     *
     * @return true if the player now sees the numbers
     */
    public static boolean toggle(Player player) {
        boolean nowSees = !sees(player);
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (nowSees ? 1 : 0));
        ONLINE.put(player.getUniqueId(), nowSees ? Choice.ON : Choice.OFF);
        return nowSees;
    }

    /**
     * Hides a new number from the players who turned numbers off. Call it before the number is added to the
     * world, so those players are never sent it.
     *
     * @return the players it was hidden from, for {@link #forgetHidden}
     */
    public static List<Player> hideFromPlayersWhoTurnedItOff(Entity number) {
        DamageIndicator plugin = DamageIndicator.getInstance();
        List<Player> hiddenFrom = new ArrayList<>();
        for (Map.Entry<UUID, Choice> entry : ONLINE.entrySet()) {
            if (sees(entry.getValue(), showByDefault)) continue;
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !plugin.tasks().ownsHere(player)) continue;
            if (!player.getWorld().equals(number.getWorld())) continue;
            player.hideEntity(plugin, number);
            hiddenFrom.add(player);
        }
        return hiddenFrom;
    }

    /**
     * After the number is removed: the server keeps a list of what each player has hidden, so take it off again,
     * or the list would keep growing until they log out.
     */
    public static void forgetHidden(Entity number, List<Player> hiddenFrom) {
        DamageIndicator plugin = DamageIndicator.getInstance();
        for (Player player : hiddenFrom) {
            if (player.isOnline() && plugin.tasks().ownsHere(player)) player.showEntity(plugin, number);
        }
    }
}
