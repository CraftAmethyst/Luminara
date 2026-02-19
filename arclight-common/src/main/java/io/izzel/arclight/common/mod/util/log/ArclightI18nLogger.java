package io.izzel.arclight.common.mod.util.log;

import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.i18n.ArclightLocale;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class ArclightI18nLogger {

    private static final MethodHandle MH_GET_LOGGER_BOOT;

    static {
        MethodHandle method = null;
        try {
            method = Unsafe.lookup().findStatic(Class.forName("io.izzel.arclight.boot.log.ArclightI18nLogger"), "getLogger", MethodType.methodType(Logger.class, String.class));
        } catch (Throwable ignored) {
            // Fabric runtime has no boot logger bridge; use in-process i18n fallback logger.
        }
        MH_GET_LOGGER_BOOT = method;
    }

    public static Logger getLogger(String name) {
        if (MH_GET_LOGGER_BOOT != null) {
            try {
                return (Logger) MH_GET_LOGGER_BOOT.invokeExact(name);
            } catch (Throwable ignored) {
            }
        }
        return new FallbackI18nLogger((ExtendedLogger) LogManager.getLogger(name));
    }

    private static String localize(String key) {
        if (key == null) {
            return null;
        }
        try {
            return ArclightLocale.getInstance().get(key);
        } catch (Throwable ignored) {
            return key;
        }
    }

    private static final class FallbackI18nLogger extends ExtendedLoggerWrapper {

        private FallbackI18nLogger(ExtendedLogger logger) {
            super(logger, logger.getName(), logger.getMessageFactory());
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, CharSequence message, Throwable t) {
            super.logMessage(fqcn, level, marker, localize(message == null ? null : message.toString()), t);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, Object message, Throwable t) {
            super.logMessage(fqcn, level, marker, localize(message == null ? null : message.toString()), t);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, MessageSupplier msgSupplier, Throwable t) {
            super.logMessage(fqcn, level, marker, msgSupplier, t);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, Supplier<?> msgSupplier, Throwable t) {
            super.logMessage(fqcn, level, marker, msgSupplier, t);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Throwable t) {
            super.logMessage(fqcn, level, marker, localize(message), t);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message) {
            super.logMessage(fqcn, level, marker, localize(message));
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object... params) {
            super.logMessage(fqcn, level, marker, localize(message), params);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0) {
            super.logMessage(fqcn, level, marker, localize(message), p0);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4, p5);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4, p5, p6);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4, p5, p6, p7);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4, p5, p6, p7, p8);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
            super.logMessage(fqcn, level, marker, localize(message), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
        }

        @Override
        protected void logMessage(String fqcn, Level level, Marker marker, String message, Supplier<?>... paramSuppliers) {
            super.logMessage(fqcn, level, marker, localize(message), paramSuppliers);
        }
    }
}
