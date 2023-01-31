## Lagg Bot
A Spigot TPS monitoring bot to collect samplers & lag metrics automatically when a server drops below a configurable TPS value. Lagg Bot utilizes [Spark](https://github.com/lucko/spark) to collect & view timings for Java processes.

### Config
```json
{
  "DISCORD_WEBHOOK_URL": "https://discordapp.com/api/webhooks/1234567890/1234567890",
  "DISCORD_WEBHOOK_SENDER_USERNAME": "Lagg Sampler",
  "DISCORD_WEBHOOK_SENDER_AVATAR_URL": "https://i.imgur.com/1234567890.png",
  "DISCORD_EMBED_COLOR": 16711680,
  "MINUTES_BETWEEN_LAG_SAMPLERS": 5,
  "SLEEP_AFTER_START_FOR_MINUTES": 5,
  "TRIGGER_TPS_THRESHOLD": 15.0,
  "SPARK_SAMPLER_DURATION": 60,
  "FORCE_ASYNC_SPARK_SAMPLER": true,
  "IGNORE_NATIVE_IN_SPARK_SAMPLER": true,
  "IGNORE_SLEEPING_IN_SPARK_SAMPLER": true
}
```

### Hindrance
Spark does not expose specific methods used to start, stop, and get active "samplers", so reflections is used to expose these methods. Because Java 9+ no longer supports `Field#setAccessible()` methods, you must include the JVM argument `--add-opens java.base/java.lang=ALL-UNNAMED` to allow reflections to access these methods.

### Credits
- [Lucko & Contributors](https://github.com/lucko/spark/graphs/contributors)