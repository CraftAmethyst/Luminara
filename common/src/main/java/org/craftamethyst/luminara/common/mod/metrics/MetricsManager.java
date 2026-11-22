package org.craftamethyst.luminara.common.mod.metrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.craftamethyst.luminara.common.bridge.core.server.MinecraftServerBridge;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;
import org.craftamethyst.luminara.internal.bstats.MetricsBase;
import org.craftamethyst.luminara.internal.bstats.charts.SingleLineChart;
import org.craftamethyst.luminara.internal.bstats.json.JsonObjectBuilder;

public final class MetricsManager {

  private static final int SERVICE_ID = 27601;
  private static MetricsBase metrics;

  private MetricsManager() {}

  public static void initialize(String serverVersion, File dataFolder, MinecraftServerBridge server)
      throws IOException, InvalidConfigurationException {
    if (metrics != null) {
      return;
    }

    YamlConfiguration config = loadConfig(new File(dataFolder, "bStats/config.yml"));
    if (!config.getBoolean("enabled", true)) {
      return;
    }

    metrics =
        new MetricsBase(
            "server-implementation",
            config.getString("serverUuid"),
            SERVICE_ID,
            true,
            MetricsManager::appendPlatformData,
            data -> data.appendField("pluginVersion", serverVersion),
            task ->
                server.bridge$queuedProcess(
                    () -> {
                      if (!server.bridge$hasStopped()) {
                        task.run();
                      }
                    }),
            () -> !server.bridge$hasStopped(),
            (message, error) -> LuminaraLog.LOGGER.warn(message, error),
            message -> LuminaraLog.LOGGER.info(message),
            config.getBoolean("logFailedRequests"),
            config.getBoolean("logSentData"),
            config.getBoolean("logResponseStatusText"),
            false);
    metrics.addCustomChart(new SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));
  }

  public static void shutdown() {
    if (metrics != null) {
      metrics.shutdown();
      metrics = null;
    }
  }

  private static YamlConfiguration loadConfig(File file)
      throws IOException, InvalidConfigurationException {
    YamlConfiguration config = new YamlConfiguration();
    if (!Files.notExists(file.toPath())) {
      config.load(file);
    }
    if (!config.isSet("serverUuid")) {
      config.addDefault("serverUuid", UUID.randomUUID().toString());
      config.addDefault("enabled", true);
      config.addDefault("logFailedRequests", false);
      config.addDefault("logSentData", false);
      config.addDefault("logResponseStatusText", false);
      config.options().setHeader(
          List.of(
              "bStats collects basic server statistics for Luminara.",
              "Set enabled to false to opt out.",
              "https://bstats.org/docs/server-owners"));
      config.options().copyDefaults(true);
      config.save(file);
    }
    return config;
  }

  private static void appendPlatformData(JsonObjectBuilder data) {
    data.appendField("playerAmount", Bukkit.getOnlinePlayers().size());
    data.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
    data.appendField("bukkitVersion", Bukkit.getVersion());
    data.appendField("bukkitName", Bukkit.getName());
    data.appendField("javaVersion", System.getProperty("java.version"));
    data.appendField("osName", System.getProperty("os.name"));
    data.appendField("osArch", System.getProperty("os.arch"));
    data.appendField("osVersion", System.getProperty("os.version"));
    data.appendField("coreCount", Runtime.getRuntime().availableProcessors());
  }
}
