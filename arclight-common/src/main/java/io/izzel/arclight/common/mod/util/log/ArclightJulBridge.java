package io.izzel.arclight.common.mod.util.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Routes {@code java.util.logging} output into Log4j.
 * <p>
 * Bukkit logs through JUL: {@code CraftServer} owns {@code Logger.getLogger("Minecraft")} and
 * every plugin logger inherits from it. The legacy launcher either replaced the JUL
 * {@code LogManager} through a JVM argument or rewrote {@code Logger.getLogger} call sites from a
 * ModLauncher launch plugin ({@code LoggerTransformer}). A standalone mod can do neither, so JUL
 * kept its default {@link ConsoleHandler} and Spigot lines were printed in the JUL
 * {@code SimpleFormatter} format, bypassing the server log format and the console appender.
 */
public final class ArclightJulBridge {

    private static final String LOG4J_LOG_MANAGER = "org.apache.logging.log4j.jul.LogManager";
    private static final String DEFAULT_LOGGER_NAME = "Minecraft";

    private ArclightJulBridge() {
    }

    public static void install() {
        var manager = java.util.logging.LogManager.getLogManager();
        if (manager.getClass().getName().equals(LOG4J_LOG_MANAGER)) {
            // JUL hands out Log4j backed loggers already, its handlers are never consulted.
            return;
        }
        var root = manager.getLogger("");
        if (root == null) {
            return;
        }
        for (var handler : root.getHandlers()) {
            if (handler instanceof Log4jHandler) {
                return;
            }
        }
        for (var handler : root.getHandlers()) {
            // Only the default console handler is replaced, handlers other mods installed stay.
            if (handler instanceof ConsoleHandler) {
                root.removeHandler(handler);
            }
        }
        root.addHandler(new Log4jHandler());
    }

    private static final class Log4jHandler extends Handler {

        private static final Formatter FORMATTER = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return formatMessage(record);
            }
        };

        @Override
        public void publish(LogRecord record) {
            if (record == null || !isLoggable(record)) {
                return;
            }
            var name = record.getLoggerName();
            var logger = LogManager.getLogger(name == null || name.isEmpty() ? DEFAULT_LOGGER_NAME : name);
            var level = convert(record.getLevel());
            if (!logger.isEnabled(level)) {
                return;
            }
            logger.log(level, FORMATTER.format(record), record.getThrown());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        /**
         * Mirrors {@code org.apache.logging.log4j.jul.DefaultLevelConverter} so this bridge and the
         * launcher's {@code LogManager} replacement render the same records at the same level.
         */
        private static Level convert(java.util.logging.Level level) {
            var value = level.intValue();
            if (value >= java.util.logging.Level.SEVERE.intValue()) {
                return Level.ERROR;
            }
            if (value >= java.util.logging.Level.WARNING.intValue()) {
                return Level.WARN;
            }
            if (value >= java.util.logging.Level.INFO.intValue()) {
                return Level.INFO;
            }
            if (value >= java.util.logging.Level.FINE.intValue()) {
                return Level.DEBUG;
            }
            return Level.TRACE;
        }
    }
}
