package io.izzel.arclight.gradle.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Packages third party libraries as NeoForge jar-in-jar entries instead of merging their
 * classes into the mod JAR.
 * <p>
 * A mod JAR is exposed to the module system as an automatic module that exports every
 * package it contains, so a shaded library collides with the same library shipped by any
 * other mod ({@code Modules org.yaml.snakeyaml and arclight export package
 * org.yaml.snakeyaml.reader to module ...}). Nested libraries are instead resolved by
 * {@code net.neoforged.jarjar.selection.JarSelector}, which keeps a single copy per
 * {@code group:artifact} across all installed mods.
 */
public abstract class BundleJarJarTask extends DefaultTask {

    private static final String JARJAR_DIRECTORY = "META-INF/jarjar";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract ConfigurableFileCollection getLibraries();

    /**
     * Maps every {@link #getLibraries()} file name to its {@code group:artifact:version}.
     */
    @Input
    public abstract MapProperty<String, String> getLibraryCoordinates();

    /**
     * Optional Maven version range per {@code group:artifact}, defaults to {@code [version,)}.
     * A range must stay open ended, otherwise a mod shipping a newer copy of the same library
     * fails the whole dependency resolution instead of winning the selection.
     */
    @Input
    public abstract MapProperty<String, String> getVersionRanges();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void bundle() throws IOException {
        var root = getOutputDirectory().get().getAsFile().toPath();
        deleteRecursively(root);
        var directory = root.resolve(JARJAR_DIRECTORY);
        Files.createDirectories(directory);

        var coordinates = getLibraryCoordinates().get();
        var ranges = getVersionRanges().get();
        var files = new ArrayList<>(getLibraries().getFiles());
        files.sort(Comparator.comparing(File::getName));

        var jars = new JsonArray();
        for (var file : files) {
            jars.add(describe(file, coordinates, ranges));
            Files.copy(file.toPath(), directory.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
        if (jars.isEmpty()) {
            throw new GradleException("no jar-in-jar libraries resolved, remove the task instead");
        }

        var metadata = new JsonObject();
        metadata.add("jars", jars);
        Files.writeString(directory.resolve("metadata.json"), GSON.toJson(metadata) + "\n", StandardCharsets.UTF_8);
    }

    private JsonObject describe(File file, Map<String, String> coordinates, Map<String, String> ranges) {
        var coordinate = coordinates.get(file.getName());
        if (coordinate == null) {
            throw new GradleException("missing coordinates for jar-in-jar library " + file.getName());
        }
        var parts = coordinate.split(":");
        if (parts.length != 3) {
            throw new GradleException("expected group:artifact:version for " + file.getName() + ", found " + coordinate);
        }
        var group = parts[0];
        var artifact = parts[1];
        var version = parts[2];
        var range = ranges.getOrDefault(group + ":" + artifact, "[" + version + ",)");
        if (!range.startsWith("[") && !range.startsWith("(")) {
            throw new GradleException("version range " + range + " of " + group + ":" + artifact
                + " must be a Maven range, a bare version makes JarSelector reject every other copy");
        }

        var identifier = new JsonObject();
        identifier.addProperty("group", group);
        identifier.addProperty("artifact", artifact);
        var containedVersion = new JsonObject();
        containedVersion.addProperty("range", range);
        containedVersion.addProperty("artifactVersion", version);

        var entry = new JsonObject();
        entry.add("identifier", identifier);
        entry.add("version", containedVersion);
        entry.addProperty("path", JARJAR_DIRECTORY + "/" + file.getName());
        entry.addProperty("isObfuscated", false);
        return entry;
    }

    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            for (var path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
