package dev.punjarrr.damageIndicator;

import dev.punjarrr.damageIndicator.commands.DamageIndicatorCommand;
import dev.punjarrr.damageIndicator.listeners.HealthChangeListener;
import dev.punjarrr.damageIndicator.utils.Language;
import dev.punjarrr.damageIndicator.utils.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DamageIndicator extends JavaPlugin {

    private static DamageIndicator instance;

    @Override
    public void onEnable() {
        instance = this;
        Config.init();
        Language.init();

        getServer().getPluginManager().registerEvents(new HealthChangeListener(), this);
        DamageIndicatorCommand cmd = new DamageIndicatorCommand();
        Objects.requireNonNull(getCommand("damageindicator")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("damageindicator")).setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {}

    public static DamageIndicator getInstance() {
        return instance;
    }
}
