package io.izzel.arclight.common.mod.util.log;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.logging.log4j.jul.LogManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

public class ArclightPluginLogger extends PluginLogger {

    private static final LogManager JUL_MANAGER =
        java.util.logging.LogManager.getLogManager() instanceof
                LogManager instance
            ? instance
            : new LogManager();

    private final Logger logger;

    public ArclightPluginLogger(Plugin context) {
        super(context);
        String prefix = context.getDescription().getPrefix();
        logger = JUL_MANAGER.getLogger(
            prefix == null ? context.getName() : prefix
        );
    }

    public static Logger getLogger(String name) {
        return JUL_MANAGER.getLogger(name);
    }

    public static Logger getLogger(String name, String rb) {
        return JUL_MANAGER.getLogger(name);
    }

    @Override
    public void log(LogRecord logRecord) {
        logger.log(logRecord);
    }
}
