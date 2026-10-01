package io.izzel.arclight.gradle.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects the third party libraries the mod needs at run time into a JarJar payload and
 * writes the {@code META-INF/jarjar/metadata.json} Forge reads to load them.
 * <p>
 * Merging those libraries into the mod JAR instead does not work on the module path: the
 * mod JAR becomes an automatic module that exports every package it holds, so a bundled
 * copy of a library Forge already provides makes Java refuse to build the game layer with
 * a split-package error. JarJar keeps each library as its own module.
 */
public abstract class BundleJarJarTask extends DefaultTask {

    private static final String JARJAR_DIRECTORY = "META-INF/jarjar";

    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract ConfigurableFileCollection getLibraries();

    /** Maps the file name of each library to the {@code group:artifact:version} it provides. */
    @Input
    public abstract MapProperty<String, String> getCoordinates();

    @OutputFile
    public abstract RegularFileProperty getMetadataFile();

    @TaskAction
    public void bundle() throws IOException {
        Path outputDirectory = getMetadataFile()
            .get()
            .getAsFile()
            .toPath()
            .getParent();
        Files.createDirectories(outputDirectory);

        Map<String, String> byFileName = new LinkedHashMap<>();
        var libraries = getLibraries()
            .getFiles()
            .stream()
            .map(java.io.File::toPath)
            .sorted(Comparator.comparing(path -> path.getFileName().toString()))
            .toList();
        for (Path library : libraries) {
            String fileName = library.getFileName().toString();
            Files.copy(
                library,
                outputDirectory.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
            );
            byFileName.put(fileName, coordinateOf(fileName));
        }

        JsonArray jars = new JsonArray();
        byFileName.forEach((fileName, coordinate) -> {
            String[] parts = coordinate.split(":");
            JsonObject identifier = new JsonObject();
            identifier.addProperty("group", parts[0]);
            identifier.addProperty("artifact", parts[1]);
            JsonObject version = new JsonObject();
            version.addProperty("range", "[" + parts[2] + ",)");
            version.addProperty("artifactVersion", parts[2]);
            JsonObject jar = new JsonObject();
            jar.add("identifier", identifier);
            jar.add("version", version);
            jar.addProperty("path", JARJAR_DIRECTORY + "/" + fileName);
            jar.addProperty("isObfuscated", false);
            jars.add(jar);
        });

        JsonObject metadata = new JsonObject();
        metadata.add("jars", jars);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(
            getMetadataFile().get().getAsFile().toPath(),
            gson.toJson(metadata) + "\n",
            StandardCharsets.UTF_8
        );
    }

    private String coordinateOf(String fileName) {
        String coordinate = getCoordinates().get().get(fileName);
        if (coordinate == null) {
            throw new IllegalStateException(
                "No Maven coordinate declared for bundled library " + fileName
            );
        }
        if (coordinate.split(":").length != 3) {
            throw new IllegalStateException(
                "Malformed coordinate for " + fileName + ": " + coordinate
            );
        }
        return coordinate;
    }
}
