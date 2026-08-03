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

    public static String minecraftVersion() {
        return required("minecraftVersion");
    }

    public static String forgeVersion() {
        return required("forgeVersion");
    }

    public static String javaVersion() {
        return required("javaVersion");
    }

    public static String bukkitPackage() {
        return required("bukkitPackage");
    }

    public static String version() {
        return required("version");
    }

    public static String gitCommit() {
        return required("gitCommit");
    }

    public static void verify(String minecraftVersion, String forgeVersion) {
        if (!minecraftVersion().equals(minecraftVersion) || !forgeVersion().equals(forgeVersion)) {
            throw new IllegalStateException("Unsupported installer metadata: Minecraft " + minecraftVersion
                    + " / Forge " + forgeVersion + "; expected Minecraft " + minecraftVersion()
                    + " / Forge " + forgeVersion());
        }
        int runtimeFeature = Runtime.version().feature();
        int requiredFeature = Integer.parseInt(javaVersion());
        if (runtimeFeature < requiredFeature) {
            throw new IllegalStateException("Java " + requiredFeature + " is required; found " + runtimeFeature);
        }
    }

    public static String compatibilityLine() {
        return "Luminara " + version() + " (Minecraft " + minecraftVersion() + ", Forge " + forgeVersion()
                + ", Java " + javaVersion() + ", Bukkit " + bukkitPackage() + ", commit " + gitCommit() + ")";
    }

    static Properties load(InputStream input) {
        Objects.requireNonNull(input, "Missing " + RESOURCE);
        Properties properties = new Properties();
        try (input) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + RESOURCE, exception);
        }
        return properties;
    }

    static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing version metadata: " + key);
        }
        return value;
    }

    private static String required(String key) {
        return required(PROPERTIES, key);
    }
}
