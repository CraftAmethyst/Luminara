package org.craftamethyst.luminara.i18n;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.craftamethyst.luminara.i18n.conf.PermissionForwarding;

final class ConfigMigration {

  static final int CURRENT_VERSION = 2;
  private static final List<String> UNSAFE_SETTINGS =
      List.of(
          "optimization.async-system",
          "optimization.entity-optimization",
          "optimization.memory-optimization",
          "optimization.world-creation",
          "optimization.chunk-optimization",
          "async-world-save");

  private ConfigMigration() {}

  static CommentedConfig readLegacy(Path path) throws IOException {
    Object root = YAMLConfigurationLoader.builder().setPath(path).build().load().getValue();
    if (!(root instanceof Map<?, ?> map)) {
      throw new IOException("Expected a YAML mapping in " + path);
    }
    return toTomlTable(map);
  }

  private static CommentedConfig toTomlTable(Map<?, ?> map) {
    CommentedConfig table = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
    map.forEach(
        (key, value) -> {
          if (value != null) {
            table.set(List.of(String.valueOf(key)), toTomlValue(value));
          }
        });
    return table;
  }

  private static Object toTomlValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      return toTomlTable(map);
    }
    if (value instanceof List<?> list) {
      return list.stream().map(ConfigMigration::toTomlValue).toList();
    }
    if (value instanceof Date date) {
      return date.toInstant().atOffset(ZoneOffset.UTC);
    }
    return value;
  }

  static int version(UnmodifiableConfig config) {
    Object raw = config.get("_v");
    if (raw == null) {
      return 1;
    }
    try {
      int version = Integer.parseInt(raw.toString());
      if (version <= CURRENT_VERSION) {
        return version;
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid configuration version at _v: " + raw, e);
    }
    throw new IllegalArgumentException("Unsupported configuration version at _v: " + raw);
  }

  static List<String> migrate(CommentedConfig root) {
    List<String> removed = new ArrayList<>();
    if (version(root) < CURRENT_VERSION) {
      migratePermissionForwarding(root);
      for (String path : UNSAFE_SETTINGS) {
        if (root.contains(path)) {
          root.remove(path);
          root.removeComment(path);
          removed.add(path);
        }
      }
      root.set("_v", CURRENT_VERSION);
    }
    return removed;
  }

  private static void migratePermissionForwarding(CommentedConfig root) {
    Object raw = root.remove("compatibility.forward-permission");
    if (raw == null && root.contains("compatibility.permission-forwarding")) {
      return;
    }
    String value = raw == null ? "" : raw.toString().trim().toLowerCase(Locale.ROOT);
    PermissionForwarding forwarding =
        switch (value) {
          case "true" -> PermissionForwarding.FORGE_TO_BUKKIT;
          case "reverse" -> PermissionForwarding.BUKKIT_TO_FORGE;
          default -> PermissionForwarding.DISABLED;
        };
    root.set("compatibility.permission-forwarding", forwarding.name());
  }
}
