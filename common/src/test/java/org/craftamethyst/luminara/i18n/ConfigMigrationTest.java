package org.craftamethyst.luminara.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.craftamethyst.luminara.i18n.conf.PermissionForwarding;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigMigrationTest {

  @TempDir Path directory;

  @ParameterizedTest
  @CsvSource({
    "true, FORGE_TO_BUKKIT",
    "'true', FORGE_TO_BUKKIT",
    "reverse, BUKKIT_TO_FORGE",
    "false, DISABLED",
  })
  void migratesPermissionForwardingAndRemovesUnsafeSections(
      String legacy, PermissionForwarding expected) throws Exception {
    Path source = directory.resolve(ArclightConfig.LEGACY_FILE_NAME);
    Files.writeString(
        source,
        """
        _v: 1
        locale:
          fallback: en_us
        optimization:
          cache-plugin-class: true
          async-system:
            enabled: true
          entity-optimization:
            reduce-entity-updates: true
          memory-optimization:
            cache-cleanup-enabled: true
          world-creation:
            skip-spawn-chunk-loading: true
          chunk-optimization:
            aggressive-chunk-unloading: true
        compatibility:
          forward-permission: %s
        async-world-save:
          enabled: true
        """
            .formatted(legacy));

    var config = ConfigMigration.readLegacy(source);
    var removed = ConfigMigration.migrate(config);

    assertEquals(2, config.getInt("_v"));
    assertEquals(expected.name(), config.get("compatibility.permission-forwarding"));
    assertFalse(config.contains("compatibility.forward-permission"));
    assertEquals(6, removed.size());
    for (String path : removed) {
      assertFalse(config.contains(path), path);
    }
  }
}
