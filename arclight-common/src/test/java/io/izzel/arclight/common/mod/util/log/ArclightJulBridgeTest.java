package io.izzel.arclight.common.mod.util.log;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class ArclightJulBridgeTest {

    @Test
    void replacesDefaultConsoleHandlerAndInstallsOnlyOnce() {
        var manager = java.util.logging.LogManager.getLogManager();
        assumeFalse(manager.getClass().getName().equals("org.apache.logging.log4j.jul.LogManager"));

        var root = manager.getLogger("");
        Handler[] original = root.getHandlers();
        try {
            Arrays.stream(original).forEach(root::removeHandler);
            root.addHandler(new ConsoleHandler());

            ArclightJulBridge.install();
            ArclightJulBridge.install();

            Handler[] installed = root.getHandlers();
            assertFalse(Arrays.stream(installed).anyMatch(ConsoleHandler.class::isInstance));
            assertEquals(1, Arrays.stream(installed)
                .filter(handler -> handler.getClass().getEnclosingClass() == ArclightJulBridge.class)
                .count());
        } finally {
            Arrays.stream(root.getHandlers()).forEach(root::removeHandler);
            Arrays.stream(original).forEach(root::addHandler);
        }
    }
}
