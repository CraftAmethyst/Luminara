package io.izzel.arclight.common.mod.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArclightCrashHandlerTest {

  @TempDir Path directory;

  @Test
  void usesConfiguredRelativeCrashDirectory() {
    assertEquals(
        directory.resolve("reports").normalize(),
        ArclightCrashHandler.resolveCrashDirectory(directory, "reports"));
  }

  @Test
  void fallsBackForInvalidCrashDirectory() {
    assertEquals(
        directory.resolve("crash-reports").normalize(),
        ArclightCrashHandler.resolveCrashDirectory(directory, "bad\u0000path"));
  }
}
