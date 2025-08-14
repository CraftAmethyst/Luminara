package io.izzel.arclight.common.mod.server;

import io.izzel.arclight.common.adventure.AdventureInitializer;
import io.izzel.arclight.common.adventure.PaperAdventure;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.configuration.PaperConfigurations;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;

/**
 * Validator for Paper integration functionality.
 * This class provides methods to validate that all Paper features are working correctly.
 */
public final class PaperIntegrationValidator {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("PaperValidator");

    private PaperIntegrationValidator() {
    }

    /**
     * Validate all Paper integration components.
     *
     * @return true if all validations pass, false otherwise
     */
    public static boolean validateAll() {
        LOGGER.info("Starting Paper integration validation...");

        boolean allValid = true;

        // Validate Paper integration
        if (!validatePaperIntegration()) {
            LOGGER.error("Paper integration validation failed");
            allValid = false;
        }

        // Validate Paper configuration system
        if (!validatePaperConfiguration()) {
            LOGGER.error("Paper configuration validation failed");
            allValid = false;
        }

        // Validate Adventure integration
        if (!validateAdventureIntegration()) {
            LOGGER.error("Adventure integration validation failed");
            allValid = false;
        }

        // Validate concurrent utilities
        if (!validateConcurrentUtilities()) {
            LOGGER.error("Concurrent utilities validation failed");
            allValid = false;
        }

        if (allValid) {
            LOGGER.info("All Paper integration validations passed successfully");
        } else {
            LOGGER.error("Some Paper integration validations failed");
        }

        return allValid;
    }

    /**
     * Validate Paper integration status.
     */
    private static boolean validatePaperIntegration() {
        try {
            LOGGER.debug("Validating Paper integration...");

            // Check if Paper integration is initialized
            if (!PaperIntegration.isInitialized()) {
                LOGGER.error("Paper integration is not initialized");
                return false;
            }

            LOGGER.debug("Paper integration validation passed");
            return true;

        } catch (Exception e) {
            LOGGER.error("Paper integration validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate Paper configuration system.
     */
    private static boolean validatePaperConfiguration() {
        try {
            LOGGER.debug("Validating Paper configuration system...");

            // Test global configuration access
            GlobalConfiguration globalConfig = PaperConfigurations.getGlobalConfiguration();
            if (globalConfig == null) {
                LOGGER.error("Global configuration is null");
                return false;
            }

            LOGGER.debug("Paper configuration validation passed");
            return true;

        } catch (Exception e) {
            LOGGER.error("Paper configuration validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate Adventure integration.
     */
    private static boolean validateAdventureIntegration() {
        try {
            LOGGER.debug("Validating Adventure integration...");

            // Check if Adventure is initialized
            if (!AdventureInitializer.isInitialized()) {
                LOGGER.error("Adventure integration is not initialized");
                return false;
            }

            // Test basic Adventure functionality
            Component testComponent = Component.text("Test");
            net.minecraft.network.chat.Component vanillaComponent = PaperAdventure.asVanilla(testComponent);
            if (vanillaComponent == null) {
                LOGGER.error("Adventure component conversion failed");
                return false;
            }

            // Test reverse conversion
            Component adventureComponent = PaperAdventure.asAdventure(vanillaComponent);
            if (adventureComponent == null) {
                LOGGER.error("Vanilla component conversion failed");
                return false;
            }

            LOGGER.debug("Adventure integration validation passed");
            return true;

        } catch (Exception e) {
            LOGGER.error("Adventure integration validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate concurrent utilities.
     */
    private static boolean validateConcurrentUtilities() {
        try {
            LOGGER.debug("Validating concurrent utilities...");

            // Test MultiThreadedQueue
            ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue<String> queue =
                    new ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue<>();

            queue.add("test");
            String result = queue.poll();
            if (!"test".equals(result)) {
                LOGGER.error("MultiThreadedQueue test failed");
                return false;
            }

            LOGGER.debug("Concurrent utilities validation passed");
            return true;

        } catch (Exception e) {
            LOGGER.error("Concurrent utilities validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get validation report.
     */
    public static String getValidationReport() {
        StringBuilder report = new StringBuilder();
        report.append("Paper Integration Validation Report:\n");

        report.append("- Paper Integration: ").append(validatePaperIntegration() ? "PASS" : "FAIL").append("\n");
        report.append("- Paper Configuration: ").append(validatePaperConfiguration() ? "PASS" : "FAIL").append("\n");
        report.append("- Adventure Integration: ").append(validateAdventureIntegration() ? "PASS" : "FAIL").append("\n");
        report.append("- Concurrent Utilities: ").append(validateConcurrentUtilities() ? "PASS" : "FAIL").append("\n");

        return report.toString();
    }

    /**
     * Perform a quick validation check.
     */
    public static boolean quickValidation() {
        try {
            return PaperIntegration.isInitialized() &&
                    AdventureInitializer.isInitialized() &&
                    PaperConfigurations.getGlobalConfiguration() != null;
        } catch (Exception e) {
            LOGGER.debug("Quick validation failed: " + e.getMessage());
            return false;
        }
    }
}
