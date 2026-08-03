package io.izzel.arclight.common.mod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public record LuminaraVersion(String luminara, String minecraft, String forge, String craftBukkitPackage,
                              String gitCommit) {

    private static final String RESOURCE = "/META-INF/luminara-version.properties";
    private static final LuminaraVersion CURRENT = load(LuminaraVersion.class.getResourceAsStream(RESOURCE));

    public static LuminaraVersion current() {
        return CURRENT;
    }

    static LuminaraVersion load(InputStream input) {
        Objects.requireNonNull(input, "Missing " + RESOURCE);
        Properties properties = new Properties();
        try (input) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + RESOURCE, exception);
        }
        return new LuminaraVersion(
                required(properties, "luminara.version"),
                required(properties, "minecraft.version"),
                required(properties, "forge.version"),
                required(properties, "craftbukkit.package"),
                required(properties, "git.commit")
        );
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing version metadata: " + key);
        }
        return value;
    }

    public String compatibilityLine() {
        return "Luminara " + luminara + " (Minecraft " + minecraft + ", Forge " + forge
                + ", Bukkit " + craftBukkitPackage + ", commit " + gitCommit + ")";
    }
}
