package io.izzel.arclight.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class LuminaraVersion {

    private static final String RESOURCE = "/META-INF/luminara-version.properties";
    private static final Properties PROPERTIES = load(LuminaraVersion.class.getResourceAsStream(RESOURCE));

    private LuminaraVersion() {
    }

    public static String minecraftVersion() { return required("minecraftVersion"); }
    public static String loader() { return required("loader"); }
    public static String loaderVersion() { return required("loaderVersion"); }
    public static String javaVersion() { return required("javaVersion"); }
    public static String bukkitPackage() { return required("bukkitPackage"); }
    public static String version() { return required("version"); }
    public static String gitCommit() { return required("gitCommit"); }

    public static String compatibilityLine() {
        return minecraftVersion() + " / " + loader() + " " + loaderVersion() + " / Java " + javaVersion();
    }

    static Properties load(InputStream input) {
        Objects.requireNonNull(input, "Missing " + RESOURCE);
        Properties properties = new Properties();
        try (input) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load " + RESOURCE, e);
        }
        return properties;
    }

    static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing runtime metadata key: " + key);
        }
        return value;
    }

    private static String required(String key) {
        return required(PROPERTIES, key);
    }
}
