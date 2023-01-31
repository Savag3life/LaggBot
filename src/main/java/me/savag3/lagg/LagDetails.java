package me.savag3.lagg;

import lombok.Getter;
import lombok.Setter;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Savag3life
 * @since 1/31/2023
 * @version 1.0
 *
 * Wrapped server details object to easily move and store details related to the dump.
 */
public class LagDetails {

    @Getter @Setter private String sparkURL;
    @Getter private String sparkTPS;
    @Getter private String sparkMSPT;

    private final Spark spark;

    public LagDetails() {
        this.spark = SparkProvider.get();
    }

    public LagDetails(String sparkURL) {
        this.spark = SparkProvider.get();
        this.sparkURL = sparkURL;
        wrapTPS();
        wrapMSPT();
    }

    public void sendDetails() {
        DiscordWebhook.builder()
                .color(Config.DISCORD_EMBED_COLOR)
                .title("Lag Report")
                .description("Server was running below the configured TPS threshold, so we took a spark and collected some details for debugging!\n \nYou can find the [Spark Report Here](" + sparkURL + ")")
                .fields(
                        new DiscordWebhook.Field[] {
                                new DiscordWebhook.Field("TPS Details", sparkTPS, true),
                                new DiscordWebhook.Field("MSPT Details", sparkMSPT, true)
                        }
                )
                .sendTo(Config.DISCORD_WEBHOOK_URL)
                .username(Config.DISCORD_WEBHOOK_SENDER_USERNAME)
                .avatar_url(Config.DISCORD_WEBHOOK_SENDER_AVATAR_URL)
                .build()
                .send();
    }

    // Dirty but ez
    public void wrapTPS() {
        StringBuilder tpsTop = new StringBuilder();
        StringBuilder tpsBottom = new StringBuilder();

        tpsTop.append("`").append(round(spark.tps().poll(StatisticWindow.TicksPerSecond.SECONDS_5), 2)).append("`, ");
        tpsBottom.append("`5s`").append(", ");

        tpsTop.append("`").append(round(spark.tps().poll(StatisticWindow.TicksPerSecond.SECONDS_10), 2)).append("`, ");
        tpsBottom.append("`10s`").append(", ");

        tpsTop.append("`").append(round(spark.tps().poll(StatisticWindow.TicksPerSecond.MINUTES_1), 2)).append("`, ");
        tpsBottom.append("`1m`").append(", ");

        tpsTop.append("`").append(round(spark.tps().poll(StatisticWindow.TicksPerSecond.MINUTES_5), 2)).append("`, ");
        tpsBottom.append("`5m`").append(", ");

        this.sparkTPS = tpsTop.subSequence(0, tpsTop.length() - 2) + "\n" + tpsBottom.subSequence(0, tpsBottom.length() - 2);
    }

    // Dirty but ez
    public void wrapMSPT() {
        StringBuilder msptTop = new StringBuilder();
        StringBuilder msptBottom = new StringBuilder();

        msptTop.append("`").append(round(spark.mspt().poll(StatisticWindow.MillisPerTick.SECONDS_10).max(), 2)).append("`, ");
        msptBottom.append("`10s`").append(", ");

        msptTop.append("`").append(round(spark.mspt().poll(StatisticWindow.MillisPerTick.MINUTES_1).max(), 2)).append("`, ");
        msptBottom.append("`1m`").append(", ");

        this.sparkMSPT =  msptTop.subSequence(0, msptTop.length() - 2) + "\n" + msptBottom.subSequence(0, msptBottom.length() - 2);
    }

    // Taken from Gucci Commons
    private double round(double input, int places) {
        BigDecimal bd = BigDecimal.valueOf(input).setScale(places, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
}

