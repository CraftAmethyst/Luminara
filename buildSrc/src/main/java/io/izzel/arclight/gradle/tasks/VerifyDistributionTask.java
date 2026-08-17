package io.izzel.arclight.gradle.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class VerifyDistributionTask extends DefaultTask {

    private static final Pattern SHA1 = Pattern.compile("[0-9a-f]{40}");

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getDistributionJar();

    @Input public abstract Property<String> getExpectedFileName();
    @Input public abstract Property<String> getLoader();
    @Input public abstract Property<String> getLoaderVersion();
    @Input public abstract Property<String> getExpectedNeoForgeVersion();
    @Input public abstract Property<String> getExpectedFabricLoaderVersion();
    @Input public abstract Property<String> getMinecraftVersion();
    @Input public abstract Property<String> getJavaVersion();
    @Input public abstract Property<String> getProjectVersion();
    @Input public abstract Property<String> getGitCommit();
    @Input public abstract Property<String> getGitTimestamp();
    @Input public abstract Property<String> getMainClass();
    @Input public abstract Property<String> getLaunchMainClass();
    @Input public abstract ListProperty<String> getRequiredEntries();
    @Input public abstract ListProperty<String> getRequiredCommonEntries();
    @Input public abstract ListProperty<String> getForbiddenCommonEntries();
    @Input public abstract MapProperty<String, String> getExpectedServices();

    /**
     * Class path prefixes that are intentionally allowed to appear both in the outer
     * launcher JAR and in the embedded {@code common.jar}. During the transition the
     * standalone mod JARs carry their own runtime libraries (arclight-api, i18n-config,
     * mixin-tools, io.izzel tools) while the legacy launcher keeps embedding the same
     * libraries for its own early bootstrap. The classes are the same library versions,
     * so the classpath duplication is benign; the duplicate check still catches real
     * version conflicts of any other class.
     */
    @Input public abstract ListProperty<String> getIgnoredDuplicatePrefixes();

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
            verifyServices(zip);

            var common = readNonEmptyEntry(zip, "common.jar");
            var gson = readNonEmptyEntry(zip, "gson.jar");
            verifyCommon(common);
            verifyVersionProperties(zip);
            verifyInstaller(zip);
            verifyLaunchProperties(zip);
            verifyDuplicateClasses(zip, common, gson);
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
        expect(attributes, Attributes.Name.MAIN_CLASS.toString(), getMainClass().get());
        expect(attributes, "Implementation-Version", "luminara-" + getMinecraftVersion().get() + "-"
            + getProjectVersion().get() + "-" + getGitCommit().get());
        expect(attributes, "Implementation-Timestamp", getGitTimestamp().get());
        expect(attributes, "Automatic-Module-Name", "arclight.boot");
    }

    private void expect(Attributes attributes, String key, String expected) {
        var actual = attributes.getValue(key);
        if (!expected.equals(actual)) fail("manifest " + key + " expected " + expected + ", found " + actual);
    }

    private void verifyServices(ZipFile zip) throws IOException {
        for (var service : getExpectedServices().get().entrySet()) {
            var path = "META-INF/services/" + service.getKey();
            var actual = readText(zip, path).trim();
            if (!service.getValue().equals(actual)) {
                fail(path + " expected provider " + service.getValue() + ", found " + actual);
            }
        }
    }

    private void verifyCommon(byte[] common) throws IOException {
        var entries = nestedEntries(common, "common.jar");
        for (var entry : getRequiredCommonEntries().get()) {
            if (!entries.contains(entry)) fail("common.jar is missing " + entry);
        }
        for (var entry : getForbiddenCommonEntries().get()) {
            if (entries.contains(entry)) fail("common.jar contains forbidden entry " + entry);
        }
    }

    private void verifyVersionProperties(ZipFile zip) throws IOException {
        var properties = new Properties();
        try (var input = entryStream(zip, "META-INF/luminara-version.properties")) {
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

    private void verifyInstaller(ZipFile zip) throws IOException {
        JsonObject root;
        try (var reader = new InputStreamReader(entryStream(zip, "META-INF/installer.json"), StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }
        var installer = requiredObject(root, "installer");
        expect(installer, "minecraft", getMinecraftVersion().get());
        expect(installer, "neoforge", getExpectedNeoForgeVersion().get());
        expect(installer, "fabricLoader", getExpectedFabricLoaderVersion().get());
        verifySha1(installer, "neoforgeHash");
        verifySha1(installer, "fabricLoaderHash");

        var versions = new HashMap<String, String>();
        verifyCoordinates(requiredObject(root, "libraries"), versions);
        verifyCoordinates(requiredObject(root, "fabricExtra"), versions);
    }

    private void verifyCoordinates(JsonObject coordinates, Map<String, String> versions) {
        for (var item : coordinates.entrySet()) {
            if (item.getKey().isBlank() || item.getValue().getAsString().isBlank()) {
                fail("installer coordinate and SHA-1 values must be non-empty");
            }
            verifySha1(item.getValue(), item.getKey());
            var parts = item.getKey().split(":");
            if (parts.length < 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                fail("invalid installer coordinate " + item.getKey());
            }
            var module = parts[0] + ":" + parts[1];
            var previous = versions.putIfAbsent(module, parts[2]);
            if (previous != null && !previous.equals(parts[2])) {
                fail("installer contains conflicting versions for " + module + ": " + previous + " and " + parts[2]);
            }
        }
    }

    private void verifySha1(JsonObject object, String key) {
        var element = object.get(key);
        if (element == null) fail("installer is missing " + key);
        verifySha1(element, key);
    }

    private void verifySha1(JsonElement element, String label) {
        var value = element.getAsString();
        if (!SHA1.matcher(value).matches()) fail(label + " is not a valid SHA-1: " + value);
    }

    private void expect(JsonObject object, String key, String expected) {
        var element = object.get(key);
        var actual = element == null ? null : element.getAsString();
        if (!expected.equals(actual)) fail("installer " + key + " expected " + expected + ", found " + actual);
    }

    private JsonObject requiredObject(JsonObject parent, String key) {
        var element = parent.get(key);
        if (element == null || !element.isJsonObject()) fail("installer is missing object " + key);
        return element.getAsJsonObject();
    }

    private void verifyLaunchProperties(ZipFile zip) throws IOException {
        var properties = new Properties();
        try (var input = entryStream(zip, "arclight-server-launch.properties")) {
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        var actual = properties.getProperty("launch.mainClass");
        if (!getLaunchMainClass().get().equals(actual)) {
            fail("launch.mainClass expected " + getLaunchMainClass().get() + ", found " + actual);
        }
    }

    private void verifyDuplicateClasses(ZipFile outer, byte[] common, byte[] gson) throws IOException {
        var owners = new HashMap<String, byte[]>();
        var duplicates = new HashSet<String>();
        var entries = outer.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            registerClass(outer, entry, "outer JAR", owners, duplicates);
        }
        registerNestedClasses(common, "common.jar", owners, duplicates);
        registerNestedClasses(gson, "gson.jar", owners, duplicates);
        if (!duplicates.isEmpty()) fail("duplicate classes across archives: " + String.join(", ", duplicates));
    }

    private void registerNestedClasses(byte[] bytes, String owner, Map<String, byte[]> owners, Set<String> duplicates) throws IOException {
        try (var jar = new JarInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = jar.getNextEntry()) != null) {
                registerClass(jar, entry, owner, owners, duplicates);
            }
        }
    }

    private void registerClass(ZipFile zip, ZipEntry entry, String owner, Map<String, byte[]> owners, Set<String> duplicates) throws IOException {
        if (!isClass(entry.getName())) return;
        var content = readAllBytes(zip.getInputStream(entry));
        register(entry.getName(), content, owners, duplicates);
    }

    private void registerClass(JarInputStream jar, ZipEntry entry, String owner, Map<String, byte[]> owners, Set<String> duplicates) throws IOException {
        if (!isClass(entry.getName())) return;
        register(entry.getName(), jar.readAllBytes(), owners, duplicates);
    }

    private void register(String path, byte[] content, Map<String, byte[]> owners, Set<String> duplicates) {
        if (isIgnoredDuplicate(path)) return;
        var previous = owners.putIfAbsent(path, content);
        if (previous != null && !Arrays.equals(previous, content)) {
            duplicates.add(path + " (" + previous.length + " vs " + content.length + " bytes)");
        }
    }

    private boolean isIgnoredDuplicate(String path) {
        for (var prefix : getIgnoredDuplicatePrefixes().get()) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private byte[] readAllBytes(InputStream input) throws IOException {
        return input.readAllBytes();
    }

    private boolean isClass(String path) {
        return path.endsWith(".class") && !path.equals("module-info.class") && !path.endsWith("/module-info.class");
    }

    private Set<String> nestedEntries(byte[] bytes, String label) throws IOException {
        var entries = new HashSet<String>();
        try (var jar = new JarInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = jar.getNextEntry()) != null) entries.add(entry.getName());
        } catch (IOException exception) {
            fail(label + " cannot be opened: " + exception.getMessage());
        }
        return entries;
    }

    private byte[] readNonEmptyEntry(ZipFile zip, String path) throws IOException {
        var entry = requireEntry(zip, path);
        if (entry.getSize() == 0) fail(path + " is empty");
        try (var input = zip.getInputStream(entry)) {
            var bytes = input.readAllBytes();
            if (bytes.length == 0) fail(path + " is empty");
            return bytes;
        }
    }

    private String readText(ZipFile zip, String path) throws IOException {
        try (var input = entryStream(zip, path)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private InputStream entryStream(ZipFile zip, String path) throws IOException {
        return zip.getInputStream(requireEntry(zip, path));
    }

    private ZipEntry requireEntry(ZipFile zip, String path) {
        var entry = zip.getEntry(path);
        if (entry == null) fail("missing " + path);
        return entry;
    }

    private void fail(String detail) {
        throw new GradleException(getLoader().get() + " distribution "
            + getDistributionJar().get().getAsFile().getName() + ": " + detail);
    }
}
