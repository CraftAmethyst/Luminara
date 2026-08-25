package io.izzel.arclight.gradle.tasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Verifies the contract of a standalone Luminara mod JAR (NeoForge or Fabric).
 * <p>
 * The mod JAR is the primary install artifact and must not carry any legacy
 * launcher runtime: no Main-Class, no installer metadata and no
 * {@code io.izzel.arclight.boot.*} launch chain. Libraries are either merged into the
 * JAR or declared as jar-in-jar entries, and the two must never overlap.
 */
public abstract class VerifyModDistributionTask extends DefaultTask {

    private static final String JARJAR_METADATA = "META-INF/jarjar/metadata.json";

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getDistributionJar();

    @Input public abstract Property<String> getExpectedFileName();
    @Input public abstract Property<String> getModuleName();
    @Input public abstract Property<String> getLoader();
    @Input public abstract Property<String> getLoaderVersion();
    @Input public abstract Property<String> getMinecraftVersion();
    @Input public abstract Property<String> getJavaVersion();
    @Input public abstract Property<String> getProjectVersion();
    @Input public abstract Property<String> getGitCommit();
    @Input public abstract Property<String> getGitTimestamp();
    @Input public abstract ListProperty<String> getRequiredEntries();
    @Input public abstract ListProperty<String> getForbiddenEntries();

    @TaskAction
    public void verify() throws IOException {
        var archive = getDistributionJar().get().getAsFile();
        if (!archive.getName().equals(getExpectedFileName().get())) {
            fail("expected file name " + getExpectedFileName().get() + ", found " + archive.getName());
        }

        try (var zip = new ZipFile(archive, StandardCharsets.UTF_8)) {
            verifyManifest(zip);
            for (var entry : getRequiredEntries().get()) {
                requireEntry(zip, entry);
            }
            verifyForbidden(zip);
            verifyVersionProperties(zip);
            verifyDuplicateClasses(zip);
            verifyNestedLibraries(zip);
        }
    }

    private void verifyManifest(ZipFile zip) throws IOException {
        var entry = zip.getEntry("META-INF/MANIFEST.MF");
        if (entry == null) fail("missing META-INF/MANIFEST.MF");
        Manifest manifest;
        try (var input = zip.getInputStream(entry)) {
            manifest = new Manifest(input);
        }
        var attributes = manifest.getMainAttributes();
        var mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
        if (mainClass != null) {
            fail("standalone mod JAR must not declare a Main-Class, found " + mainClass);
        }
        expect(attributes, "MixinConnector", "io.izzel.arclight.common.mod.ArclightConnector");
        expect(attributes, "Implementation-Version", getModuleName().get() + "-" + getMinecraftVersion().get()
            + "-" + getProjectVersion().get() + "+" + getGitCommit().get());
        expect(attributes, "Implementation-Timestamp", getGitTimestamp().get());
    }

    private void expect(Attributes attributes, String key, String expected) {
        var actual = attributes.getValue(key);
        if (!expected.equals(actual)) fail("manifest " + key + " expected " + expected + ", found " + actual);
    }

