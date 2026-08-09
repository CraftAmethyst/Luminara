package io.izzel.arclight.i18n;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

public final class ArclightLocale {

    private static ArclightLocale instance;

    private final String current;
    private final String fallback;
    private final ConfigurationNode node;

    private ArclightLocale(String current, String fallback, ConfigurationNode node) {
        this.current = current;
        this.fallback = fallback;
        this.node = node;
    }

    public String getCurrent() {
        return current;
    }

    public String current() {
        return current;
    }

    public String getFallback() {
        return fallback;
    }

    public String fallback() {
        return fallback;
    }

    public ConfigurationNode getNode() {
        return node;
    }

    public String format(String path, Object... args) {
        return MessageFormat.format(get(path), args);
    }

    public String get(String path) {
        return getOption(path).orElse(path);
    }

    public Optional<String> getOption(String path) {
        ConfigurationNode value = this.node.getNode((Object[]) path.split("\\."));
        if (value.getValueType() == ValueType.LIST) {
            StringJoiner joiner = new StringJoiner("\n");
            for (ConfigurationNode child : value.getChildrenList()) {
                joiner.add(child.getString());
            }
            return Optional.of(joiner.toString());
        }
        return Optional.ofNullable(value.getString());
    }

    public static void info(String path, Object... args) {
        System.out.println(instance.format(path, args));
    }

    public static void error(String path, Object... args) {
        System.err.println(instance.format(path, args));
    }

    public static ArclightLocale getInstance() {
        return instance;
    }

    private static void init() throws Exception {
        Map.Entry<String, String> locales = configuredLocales();
        String current = locales.getKey();
        String fallback = locales.getValue();
        ConfigurationNode fallbackNode = loadLocale(fallback);
        instance = new ArclightLocale(current, fallback, fallbackNode);
        if (!current.equals(fallback)) {
            try {
                ConfigurationNode currentNode = loadLocale(current);
                currentNode.mergeValuesFrom(fallbackNode);
                instance = new ArclightLocale(current, fallback, currentNode);
            } catch (Exception ignored) {
                System.err.println(instance.format("i18n.current-not-available", current));
            }
        }
    }

    private static ConfigurationNode loadLocale(String locale) throws Exception {
        InputStream stream = ArclightLocale.class.getResourceAsStream("/META-INF/i18n/" + locale + ".yml");
        if (stream == null) {
            throw new IllegalArgumentException("Locale is not found: " + locale);
        }
        return YAMLConfigurationLoader.builder().setSource(localeSource(locale)).build().load();
    }

    private static Callable<BufferedReader> localeSource(String locale) {
        return () -> {
            InputStream stream = ArclightLocale.class.getResourceAsStream("/META-INF/i18n/" + locale + ".yml");
            if (stream == null) {
                throw new IllegalArgumentException("Locale is not found: " + locale);
            }
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        };
    }

    private static Map.Entry<String, String> configuredLocales() {
        try {
            Path path = Paths.get("luminara.yml");
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Configuration does not exist");
            }
            ConfigurationNode root = YAMLConfigurationLoader.builder().setPath(path).build().load();
            ConfigurationNode locale = root.getNode("locale");
            return new AbstractMap.SimpleImmutableEntry<>(
                locale.getNode("current").getString(systemLocale()),
                locale.getNode("fallback").getString("en_us")
            );
        } catch (Exception ignored) {
            return new AbstractMap.SimpleImmutableEntry<>(systemLocale(), "en_us");
        }
    }

    private static String systemLocale() {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry().toLowerCase(Locale.ROOT);
        return locale.getLanguage().toLowerCase(Locale.ROOT) + (country.isEmpty() ? "" : "_" + country);
    }

    static {
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
