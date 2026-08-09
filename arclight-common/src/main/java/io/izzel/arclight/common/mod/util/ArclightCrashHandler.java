package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.ErrorHandlingSpec;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.ReportType;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public final class ArclightCrashHandler {

    private ArclightCrashHandler() {
    }

    public static boolean handleCrash(Throwable throwable, CrashReport crashReport, Path serverDirectory) {
        ErrorHandlingSpec config = ArclightConfig.spec().getErrorHandling();
        if (!config.isContinueOnCrash()) {
            return false;
        }

        Path crashDirectory = resolveCrashDirectory(serverDirectory, config.getCrashReportDirectory());
        if (!"crash-reports".equals(config.getCrashReportDirectory()) && crashDirectory.equals(serverDirectory.resolve("crash-reports").normalize())) {
            ArclightServer.LOGGER.error("Invalid crash report directory '{}'; using crash-reports", config.getCrashReportDirectory());
        }
        Path crashFile = crashDirectory.resolve("crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
        try {
            Files.createDirectories(crashDirectory);
            if (!crashReport.saveToFile(crashFile, ReportType.CRASH)) {
                ArclightServer.LOGGER.error("Failed to save crash report to {}", crashFile);
            }
        } catch (Exception exception) {
            ArclightServer.LOGGER.error("Failed to save crash report to {}", crashFile, exception);
        }
        ArclightServer.LOGGER.error("Continuing after a server tick crash because error-handling.continue-on-crash is enabled", throwable);
        return true;
    }

    static Path resolveCrashDirectory(Path serverDirectory, String configuredDirectory) {
        try {
            Path configured = Path.of(configuredDirectory == null || configuredDirectory.isBlank() ? "crash-reports" : configuredDirectory);
            Path resolved = configured.isAbsolute() ? configured.normalize() : serverDirectory.resolve(configured).normalize();
            Files.createDirectories(resolved);
            if (Files.isWritable(resolved)) {
                return resolved;
            }
        } catch (InvalidPathException | java.io.IOException ignored) {
        }
        Path fallback = serverDirectory.resolve("crash-reports").normalize();
        try {
            Files.createDirectories(fallback);
        } catch (java.io.IOException ignored) {
        }
        return fallback;
    }
}
