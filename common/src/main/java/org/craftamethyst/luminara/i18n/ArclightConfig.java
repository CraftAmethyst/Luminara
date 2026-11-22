package org.craftamethyst.luminara.i18n;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.craftamethyst.luminara.common.mod.ArclightCommon;
import org.craftamethyst.luminara.i18n.conf.ConfigSpec;

public final class ArclightConfig {

  public static final String FILE_NAME = "luminara-common.toml";
  static final String LEGACY_FILE_NAME = "luminara.yml";

  private ArclightConfig() {}

  private static final class LoadedConfig {
    private static volatile ConfigSpec spec = loadPlatformConfig();
  }

  public static ConfigSpec spec() {
    return LoadedConfig.spec;
  }

  public static void applyLogging() {
    LoggingConfigurator.apply(spec());
  }

  static void useConfig(CommentedConfig config) {
    LoadedConfig.spec = readSpec(config);
  }

  private static ConfigSpec loadPlatformConfig() {
    Path path = ArclightCommon.api().getConfigDirectory().resolve(FILE_NAME);
    try {
      return load(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load Luminara configuration " + path, e);
    }
  }

  static ConfigSpec load(Path path) throws IOException {
    Path legacy = path.toAbsolutePath().getParent().getParent().resolve(LEGACY_FILE_NAME);
    boolean create = Files.notExists(path);
    CommentedConfig config;
    Path source = path;
    try {
      if (!create) {
        try (var reader = Files.newBufferedReader(path)) {
          config = new TomlParser().parse(reader);
        }
      } else if (!Files.notExists(legacy)) {
        source = legacy;
        config = ConfigMigration.readLegacy(legacy);
      } else {
        config = defaults();
      }
      boolean changed = prepare(config);
      ConfigSpec spec = readSpec(config);
      if (create || changed) {
        saveAtomically(path, config);
      }
      return spec;
    } catch (ParsingException | IllegalArgumentException e) {
      throw new IOException("Invalid Luminara configuration " + source, e);
    }
  }

  static CommentedConfig defaults() {
    String resource = "/META-INF/" + FILE_NAME;
    var stream = Objects.requireNonNull(ArclightConfig.class.getResourceAsStream(resource), resource);
    try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return new TomlParser().parse(reader);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to read " + resource, e);
    }
  }

  static boolean needsUpdate(CommentedConfig config) {
    return ConfigMigration.version(config) != ConfigMigration.CURRENT_VERSION
        || !config.contains("locale.current")
        || !I18nCommentInjector.hasLocalizedComments(config);
  }

  static boolean prepare(CommentedConfig config) {
    if (!needsUpdate(config)) {
      return false;
    }
    var removed = ConfigMigration.migrate(config);
    if (!config.contains("locale.current")) {
      config.set("locale.current", ArclightLocale.systemLocale());
    }
    readSpec(config);
    I18nCommentInjector.localize(config);
    removed.forEach(setting -> System.err.println("Removed unsupported unsafe setting: " + setting));
    return true;
  }

  private static ConfigSpec readSpec(CommentedConfig config) {
    var node = SimpleConfigurationNode.root().setValue(toNodeValue(config));
    try {
      return node.getValue(TypeToken.of(ConfigSpec.class));
    } catch (ObjectMappingException e) {
      throw new IllegalArgumentException("Unable to map Luminara configuration", e);
    }
  }

  private static Object toNodeValue(Object value) {
    if (value instanceof UnmodifiableConfig config) {
      Map<String, Object> map = new LinkedHashMap<>();
      config.valueMap().forEach((key, entry) -> map.put(key, toNodeValue(entry)));
      return map;
    }
    if (value instanceof List<?> list) {
      return list.stream().map(ArclightConfig::toNodeValue).toList();
    }
    return value;
  }

  private static void saveAtomically(Path path, CommentedConfig config) throws IOException {
    Path absolute = path.toAbsolutePath();
    Path parent = Files.createDirectories(absolute.getParent());
    Path temporary = Files.createTempFile(parent, FILE_NAME, ".tmp");
    try {
      try (var writer = Files.newBufferedWriter(temporary)) {
        new TomlWriter().write(config, writer);
      }
      try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
        channel.force(true);
      }
      try {
        Files.move(
            temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }
}
