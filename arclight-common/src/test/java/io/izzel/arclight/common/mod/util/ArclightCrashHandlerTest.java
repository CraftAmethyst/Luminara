package io.izzel.arclight.common.mod.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArclightCrashHandlerTest {

    @TempDir
    Path directory;

    @Test
    void usesConfiguredRelativeCrashDirectory() {
        assertEquals(directory.resolve("reports").normalize(), ArclightCrashHandler.resolveCrashDirectory(directory, "reports"));
    }

    @Test
    void fallsBackForInvalidCrashDirectory() {
        assertEquals(directory.resolve("crash-reports").normalize(), ArclightCrashHandler.resolveCrashDirectory(directory, "bad\u0000path"));
    }
}
