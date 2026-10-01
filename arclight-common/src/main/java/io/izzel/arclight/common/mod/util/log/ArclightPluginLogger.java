package io.izzel.arclight.common.mod.util.log;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Plugin logger that routes through the java.util.logging manager the server installed.
 * <p>
 * It deliberately does not link against {@code org.apache.logging.log4j.jul.LogManager}:
 * Forge owns the {@code org.apache.logging.log4j} packages, so a bundled copy is not what
 * the module class loader resolves and a direct reference fails when a plugin loads. Asking
 * the manager itself for a logger gives the same routing without the hard dependency.
 */
public class ArclightPluginLogger extends PluginLogger {

    private static final java.util.logging.LogManager JUL_MANAGER =
        java.util.logging.LogManager.getLogManager();

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
        // PluginLogger's own constructor logs before this subclass has assigned its field,
        // so the parent has to keep handling records that arrive that early.
        if (logger == null) {
            super.log(logRecord);
            return;
        }
        logger.log(logRecord);
    }
}
