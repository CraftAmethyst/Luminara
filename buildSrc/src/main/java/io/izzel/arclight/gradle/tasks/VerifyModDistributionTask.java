package io.izzel.arclight.gradle.tasks;

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

/**
 * Verifies the contract of a standalone Luminara mod JAR (NeoForge or Fabric).
 * <p>
 * The mod JAR is the primary install artifact and must not carry any legacy
 * launcher runtime: no Main-Class, no embedded jars, no installer metadata and
 * no {@code io.izzel.arclight.boot.*} launch chain.
 */
public abstract class VerifyModDistributionTask extends DefaultTask {

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