    private void verifyForbidden(ZipFile zip) {
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            var name = entries.nextElement().getName();
            for (var forbidden : getForbiddenEntries().get()) {
                if (forbidden.endsWith("/")) {
                    if (name.startsWith(forbidden)) fail("standalone mod JAR must not contain " + name);
                } else if (name.equals(forbidden)) {
                    fail("standalone mod JAR must not contain " + name);
                }
            }
        }
    }

    private void verifyVersionProperties(ZipFile zip) throws IOException {
        var properties = new Properties();
        try (var input = zip.getInputStream(requireEntry(zip, "META-INF/luminara-version.properties"))) {
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        expect(properties, "minecraftVersion", getMinecraftVersion().get());
        expect(properties, "loader", getLoader().get());
        expect(properties, "loaderVersion", getLoaderVersion().get());
        expect(properties, "javaVersion", getJavaVersion().get());
        expect(properties, "bukkitPackage", "v1_21_R1");
        expect(properties, "version", getProjectVersion().get() + "+" + getGitCommit().get());
        expect(properties, "gitCommit", getGitCommit().get());
    }

    private void expect(Properties properties, String key, String expected) {
        var actual = properties.getProperty(key);
        if (!expected.equals(actual)) fail("version property " + key + " expected " + expected + ", found " + actual);
    }

    private void verifyDuplicateClasses(ZipFile zip) {
        var owners = new HashMap<String, ZipEntry>();
        var duplicates = new HashSet<String>();
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            registerClass(entry.getName(), entry, owners, duplicates);
        }
        if (!duplicates.isEmpty()) fail("duplicate classes in mod JAR: " + String.join(", ", duplicates));
    }

    private void registerClass(String path, ZipEntry entry, HashMap<String, ZipEntry> owners, Set<String> duplicates) {
        if (!path.endsWith(".class") || path.equals("module-info.class") || path.endsWith("/module-info.class")) return;
        var previous = owners.putIfAbsent(path, entry);
        if (previous != null) duplicates.add(path);
    }

    /**
     * A mod JAR is an automatic module that exports every package it contains, so a merged
     * library clashes with the same library shipped by another mod. Libraries that keep their
     * original package names are nested instead, and NeoForge selects a single copy per
     * {@code group:artifact}. That only holds while the nested copy is the sole one.
     */
    private void verifyNestedLibraries(ZipFile zip) throws IOException {
        var metadataEntry = zip.getEntry(JARJAR_METADATA);
        if (metadataEntry == null) return;

        JsonObject metadata;
        try (var reader = new InputStreamReader(zip.getInputStream(metadataEntry), StandardCharsets.UTF_8)) {
            metadata = JsonParser.parseReader(reader).getAsJsonObject();
        }
        var jars = metadata.getAsJsonArray("jars");
        if (jars == null || jars.isEmpty()) fail(JARJAR_METADATA + " declares no nested library");

        var merged = classPackages(zip);
        for (var element : jars) {
            var jar = element.getAsJsonObject();
            var identifier = jar.getAsJsonObject("identifier");
            var artifact = identifier.get("group").getAsString() + ":" + identifier.get("artifact").getAsString();
            var range = jar.getAsJsonObject("version").get("range").getAsString();
            // JarSelector only intersects real ranges; a bare version makes every other copy
            // of the same library fail resolution instead of losing the selection.
            if (!range.startsWith("[") && !range.startsWith("(")) {
                fail("nested library " + artifact + " declares version " + range + " instead of a Maven range");
            }
            var path = jar.get("path").getAsString();
            for (var packageName : nestedPackages(zip, requireEntry(zip, path))) {
                if (merged.contains(packageName)) {
                    fail("package " + packageName + " is both merged into the mod JAR and nested in " + path);
                }
            }
        }
    }

    private Set<String> classPackages(ZipFile zip) {
        var packages = new HashSet<String>();
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            registerPackage(entries.nextElement().getName(), packages);
        }
        return packages;
    }

    private Set<String> nestedPackages(ZipFile zip, ZipEntry entry) throws IOException {
        var packages = new HashSet<String>();
        try (var nested = new ZipInputStream(zip.getInputStream(entry))) {
            for (ZipEntry nestedEntry; (nestedEntry = nested.getNextEntry()) != null; ) {
                registerPackage(nestedEntry.getName(), packages);
            }
        }
        return packages;
    }

    private void registerPackage(String path, Set<String> packages) {
        // Multi release copies live under META-INF and are not packages of their own.
        if (!path.endsWith(".class") || path.startsWith("META-INF/")) return;
        var separator = path.lastIndexOf('/');
        if (separator < 0) return;
        packages.add(path.substring(0, separator).replace('/', '.'));
    }

    private ZipEntry requireEntry(ZipFile zip, String path) {
        var entry = zip.getEntry(path);
        if (entry == null) fail("missing " + path);
        return entry;
    }

    private void fail(String detail) {
        throw new GradleException(getLoader().get() + " mod distribution "
            + getDistributionJar().get().getAsFile().getName() + ": " + detail);
    }
}