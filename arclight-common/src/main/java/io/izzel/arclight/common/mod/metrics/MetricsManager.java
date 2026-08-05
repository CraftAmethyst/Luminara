package io.izzel.arclight.common.mod.metrics;

import io.izzel.arclight.common.mod.ArclightMod;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MetricsManager {

    private static final int SERVICE_ID = 27601;
    private static Metrics metrics;

    public static void initialize(String serverVersion, File dataFolder) {
        if (metrics != null) {
            return;
        }

        try {
            metrics = new Metrics(serverVersion, SERVICE_ID, dataFolder);

            // single line chart

            // server count
            metrics.addCustomChart(
                new Metrics.SingleLineChart("servers", () -> 1)
            );

            // player count
            metrics.addCustomChart(
                new Metrics.SingleLineChart("players", () ->
                    Bukkit.getOnlinePlayers().size()
                )
            );

            // simple pie

            // cpu core count
            metrics.addCustomChart(
                new Metrics.SimplePie("coreCount", () ->
                    String.valueOf(Runtime.getRuntime().availableProcessors())
                )
            );

            // os architecture
            metrics.addCustomChart(
                new Metrics.SimplePie("osArch", () ->
                    System.getProperty("os.arch")
                )
            );

            // os name
            metrics.addCustomChart(
                new Metrics.SimplePie(
                    "os",
                    () ->
                        System.getProperty("os.name") +
                            " " +
                            System.getProperty("os.version")
                )
            );

            // country
            metrics.addCustomChart(
                new Metrics.SimplePie("location", () -> {
                    try {
                        return getCountryCode();
                    } catch (Exception e) {
                        return "Unknown";
                    }
                })
            );

            // drilldown pie

            // location map
            metrics.addCustomChart(
                new Metrics.DrilldownPie("locationMap", () -> {
                    Map<String, Map<String, Integer>> map = new HashMap<>();
                    try {
                        String country = getCountryCode();
                        Map<String, Integer> entry = new HashMap<>();
                        entry.put(country, 1);
                        map.put(country, entry);
                    } catch (Exception e) {
                        Map<String, Integer> entry = new HashMap<>();
                        entry.put("Unknown", 1);
                        map.put("Unknown", entry);
                    }
                    return map;
                })
            );
        } catch (Exception e) {
            ArclightMod.LOGGER.error("Failed to initialize bStats metrics", e);
        }
    }

    public static void shutdown() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }

    private static String getCountryCode() {
        try {
            URL url = new URL("https://ipapi.co/country/");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Luminara-Metrics/1.0");

            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        connection.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
            ) {
                String country = reader.readLine();
                if (
                    country != null &&
                        !country.isEmpty() &&
                        country.length() <= 3
                ) {
                    return country.trim().toUpperCase();
                }
            }
        } catch (Exception e) {
            ArclightMod.LOGGER.warn("Failed to get country code", e);
        }
        return "Unknown";
    }
}
