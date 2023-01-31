package me.savag3.lagg;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.common.sampler.Sampler;
import me.savag3.lagg.spark.SparkSampler;

import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 *
 * LaggTask ticks every X configured minutes to check server for TPS & lag.
 */
public class LaggTask extends TimerTask {

    private boolean sleeping = false;
    private long sleepingStart = -1;

    private final Spark spark;
    private final SparkSampler sampler;

    public LaggTask(SparkSampler sampler) {
        super();
        this.spark = SparkProvider.get();
        this.sampler = sampler;
        Lagg.getInstance().getLogger().info("LaggTask started! We are watching TPS & lag for you!");
    }

    @Override
    public void run() {

        if (sleeping) {
            if (sleepingStart + TimeUnit.MINUTES.toMillis(Config.MINUTES_BETWEEN_LAG_SAMPLERS) < System.currentTimeMillis()) {
                sleeping = false;
                this.sleepingStart = -1;
            } else {
                return;
            }
        }

        if (spark.tps().poll(StatisticWindow.TicksPerSecond.SECONDS_5) < Config.TRIGGER_TPS_THRESHOLD) {
            Optional<Sampler> sampler = new SparkSampler().getActiveSampler();
            if (sampler.isPresent()) {

                sleeping = true;
                sleepingStart = System.currentTimeMillis();
                String url = this.sampler.profilerStop();
                LagDetails details = new LagDetails(url);
                details.sendDetails();

            } else {
                this.sleeping = true;
                this.sleepingStart = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Config.SPARK_SAMPLER_DURATION);

                DiscordWebhook.builder()
                        .color(Config.DISCORD_EMBED_COLOR)
                        .content("LaggBot has been activated, but no sampler was found! We'll start one now & post the results here in 30 seconds!")
                        .username(Config.DISCORD_WEBHOOK_SENDER_USERNAME)
                        .avatar_url(Config.DISCORD_WEBHOOK_SENDER_AVATAR_URL)
                        .sendTo(Config.DISCORD_WEBHOOK_URL)
                        .build()
                        .send();

                this.sampler.startSampler();
            }
        }

    }
}
