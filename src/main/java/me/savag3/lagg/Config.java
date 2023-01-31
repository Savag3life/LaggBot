package me.savag3.lagg;

import java.io.File;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 */
public class Config {

    public static transient Config instance = new Config();

    // Webhook Settings
    public static String DISCORD_WEBHOOK_URL = "https://discordapp.com/api/webhooks/1234567890/1234567890";
    public static String DISCORD_WEBHOOK_SENDER_USERNAME = "Lagg Sampler";
    public static String DISCORD_WEBHOOK_SENDER_AVATAR_URL = "https://i.imgur.com/1234567890.png";
    public static int DISCORD_EMBED_COLOR = 0xFF0000;

    // Sampler Settings
    public static int MINUTES_BETWEEN_LAG_SAMPLERS = 5;
    public static int SLEEP_AFTER_START_FOR_MINUTES = 5;
    public static double TRIGGER_TPS_THRESHOLD = 15.0;

    // Spark Sampler Settings
    public static int SPARK_SAMPLER_DURATION = 60;
    public static boolean FORCE_ASYNC_SPARK_SAMPLER = true;
    public static boolean IGNORE_NATIVE_IN_SPARK_SAMPLER = true;
    public static boolean IGNORE_SLEEPING_IN_SPARK_SAMPLER = true;

    public static void save() {
        Lagg.getInstance().getJson().save(instance, new File(Lagg.getInstance().getDataFolder(), "config.yml"));
    }

    public static void load() {
        Lagg.getInstance().getJson().loadOrSaveDefault(instance, new File(Lagg.getInstance().getDataFolder(), "config.yml"), Config.class);
    }

}
