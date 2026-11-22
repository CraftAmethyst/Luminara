package org.craftamethyst.luminara.i18n;

import static org.junit.jupiter.api.Assertions.*;

import com.electronwill.nightconfig.toml.TomlParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.craftamethyst.luminara.i18n.conf.AsyncCatcherSpec.Operation;
import org.craftamethyst.luminara.i18n.conf.PermissionForwarding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArclightConfigPersistenceTest {

  @TempDir Path directory;

  private Path configPath() {
    return directory.resolve("config").resolve(ArclightConfig.FILE_NAME);
  }

  @Test
  void createsLocalizedDefaultsInConfigDirectory() throws Exception {
    Locale original = Locale.getDefault();
    try {
      Locale.setDefault(Locale.US);
      var spec = ArclightConfig.load(configPath());

      assertEquals(2, spec.getVersion());
      assertEquals("en_us", spec.getLocale().getCurrent());
      assertEquals("zh_cn", spec.getLocale().getFallback());
      assertTrue(spec.getOptimization().isCachePluginClass());
      assertEquals(Operation.BLOCK, spec.getAsyncCatcher().getDefaultOp());
      assertEquals(PermissionForwarding.FORGE_TO_BUKKIT, spec.getCompat().getPermissionForwarding());
      String toml = Files.readString(configPath());
      String normalized = toml.replace("\r\n", "\n");
      assertTrue(normalized.contains("# Config version number, do not edit.\n_v = 2"));
      assertTrue(normalized.contains("# Language/I18n settings\n[locale]"));
      assertEquals("en_us", ArclightConfig.load(configPath()).getLocale().getCurrent());
      assertEquals(toml, Files.readString(configPath()));
    } finally {
      Locale.setDefault(original);
    }
  }

  @Test
  void localizesExistingTomlUsingConfiguredLocale() throws Exception {
    Path config = configPath();
    Files.createDirectories(config.getParent());
    Files.writeString(config, "_v = 2\n[locale]\ncurrent = \"es_es\"\n");
    assertEquals("es_es", ArclightConfig.load(config).getLocale().getCurrent());

    String localized = Files.readString(config).replace("\r\n", "\n");
    assertTrue(localized.contains("# Repositorio: https://github.com/CraftAmethyst/Luminara"));
    assertTrue(localized.contains("# Versión de la configuración, no editar.\n_v = 2"));
  }

  @Test
  void migratesLegacyValuesAndUsesTomlOnSubsequentLoads() throws Exception {
    Path legacy = directory.resolve(ArclightConfig.LEGACY_FILE_NAME);
    String yaml =
        """
        _v: 1
        locale:
          current: en_us
        compatibility:
          forward-permission: reverse
          preload-bungee-chat-classes: false
          enable-bukkit-reload-command: false
          extra-logic-worlds: [example.First, example.Second]
          material-property-overrides:
            'test:custom.item':
              maxStack: 16
              hardness: 1.25
        async-catcher:
          defaultOperation: block
          overrides:
            'reason.with.dot': exception
        velocity:
          forwarding-secret: 'secret#with"quotes'
        optimization:
          async-system:
            enabled: true
        """;
    Files.writeString(legacy, yaml);
    var migrated = ArclightConfig.load(configPath());

    assertEquals(PermissionForwarding.BUKKIT_TO_FORGE, migrated.getCompat().getPermissionForwarding());
    assertEquals(List.of("example.First", "example.Second"), migrated.getCompat().getExtraLogicWorlds());
    assertFalse(migrated.getCompat().isPreloadBungeeChatClasses());
    assertFalse(migrated.getCompat().isEnableBukkitReloadCommand());
    assertEquals(Operation.EXCEPTION, migrated.getAsyncCatcher().getOverrides().get("reason.with.dot"));
    assertEquals("secret#with\"quotes", migrated.getVelocity().getForwardingSecret());
    assertEquals(yaml, Files.readString(legacy));

    try (var reader = Files.newBufferedReader(configPath())) {
      assertFalse(new TomlParser().parse(reader).contains("optimization.async-system"));
    }
    Files.writeString(legacy, "invalid: [\n");
    var reloaded = ArclightConfig.load(configPath());
    var material = reloaded.getCompat().getMaterial("test:custom.item").orElseThrow();
    assertEquals(16, material.maxStack);
    assertEquals(1.25f, material.hardness);
    assertEquals(Operation.BLOCK, reloaded.getAsyncCatcher().getDefaultOp());
  }

  @Test
  void invalidTomlLeavesOriginalBytesUntouched() throws Exception {
    Path config = configPath();
    Files.createDirectories(config.getParent());
    String original = "_v = 2\ncompatibility = [\n";
    Files.writeString(config, original);

    assertThrows(IOException.class, () -> ArclightConfig.load(config));
    assertEquals(original, Files.readString(config));
  }

  @Test
  void invalidLegacyYamlDoesNotCreateToml() throws Exception {
    Path legacy = directory.resolve(ArclightConfig.LEGACY_FILE_NAME);
    String original = "_v: 1\ncompatibility: [\n";
    Files.writeString(legacy, original);

    assertThrows(IOException.class, () -> ArclightConfig.load(configPath()));
    assertEquals(original, Files.readString(legacy));
    assertFalse(Files.exists(configPath()));
  }
}
