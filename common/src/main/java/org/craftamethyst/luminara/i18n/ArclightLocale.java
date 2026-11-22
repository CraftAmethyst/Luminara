package org.craftamethyst.luminara.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record ArclightLocale(String current, String fallback, ConfigurationNode node) {

  static final String DEFAULT_FALLBACK = "zh_cn";
  private static final Logger LOGGER = LogManager.getLogger("ArclightLocale-Debug");
  private static volatile ArclightLocale instance;

  public static void info(String path, Object... args) {
    System.out.println(getInstance().format(path, args));
  }

  public static void error(String path, Object... args) {
    System.err.println(getInstance().format(path, args));
  }

  public static ArclightLocale getInstance() {
    var locale = ArclightConfig.spec().getLocale();
    String current = locale.getCurrent() == null ? systemLocale() : locale.getCurrent();
    String fallback = locale.getFallback() == null ? DEFAULT_FALLBACK : locale.getFallback();
    ArclightLocale loaded = instance;
    if (loaded == null || !current.equals(loaded.current) || !fallback.equals(loaded.fallback)) {
      try {
        loaded = load(current, fallback);
        instance = loaded;
      } catch (IOException e) {
        throw new UncheckedIOException("Unable to load Luminara locale " + current, e);
      }
    }
    return loaded;
  }

  private static ArclightLocale load(String current, String fallback) throws IOException {
    ConfigurationNode fallbackNode = readBundle(fallback);
    if (current.equals(fallback)) {
      return new ArclightLocale(current, fallback, fallbackNode);
    }
    if (!hasBundle(current)) {
      ArclightLocale loaded = new ArclightLocale(current, fallback, fallbackNode);
      LOGGER.warn(loaded.format("i18n.current-not-available", current));
      return loaded;
    }

    ConfigurationNode currentNode = readBundle(current);
    currentNode.mergeValuesFrom(fallbackNode);
    return new ArclightLocale(current, fallback, currentNode);
  }

  static boolean hasBundle(String locale) {
    return ArclightLocale.class.getResource(bundlePath(locale)) != null;
  }

  static ConfigurationNode readBundle(String locale) throws IOException {
    var stream = ArclightLocale.class.getResourceAsStream(bundlePath(locale));
    if (stream == null) {
      throw new IOException("Locale is not available: " + locale);
    }
    try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      return YAMLConfigurationLoader.builder().setSource(() -> reader).build().load();
    }
  }

  private static String bundlePath(String locale) {
    return "/META-INF/i18n/" + locale + ".yml";
  }

  static String systemLocale() {
    Locale locale = Locale.getDefault();
    return (locale.getLanguage().toLowerCase(Locale.ROOT)
        + "_"
        + locale.getCountry().toLowerCase(Locale.ROOT));
  }

  public String format(String node, Object... args) {
    return MessageFormat.format(get(node), args);
  }

  public String get(String path) {
    if (path.contains("command") || path.contains("i18n") || path.contains("error")) {
      LOGGER.debug("Getting localized string for: {}", path);
    }
    String result = getOption(path).orElse(path);
    if ((path.contains("command") || path.contains("i18n") || path.contains("error"))
        && result.equals(path)) {
      LOGGER.debug("No translation found for: {}, returning original", path);
    }
    return result;
  }

  public Optional<String> getOption(String path) {
    ConfigurationNode node = this.node.getNode((Object[]) path.split("\\."));
    if (node.getValueType() == ValueType.LIST) {
      StringJoiner joiner = new StringJoiner("\n");
      for (ConfigurationNode configurationNode : node.getChildrenList()) {
        joiner.add(configurationNode.getString());
      }
      return Optional.of(joiner.toString());
    }
    return Optional.ofNullable(node.getString());
  }
}
