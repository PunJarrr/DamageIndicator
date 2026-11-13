package dev.punjarrr.damageIndicator;

import dev.punjarrr.damageIndicator.commands.DamageIndicatorCommand;
import dev.punjarrr.damageIndicator.listeners.HealthChangeListener;
import dev.punjarrr.damageIndicator.platform.Tasks;
import dev.punjarrr.damageIndicator.platform.TextBridge;
import dev.punjarrr.damageIndicator.utils.BlacklistedEntities;
import dev.punjarrr.damageIndicator.utils.Config;
import dev.punjarrr.damageIndicator.utils.Language;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DamageIndicator extends JavaPlugin {

    private static DamageIndicator instance;

    private TextBridge textBridge;
    private Tasks tasks;

    @Override
    public void onEnable() {
        instance = this;
        Config.load();
        Language.init();
        BlacklistedEntities.reload();

        textBridge = TextBridge.detect();
        tasks = Tasks.detect(this);
        getLogger().info("Showing text with " + textBridge.description() + ", removing it with " + tasks.description());

        getServer().getPluginManager().registerEvents(new HealthChangeListener(), this);
        PluginCommand command = getCommand("damageindicator");
        if (command != null) {
            DamageIndicatorCommand executor = new DamageIndicatorCommand();
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {}

    public static DamageIndicator getInstance() {
        return instance;
    }

    public TextBridge textBridge() {
        return textBridge;
    }

    public Tasks tasks() {
        return tasks;
    }
}
