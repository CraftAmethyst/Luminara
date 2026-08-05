package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.ErrorHandlingSpec;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Simplified crash handler for continue-on-crash functionality
 */
public class ArclightCrashHandler {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger(
        "ArclightCrashHandler"
    );

    /**
     * Handles a server crash based on configuration
     *
     * @param throwable       The throwable that caused the crash
     * @param crashReport     The crash report
     * @param serverDirectory The server directory for crash reports
     * @return true if the server should continue running, false if it should stop
     */
    public static boolean handleCrash(
        Throwable throwable,
        CrashReport crashReport,
        File serverDirectory
    ) {
        ErrorHandlingSpec config = ArclightConfig.spec().getErrorHandling();

        if (config == null) {
            // Fallback to safe behavior if config is not available
            LOGGER.error(
                "Error handling configuration not available, stopping server for safety"
            );
            return false;
        }

        // Check if we should continue based on configuration
        if (!config.isContinueOnCrash()) {
            LOGGER.info(
                "optimization.error-handling.continue-on-crash-disabled"
            );
            return false;
        }

        // Warn about potential risks
        LOGGER.warn("optimization.error-handling.continue-on-crash-warning");

        // Generate crash report with custom directory
        generateCrashReport(crashReport, serverDirectory, config);

        return true;
    }

    private static void generateCrashReport(
        CrashReport crashReport,
        File serverDirectory,
        ErrorHandlingSpec config
    ) {
        try {
            File crashDir = new File(
                serverDirectory,
                config.getCrashReportDirectory()
            );
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }

            File crashFile = new File(
                crashDir,
                "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt"
            );
            if (crashReport.saveToFile(crashFile)) {
                LOGGER.info(
                    "Crash report saved to: {}",
                    crashFile.getAbsolutePath()
                );
            } else {
                LOGGER.error("Failed to save crash report");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save crash report", e);
        }
    }
}
