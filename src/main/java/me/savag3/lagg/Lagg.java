package me.savag3.lagg;

import lombok.Getter;
import me.savag3.lagg.commons.JsonSource;
import me.savag3.lagg.spark.SparkSampler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 */
public class Lagg extends JavaPlugin {

    @Getter private static Lagg instance;

    // Use a timer to stay outside Bukkit thread locks
    private final Timer timer = new Timer();
    @Getter private final JsonSource json = new JsonSource();
    @Getter private final SparkSampler sampler = new SparkSampler();

    @Override
    public void onEnable() {
        instance = this;

        getDataFolder().mkdirs();
        Config.load();

        if (!checkEnabled("Spark")) {
            getLogger().warning("Spark is not enabled! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

//        if (!checkEnabled("ClearLag")) {
//            getLogger().warning("ClearLag is not enabled! Disabling...");
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.scheduleAtFixedRate(new LaggTask(sampler), 0, TimeUnit.MINUTES.toMillis(1));
            }
        }, TimeUnit.MINUTES.toMillis(Config.SLEEP_AFTER_START_FOR_MINUTES));
    }

    private boolean checkEnabled(String plugin) {
        Plugin p = Bukkit.getPluginManager().getPlugin(plugin);
        return p != null && p.isEnabled();
    }
}
