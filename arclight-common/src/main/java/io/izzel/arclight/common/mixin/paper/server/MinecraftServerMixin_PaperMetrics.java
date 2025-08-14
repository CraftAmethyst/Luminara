package io.izzel.arclight.common.mixin.paper.server;

import com.destroystokyo.paper.Metrics;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to automatically initialize Paper Metrics system.
 * This provides seamless bStats integration for Paper compatibility.
 */
@Mixin(value = MinecraftServer.class, remap = false)
public class MinecraftServerMixin_PaperMetrics {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperMetrics");
    private Metrics arclight$paperMetrics;

    /**
     * Initialize Paper Metrics during server startup.
     */
    @Inject(method = "runServer", at = @At("HEAD"))
    private void arclight$initializePaperMetrics(CallbackInfo ci) {
        try {
            // Generate a server UUID for metrics (in production, this should be persistent)
            String serverUUID = UUID.randomUUID().toString();

            // Initialize Paper Metrics
            this.arclight$paperMetrics = new Metrics(
                    "Arclight-Paper",
                    serverUUID,
                    true,
                    java.util.logging.Logger.getLogger("PaperMetrics")
            );

            // Add some basic charts
            this.arclight$addBasicMetricsCharts();

            LOGGER.info("Initialized Paper Metrics system");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Paper Metrics: " + e.getMessage(), e);
        }
    }

    /**
     * Add basic metrics charts for Paper compatibility.
     */
    private void arclight$addBasicMetricsCharts() {
        if (this.arclight$paperMetrics != null) {
            // Add server software chart
            this.arclight$paperMetrics.addCustomChart(new Metrics.SimplePie("server_software", () -> "Arclight-Paper"));

            // Add Java version chart
            this.arclight$paperMetrics.addCustomChart(new Metrics.SimplePie("java_version", () ->
                    System.getProperty("java.version")));
        }
    }
}
