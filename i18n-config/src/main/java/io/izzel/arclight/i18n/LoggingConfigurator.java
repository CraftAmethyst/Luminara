package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.ConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

final class LoggingConfigurator {

    private static final String JUL_FORMAT_PROPERTY = "java.util.logging.SimpleFormatter.format";
    private static final String JUL_SIMPLE_FORMAT = "[%1$tT] [JUL] %5$s%6$s%n";
    private static final String JUL_DETAILED_FORMAT = "[%1$tT] [JUL/%4$s] [%3$s] %5$s%6$s%n";
    private static final String CONSOLE_CHARSET_PROPERTY = "luminara.console.charset";
    private static final String ANSI_FLAG_PROPERTY = "terminal.ansi";
    private static final String COLOR_RESET = "\u001B[0m";
    private static final String COLOR_BOLD = "\u001B[1m";
    private static final String COLOR_CYAN = "\u001B[36m";
    private static final String COLOR_MAGENTA = "\u001B[35m";
    private static final String COLOR_GREEN = "\u001B[32m";
    private static final String COLOR_YELLOW = "\u001B[33m";
    private static final String COLOR_RED = "\u001B[31m";
    private static final String COLOR_BLUE = "\u001B[34m";
    private static final String COLOR_AQUA = "\u001B[36m";
    private static final String COLOR_GRAY = "\u001B[37m";
    private static final String COLOR_DARK_GRAY = "\u001B[90m";
    private static final String COLOR_LIGHT_RED = "\u001B[91m";
    private static final String COLOR_LIGHT_GREEN = "\u001B[92m";
    private static final String COLOR_LIGHT_YELLOW = "\u001B[93m";
    private static final String COLOR_LIGHT_BLUE = "\u001B[94m";
    private static final String COLOR_LIGHT_MAGENTA = "\u001B[95m";
    private static final String COLOR_LIGHT_AQUA = "\u001B[96m";
    private static final String COLOR_WHITE = "\u001B[37m";
    private static final DateTimeFormatter JUL_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private LoggingConfigurator() {
    }

    static void apply(ConfigSpec spec) {
        try {
            boolean useSimpleFormat;
            try {
                useSimpleFormat = spec != null
                        && spec.getLogging() != null
                        && spec.getLogging().isUseSimpleFormat();
            } catch (Exception e) {
                useSimpleFormat = false;
            }

            String configFile = useSimpleFormat ? "arclight-log4j2.xml" : "arclight-log4j2-detailed.xml";
            reconfigureJulLogging(useSimpleFormat);
            reconfigureLogging(configFile);

            System.out.println("[Luminara] Applied logging configuration: " +
                    (useSimpleFormat ? "Simple format" : "Detailed format"));

        } catch (Exception e) {
            System.err.println("Failed to apply logging configuration: " + e.getMessage());
        }
    }

    private static void reconfigureLogging(String configFile) {
        try {
            ClassLoader classLoader = LoggingConfigurator.class.getClassLoader();
            URI configUri = Objects.requireNonNull(classLoader.getResource(configFile)).toURI();

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.setConfigLocation(configUri);
            context.reconfigure();

        } catch (Exception e) {
            System.err.println("Failed to reconfigure logging: " + e.getMessage());
            System.setProperty("log4j.configurationFile", configFile);
        }
    }

    private static void reconfigureJulLogging(boolean useSimpleFormat) {
        String format = useSimpleFormat ? JUL_SIMPLE_FORMAT : JUL_DETAILED_FORMAT;
        System.setProperty(JUL_FORMAT_PROPERTY, format);
        try {
            String outputEncoding = resolveOutputEncoding();
            Formatter formatter = new ColoredJulFormatter(!useSimpleFormat);
            refreshAllJulHandlers(formatter, outputEncoding);
            // Some JUL handlers are created after bootstrap; refresh once more shortly after startup.
            Thread refresher = new Thread(() -> {
                try {
                    Thread.sleep(2500L);
                    refreshAllJulHandlers(formatter, outputEncoding);
                } catch (Throwable ignored) {
                }
            }, "Luminara-JUL-Refresher");
            refresher.setDaemon(true);
            refresher.start();
        } catch (Throwable ignored) {
        }
    }

