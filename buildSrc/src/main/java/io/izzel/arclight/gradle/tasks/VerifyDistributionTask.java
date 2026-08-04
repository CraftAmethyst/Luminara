package io.izzel.arclight.gradle.tasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class VerifyDistributionTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getDistributionJar();

    @InputFile
    public abstract RegularFileProperty getForgeInstallerJar();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getForgeVersion();

    @Input
    public abstract Property<String> getMainClass();

    @Input
    public abstract ListProperty<String> getRequiredEntries();

    @Input
    public abstract ListProperty<String> getRequiredCommonEntries();

    @Input
    public abstract MapProperty<String, String> getExpectedServices();

    @TaskAction
    public void verify() throws Exception {
        Set<String> classes = new HashSet<>();
        try (
            JarFile distribution = new JarFile(
                getDistributionJar().get().getAsFile()
            )
        ) {
            String mainClass = distribution
                .getManifest()
                .getMainAttributes()
                .getValue("Main-Class");
            require(
                getMainClass().get().equals(mainClass),
                "Unexpected Main-Class: " + mainClass
            );
            for (String entry : getRequiredEntries().get()) {
                require(
                    distribution.getEntry(entry) != null,
                    "Missing distribution entry: " + entry
                );
            }
            collectClasses(distribution, "distribution", classes);
            for (Map.Entry<String, String> service : getExpectedServices()
                .get()
                .entrySet()) {
                JarEntry entry = distribution.getJarEntry(
                    "META-INF/services/" + service.getKey()
                );
                require(
                    entry != null,
                    "Missing service provider: " + service.getKey()
                );
                String content = new String(
                    distribution.getInputStream(entry).readAllBytes(),
                    StandardCharsets.UTF_8
                );
                require(
                    content
                        .lines()
                        .map(String::trim)
                        .anyMatch(service.getValue()::equals),
                    "Missing service implementation " + service.getValue()
                );
            }
            verifyInstaller(distribution);
            verifyNestedJar(
                distribution,
                "common.jar",
                getRequiredCommonEntries().get(),
                classes
            );
            verifyNestedJar(
                distribution,
                "gson.jar",
                java.util.List.of(),
                classes
            );
        }
    }

    private void verifyInstaller(JarFile distribution) throws Exception {
        JsonObject root = JsonParser.parseReader(
            new InputStreamReader(
                distribution.getInputStream(
                    distribution.getJarEntry("META-INF/installer.json")
                ),
                StandardCharsets.UTF_8
            )
        ).getAsJsonObject();
        JsonObject installer = root.getAsJsonObject("installer");
        require(
            getMinecraftVersion()
                .get()
                .equals(installer.get("minecraft").getAsString()),
            "Unexpected installer Minecraft version"
        );
        require(
            getForgeVersion()
                .get()
                .equals(installer.get("forge").getAsString()),
            "Unexpected installer Forge version"
        );
        String expected = installer.get("hash").getAsString();
        String actual = hash(
            getForgeInstallerJar().get().getAsFile().toPath(),
            "SHA-1"
        );
        require(expected.equals(actual), "Forge installer SHA-1 mismatch");
        ensureUniqueCoordinates(root.getAsJsonObject("libraries"));
        JsonObject libraries = root.getAsJsonObject("libraries");
        require(
            root.has("runtimeLibraries"),
            "Missing runtime library metadata"
        );
        root.getAsJsonArray("runtimeLibraries").forEach(coordinate ->
            require(
                libraries.has(coordinate.getAsString()),
                "Runtime library lacks download metadata: " +
                    coordinate.getAsString()
            )
        );
    }

    private static void ensureUniqueCoordinates(JsonObject libraries) {
        Map<String, String> versions = new HashMap<>();
        for (String coordinate : libraries.keySet()) {
            String[] parts = coordinate.split(":");
            require(
                parts.length >= 3,
                "Malformed installer coordinate: " + coordinate
            );
            String ga = parts[0] + ":" + parts[1];
            String previous = versions.putIfAbsent(ga, parts[2]);
            require(
                previous == null || previous.equals(parts[2]),
                "Multiple installer versions for " +
                    ga +
                    ": " +
                    previous +
                    " and " +
                    parts[2]
            );
        }
    }

    private static void verifyNestedJar(
        JarFile distribution,
        String nestedName,
        java.util.List<String> requiredEntries,
        Set<String> classes
    ) throws IOException {
        JarEntry nestedEntry = distribution.getJarEntry(nestedName);
        java.nio.file.Path temporary = Files.createTempFile(
            "luminara-nested",
            ".jar"
        );
        try {
            Files.copy(
                distribution.getInputStream(nestedEntry),
                temporary,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            try (JarFile nested = new JarFile(temporary.toFile())) {
                for (String required : requiredEntries) {
                    require(
                        nested.getEntry(required) != null,
                        "Missing " + required + " from " + nestedName
                    );
                }
                collectClasses(nested, nestedName, classes);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void collectClasses(
        JarFile archive,
        String origin,
        Set<String> classes
    ) {
        var entries = archive.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (
                name.endsWith(".class") && !name.endsWith("module-info.class")
            ) {
                require(
                    classes.add(name),
                    "Duplicate class path " + name + " in " + origin
                );
            }
        }
    }

    private static String hash(java.nio.file.Path path, String algorithm)
        throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0)
                digest.update(buffer, 0, length);
        }
        return java.util.HexFormat.of().formatHex(digest.digest());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GradleException(message);
    }
}
