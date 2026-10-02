package io.izzel.arclight.common.mod.util.log;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Plugin logger that routes Bukkit's JUL API through the Log4j logger owned by Forge.
 */
public class ArclightPluginLogger extends PluginLogger {

    private final java.util.logging.Logger logger;

    public ArclightPluginLogger(Plugin context) {
        super(context);
        String prefix = context.getDescription().getPrefix();
        logger = new Log4jLogger(
            prefix == null ? context.getName() : prefix
        );
    }

    public static java.util.logging.Logger getLogger(String name) {
        return new Log4jLogger(name);
    }

    public static java.util.logging.Logger getLogger(String name, String rb) {
        return new Log4jLogger(name);
    }

    @Override
    public void log(LogRecord logRecord) {
        // PluginLogger's own constructor logs before this subclass has assigned its field,
        // so the parent has to keep handling records that arrive that early.
        if (logger == null) {
            super.log(logRecord);
            return;
        }
        logger.log(logRecord);
    }

    private static final class Log4jLogger extends java.util.logging.Logger {

        private final Logger delegate;

        private Log4jLogger(String name) {
            super(name, null);
            delegate = LogManager.getLogger(name);
        }

        @Override
        public void log(LogRecord record) {
            if (record == null) return;
            String message = record.getMessage();
            Object[] parameters = record.getParameters();
            if (parameters != null && parameters.length > 0) {
                message = MessageFormat.format(message, parameters);
            }
            delegate.log(
                toLog4jLevel(record.getLevel()),
                message,
                record.getThrown()
            );
        }

        @Override
        public void log(Level level, String message) {
            delegate.log(toLog4jLevel(level), message);
        }

        @Override
        public void log(Level level, String message, Object parameter) {
            delegate.log(toLog4jLevel(level), message, parameter);
        }

        @Override
        public void log(Level level, String message, Object[] parameters) {
            delegate.log(toLog4jLevel(level), message, parameters);
        }

        @Override
        public void log(Level level, String message, Throwable throwable) {
            delegate.log(toLog4jLevel(level), message, throwable);
        }

        @Override
        public void log(Level level, Supplier<String> supplier) {
            if (isLoggable(level)) {
                delegate.log(toLog4jLevel(level), supplier.get());
            }
        }

        @Override
        public boolean isLoggable(Level level) {
            return delegate.isEnabled(toLog4jLevel(level));
        }

        private static org.apache.logging.log4j.Level toLog4jLevel(
            Level level
        ) {
            int value = level.intValue();
            if (value >= Level.SEVERE.intValue()) {
                return org.apache.logging.log4j.Level.ERROR;
            }
            if (value >= Level.WARNING.intValue()) {
                return org.apache.logging.log4j.Level.WARN;
            }
            if (value >= Level.INFO.intValue()) {
                return org.apache.logging.log4j.Level.INFO;
            }
            if (value >= Level.CONFIG.intValue()) {
                return org.apache.logging.log4j.Level.DEBUG;
            }
            return org.apache.logging.log4j.Level.TRACE;
        }
    }
}