    private static void refreshAllJulHandlers(Formatter formatter, String outputEncoding) {
        java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
        refreshJulHandlers(manager.getLogger(""), formatter, outputEncoding);
        refreshJulHandlers(manager.getLogger("global"), formatter, outputEncoding);
        Enumeration<String> names = manager.getLoggerNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            refreshJulHandlers(manager.getLogger(name), formatter, outputEncoding);
        }
    }

    private static String resolveOutputEncoding() {
        String[] keys = new String[]{
                CONSOLE_CHARSET_PROPERTY,
                "stdout.encoding",
                "sun.stdout.encoding",
                "native.encoding",
                "file.encoding"
        };
        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static void refreshJulHandlers(java.util.logging.Logger logger, Formatter formatter, String outputEncoding) {
        if (logger == null) {
            return;
        }
        for (Handler handler : logger.getHandlers()) {
            try {
                handler.setFormatter(formatter);
            } catch (Exception ignored) {
            }
            if (outputEncoding != null && !outputEncoding.isEmpty()) {
                try {
                    handler.setEncoding(outputEncoding);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static final class ColoredJulFormatter extends Formatter {

        private final boolean detailed;
        private final boolean useAnsi;

        private ColoredJulFormatter(boolean detailed) {
            this.detailed = detailed;
            this.useAnsi = !"false".equalsIgnoreCase(System.getProperty(ANSI_FLAG_PROPERTY, "true"));
        }

        private static String stripMinecraftFormatting(String message) {
            if (message == null || message.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder(message.length());
            for (int i = 0; i < message.length(); i++) {
                char c = message.charAt(i);
                if (c == '\u00A7' && i + 1 < message.length()) {
                    i++;
                    continue;
                }
                sb.append(c);
            }
            return sb.toString();
        }

        private static String colorFor(Level level) {
            if (level == null) {
                return COLOR_GREEN;
            }
            int value = level.intValue();
            if (value >= Level.SEVERE.intValue()) {
                return COLOR_RED;
            }
            if (value >= Level.WARNING.intValue()) {
                return COLOR_YELLOW;
            }
            return COLOR_GREEN;
        }

        @Override
        public String format(LogRecord record) {
            String time = JUL_TIME.format(Instant.ofEpochMilli(record.getMillis()).atZone(ZoneId.systemDefault()).toLocalTime());
            String level = record.getLevel() == null ? "INFO" : record.getLevel().getLocalizedName();
            String loggerName = record.getLoggerName() == null ? "global" : record.getLoggerName();
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(sw));
                throwable = sw.toString();
            }

            if (!useAnsi) {
                if (detailed) {
                    return "[" + time + "] [JUL/" + level + "] [" + loggerName + "] " + message + System.lineSeparator() + throwable;
                }
                return "[" + time + "] [JUL/" + level + "] " + message + System.lineSeparator() + throwable;
            }

            String levelColor = colorFor(record.getLevel());
            String coloredMessage = COLOR_WHITE + stripMinecraftFormatting(message);
            StringBuilder sb = new StringBuilder(256);
            sb.append(COLOR_WHITE).append('[').append(COLOR_RESET)
                    .append(COLOR_CYAN).append(time).append(COLOR_RESET)
                    .append(COLOR_WHITE).append(']').append(COLOR_RESET).append(' ')
                    .append(COLOR_WHITE).append('[').append(COLOR_RESET)
                    .append(COLOR_BOLD).append(COLOR_LIGHT_MAGENTA).append("JUL").append(COLOR_RESET)
                    .append(COLOR_WHITE).append('/').append(COLOR_RESET)
                    .append(levelColor).append(level).append(COLOR_RESET)
                    .append(COLOR_WHITE).append(']').append(COLOR_RESET);
            if (detailed) {
                sb.append(' ').append('[').append(COLOR_MAGENTA).append(loggerName).append(COLOR_RESET).append(']');
            }
            sb.append(' ').append(coloredMessage).append(COLOR_RESET).append(System.lineSeparator());
            if (!throwable.isEmpty()) {
                sb.append(throwable);
            }
            return sb.toString();
        }
    }
}
