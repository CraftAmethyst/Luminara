package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.ConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class LoggingConfigurator {

    private static final String CONSOLE_CHARSET_PROPERTY = "luminara.console.charset";
    // JLine, which backs the TerminalConsole appender, falls back to file.encoding.
    // Since Java 18 that is UTF-8 even when the console itself uses the platform
    // charset, which turns every non-ASCII line into mojibake on Windows.
    private static final String JLINE_CHARSET_PROPERTY = "org.jline.terminal.encoding";

    private static final String SIMPLE_CONFIG = "arclight-log4j2-simple";
    private static final String DETAILED_CONFIG = "arclight-log4j2-detailed";
    private static final String CONSOLE_SUFFIX = "-console";
    private static final String CONFIG_SUFFIX = ".xml";
    // PatternLayout disables ANSI on Windows unless Jansi is expected to handle it
    // (its builder defaults to log4j.skipJansi=true). We provide the terminal/ANSI
    // handling ourselves, so let the layouts emit colours.
    private static final String SKIP_JANSI_PROPERTY = "log4j.skipJansi";

    private LoggingConfigurator() {
    }

    static void apply(ConfigSpec spec) {
        try {
            configureConsoleCharset();
            boolean useSimpleFormat = isSimpleFormat(spec);
            // The TerminalConsole appender and the Minecraft formatting converter come
            // from terminalconsoleappender. On the standalone Fabric mod Log4j is
            // loaded by the system class loader while mod JARs are loaded by Knot, so
            // Log4j cannot see those plugins. Keep a console-only variant for that
            // case instead of handing Log4j a configuration it cannot build.
            boolean terminalConsole = isTerminalConsoleUsable();
            String configFile =
                (useSimpleFormat ? SIMPLE_CONFIG : DETAILED_CONFIG) +
                (terminalConsole ? "" : CONSOLE_SUFFIX) +
                CONFIG_SUFFIX;

            enableAnsiLayouts(terminalConsole || installAnsiSupport());

            if (reconfigureLogging(configFile)) {
                Logger logger = LogManager.getLogger("Luminara");
                logger.info(
                    "Applied logging configuration: {} format",
                    useSimpleFormat ? "Simple" : "Detailed"
                );
                logger.debug(
                    "Logging configuration: {} (terminal appender: {}, console stream: {})",
                    configFile,
                    terminalConsole,
                    System.getProperty("luminara.console.ansi", "n/a")
                );
            } else {
                System.err.println(
                    "[Luminara] Failed to apply logging configuration: " +
                        configFile
                );
            }
        } catch (Exception e) {
            System.err.println(
                "[Luminara] Failed to apply logging configuration: " + e
            );
        }
    }

    private static boolean isSimpleFormat(ConfigSpec spec) {
        try {
            return (
                spec != null &&
                spec.getLogging() != null &&
                spec.getLogging().isUseSimpleFormat()
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The TerminalConsole appender only pays off when it owns a real terminal. On the
     * standalone Fabric mod Log4j is loaded by the system class loader while mod JARs
     * are loaded by Knot, so its plugin registry predates the mod class path and the
     * appender is never created; on a headless server the appender cannot obtain a
     * terminal either. Both cases fall back to a plain console appender with Jansi,
     * which still renders colours.
     */
    private static boolean isTerminalConsoleUsable() {
        try {
            Class<?> appender = Class.forName(
                "net.minecrell.terminalconsole.TerminalConsoleAppender",
                false,
                LoggingConfigurator.class.getClassLoader()
            );
            return appender.getMethod("getTerminal").invoke(null) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Without the terminal appender Log4j writes raw ANSI sequences to the console.
     * Jansi turns them into real console attributes on Windows and passes them
     * through on terminals that understand them.
     */
    private static boolean installAnsiSupport() {
        // Prefer the Jansi copy Log4j itself can see (the game libraries on NeoForge).
        // The standalone Fabric mod only ships Jansi inside the mod JAR, which the mod
        // class loader resolves. Older Jansi versions are API-compatible here.
        for (
            ClassLoader loader : new ClassLoader[]{
                LogManager.class.getClassLoader(),
                LoggingConfigurator.class.getClassLoader()
            }
        ) {
            if (loader == null) {
                continue;
            }
            try {
                Class<?> ansiConsole = Class.forName(
                    "org.fusesource.jansi.AnsiConsole",
                    true,
                    loader
                );
                ansiConsole.getMethod("systemInstall").invoke(null);
                System.setProperty(
                    "luminara.console.ansi",
                    System.out.getClass().getName()
                );
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static void enableAnsiLayouts(boolean enabled) {
        if (enabled) {
            System.setProperty(SKIP_JANSI_PROPERTY, "false");
        }
    }

    /**
     * Resolves the charset the console actually uses and publishes it for both the
     * Log4j layouts and JLine. Detection has to happen before the logging context is
     * reconfigured: the terminal is created as soon as the appender is instantiated
     * and JLine reads its encoding only once.
     */
    private static void configureConsoleCharset() {
        String charset = System.getProperty(CONSOLE_CHARSET_PROPERTY);
        if (charset == null || charset.isBlank()) {
            charset = detectConsoleCharset();
            System.setProperty(CONSOLE_CHARSET_PROPERTY, charset);
        }
        if (System.getProperty(JLINE_CHARSET_PROPERTY) == null) {
            System.setProperty(JLINE_CHARSET_PROPERTY, charset);
        }
    }

    private static String detectConsoleCharset() {
        for (String property : new String[]{
            "stdout.encoding",
            "sun.stdout.encoding",
            "native.encoding",
            "file.encoding"
        }) {
            String candidate = System.getProperty(property);
            if (candidate != null && !candidate.isBlank()) {
                try {
                    return Charset.forName(candidate).name();
                } catch (Exception ignored) {
                }
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    private static boolean reconfigureLogging(String configFile) {
        URL resource = LoggingConfigurator.class.getClassLoader().getResource(
            configFile
        );
        if (resource == null) {
            return false;
        }
        try {
            URI configUri = resource.toURI();
            LoggerContext context = (LoggerContext) LogManager.getContext(
                false
            );
            context.setConfigLocation(configUri);
            context.reconfigure();
            // Elements Log4j cannot build are dropped from the configuration, which
            // leaves the previous appenders active. Verify that ours were installed
            // instead of trusting reconfigure() to have thrown.
            var configuration = context.getConfiguration();
            return (
                configuration != null &&
                "LuminaraLogging".equals(configuration.getName()) &&
                configuration.getAppenders().containsKey("Console")
            );
        } catch (Exception e) {
            System.err.println(
                "[Luminara] Failed to reconfigure logging with " +
                    configFile + ": " + e
            );
            return false;
        }
    }
}
