package me.savag3.lagg.spark;

import me.lucko.spark.bukkit.BukkitCommandSender;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.activitylog.Activity;
import me.lucko.spark.common.sampler.Sampler;
import me.lucko.spark.common.sampler.SamplerBuilder;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.sampler.ThreadGrouper;
import me.lucko.spark.common.sampler.node.MergeMode;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.util.MethodDisambiguator;
import me.lucko.spark.lib.adventure.platform.bukkit.BukkitAudiences;
import me.lucko.spark.proto.SparkSamplerProtos;
import me.savag3.lagg.Config;
import me.savag3.lagg.LagDetails;
import me.savag3.lagg.Lagg;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Savag3life
 * @author Lucko & Contributors
 * @since 1/31/2023
 * @version 1.0
 */
public class SparkSampler {

    private static final String SPARK_SAMPLER_MEDIA_TYPE = "application/x-spark-sampler";

    private final Plugin spark;
    private final Field platformField;

    public SparkSampler() {
        this.spark = Bukkit.getPluginManager().getPlugin("Spark");
        Class<?> clazz = this.spark.getClass();
        try {
            this.platformField = clazz.getDeclaredField("platform");
            this.platformField.setAccessible(true);
        } catch (NoSuchFieldException er) {
            System.out.println("Failed to bind to spark! (NoSuchFieldException)");
            throw new RuntimeException(er);
        }
    }

    /**
     * Gets the current running spark sampler
     * @return the spark sampler if it exists (nullable)
     */
    public Optional<Sampler> getActiveSampler() {
        SparkPlatform platform = getPlatform();
        return Optional.ofNullable(platform.getSamplerContainer().getActiveSampler());
    }

    /**
     * Use reflection to get the platform instance from Spark
     * @return the platform instance
     */
    private SparkPlatform getPlatform() {
        try {
            return (SparkPlatform) platformField.get(spark);
        } catch (IllegalAccessException er) {
            System.out.println("Failed to bind to spark! (IllegalAccessException)");
        }
        return null;
    }

    /**
     * Posts the results of a sampler to Bytebin to be displayed on Sparks viewer.
     * Main content from:
     * https://github.com/lucko/spark/blob/d83e49128ad59308f4b3ff19cf4b22b53236be8d/spark-common/src/main/java/me/lucko/spark/common/command/modules/SamplerModule.java
     *
     * @param platform The platform to use (Bukkit)
     * @param sampler The sampler to use
     * @param comment The comment to be used on the Spark viewer
     * @param mergeMode The merge mode to use
     * @return The URL of the Spark viewer
     */
    public String handleUpload(SparkPlatform platform, Sampler sampler, String comment, MergeMode mergeMode) {
        if (platform == null) platform = getPlatform();
        SparkSamplerProtos.SamplerData output = sampler.toProto(platform, new BukkitCommandSender(Bukkit.getConsoleSender(), BukkitAudiences.builder(Bukkit.getPluginManager().getPlugin("LaggBot")).build()), comment, mergeMode, ClassSourceLookup.create(platform));
        try {
            String key = platform.getBytebinClient().postContent(output, SPARK_SAMPLER_MEDIA_TYPE).key();
            String url = platform.getViewerUrl() + key;
            platform.getActivityLog().addToLog(Activity.urlActivity(new BukkitCommandSender(Bukkit.getConsoleSender(), BukkitAudiences.builder(Bukkit.getPluginManager().getPlugin("LaggBot")).build()), System.currentTimeMillis(), "Profiler", url));
            return url;
        } catch (Exception e) {
            System.out.println("Failed to upload to bytebin! (Exception)");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Start a new Spark Sampler
     * Main content from:
     * https://github.com/lucko/spark/blob/d83e49128ad59308f4b3ff19cf4b22b53236be8d/spark-common/src/main/java/me/lucko/spark/common/command/modules/SamplerModule.java
     */
    public void startSampler() {
        Sampler sampler = getPlatform().getSamplerContainer().getActiveSampler();

        if (sampler != null) {
            System.out.println("Sampler already running!");
            return;
        }

        SamplerBuilder builder = new SamplerBuilder();

        builder.forceJavaSampler(Config.FORCE_ASYNC_SPARK_SAMPLER)
                .ignoreNative(Config.IGNORE_NATIVE_IN_SPARK_SAMPLER)
                .ignoreSleeping(Config.IGNORE_SLEEPING_IN_SPARK_SAMPLER)
                .threadDumper(ThreadDumper.ALL)
                .threadGrouper(ThreadGrouper.BY_POOL)
                .completeAfter(Config.SPARK_SAMPLER_DURATION, TimeUnit.SECONDS);

        try {
            sampler = builder.start(getPlatform());
        } catch (Exception e) {
            System.out.println("Failed to start sampler! (Exception)");
            e.printStackTrace();
            return;
        }

        getPlatform().getSamplerContainer().setActiveSampler(sampler);

        sampler.getFuture().whenCompleteAsync((s, throwable) -> {
            if (throwable != null) {
                System.out.println("Failed to complete sampler! (Exception)");
                throwable.printStackTrace();
                return;
            }

            Lagg.getInstance().getLogger().info("Spark sampler completed. Uploading to bytebin...");
            String comment = "LaggBot Sampler";
            MethodDisambiguator methodDisambiguator = new MethodDisambiguator();
            MergeMode mergeMode = MergeMode.separateParentCalls(methodDisambiguator);
            String url = handleUpload(null, s, comment, mergeMode);
            Lagg.getInstance().getLogger().info("Spark sampler uploaded to bytebin. URL: " + url);
            LagDetails details = new LagDetails(url);
            details.sendDetails();
        });
    }

    /**
     * Stop the platforms current sampler & post results
     * Main content from:
     * https://github.com/lucko/spark/blob/d83e49128ad59308f4b3ff19cf4b22b53236be8d/spark-common/src/main/java/me/lucko/spark/common/command/modules/SamplerModule.java
     * @return URL of the sampler results
     */
    public String profilerStop() {
        SparkPlatform platform = getPlatform();
        Sampler sampler = platform.getSamplerContainer().getActiveSampler();

        if (sampler == null) {
            System.out.println("Asked to stop profiler, but it isn't running.");
            return null;
        } else {
            platform.getSamplerContainer().unsetActiveSampler(sampler);
            sampler.stop(false);

            String comment = "LaggBot Sampler";
            MethodDisambiguator methodDisambiguator = new MethodDisambiguator();
            MergeMode mergeMode = MergeMode.separateParentCalls(methodDisambiguator); //: MergeMode.sameMethod(methodDisambiguator);
            String url = handleUpload(platform, sampler, comment, mergeMode);

            // if the previous sampler was running in the background, create a new one
            if (platform.getBackgroundSamplerManager().restartBackgroundSampler()) {
                System.out.println("Restarted background sampler.");
            }

            return url;
        }
    }
}
