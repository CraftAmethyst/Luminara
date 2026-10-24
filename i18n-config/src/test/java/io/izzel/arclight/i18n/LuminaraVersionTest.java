package io.izzel.arclight.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LuminaraVersionTest {
  @Test
  void exposesAllRuntimeMetadata() {
    assertEquals("1.21.1", LuminaraVersion.minecraftVersion());
    assertEquals("TestLoader", LuminaraVersion.loader());
    assertEquals("1.0", LuminaraVersion.loaderVersion());
    assertEquals("21", LuminaraVersion.javaVersion());
    assertEquals("v1_21_R1", LuminaraVersion.bukkitPackage());
    assertEquals("test-version", LuminaraVersion.version());
    assertEquals("0123456", LuminaraVersion.gitCommit());
    assertEquals("2026-01-01T00:00:00Z", LuminaraVersion.buildTime());
    assertEquals("1.21.1 / TestLoader 1.0 / Java 21", LuminaraVersion.compatibilityLine());
  }
}
