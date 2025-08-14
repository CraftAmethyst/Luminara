package io.izzel.arclight.common.mixin.paper.configuration;

import io.izzel.arclight.common.mod.server.PaperConfigurationInitializer;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.configuration.WorldConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate Paper configuration system into Luminara.
 * This mixin ensures that Paper's configuration system is properly initialized
 * and accessible throughout the server lifecycle.
 */
@Mixin(MinecraftServer.class)
public class PaperConfigurationMixin {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperConfiguration");

    /**
     * Initialize Paper configuration system when the server starts.
     * This ensures that both global and world configurations are available.
     */
    @Inject(method = "runServer", at = @At("HEAD"))
    private void arclight$initializePaperConfiguration(CallbackInfo ci) {
        try {
            // Initialize Paper configuration system using our initializer
            PaperConfigurationInitializer.initialize();
        } catch (Exception e) {
            // Log error but don't fail server startup
            LOGGER.error("Failed to initialize Paper configuration system: " + e.getMessage(), e);
        }
    }

    /**
     * Ensure world configuration is properly initialized for each world.
     */
    @Inject(method = "createLevels", at = @At("RETURN"))
    private void arclight$initializeWorldConfigurations(CallbackInfo ci) {
        try {
            MinecraftServer server = (MinecraftServer) (Object) this;
            for (ServerLevel level : server.getAllLevels()) {
                // Initialize world configuration for each level
                PaperConfigurationInitializer.initializeWorldConfiguration(level);

                // Verify configuration is available
                WorldConfiguration worldConfig = PaperConfigurationInitializer.getWorldConfiguration(level);
                if (worldConfig == null) {
                    LOGGER.warn("Warning: World configuration not available for level " + level.dimension().location());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize world configurations: " + e.getMessage(), e);
        }
    }
}
