package io.izzel.arclight.common.mod.util.log;

import io.izzel.arclight.api.Unsafe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class ArclightI18nLogger {

    private static final MethodHandle MH_GET_LOGGER_BOOT;

    static {
        MethodHandle method = null;
        try {
            method = Unsafe.lookup().findStatic(Class.forName("io.izzel.arclight.boot.log.ArclightI18nLogger"), "getLogger", MethodType.methodType(Logger.class, String.class));
        } catch (Throwable ignored) {
            // Fabric runtime has no boot logger bridge; fall back to plain Log4j logger.
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
        return LogManager.getLogger(name);
    }
}
