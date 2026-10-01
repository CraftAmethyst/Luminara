package io.izzel.arclight.gradle.tasks;

import com.google.gson.JsonArray;
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
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Validates that the assembled artifact is a standard Forge mod JAR and that no legacy
 * launcher entry survived the migration.
 */
public abstract class VerifyForgeModTask extends DefaultTask {

    private static final Set<String> FORBIDDEN_ENTRIES = Set.of(
        "common.jar",
        "gson.jar",
        "META-INF/installer.json",
        "META-INF/services/java.util.function.Consumer",
        "META-INF/services/cpw.mods.modlauncher.api.ILaunchHandlerService",
        "META-INF/services/cpw.mods.modlauncher.serviceapi.ILaunchPluginService",
        "META-INF/services/net.minecraftforge.forgespi.locating.IModLocator",
        "io/izzel/arclight/server/Launcher.class"
    );

    private static final Set<String> FORBIDDEN_PREFIXES = Set.of(
        "io/izzel/arclight/boot/",
        "io/izzel/arclight/server/",
        "io/izzel/arclight/forgeinstaller/"
    );

    private static String read(JarFile archive, JarEntry entry)
        throws IOException {
        try (InputStream input = archive.getInputStream(entry)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GradleException(message);
    }

    /**
     * Parses the flat tables and repeatable array-of-tables that make up the metadata this
     * task validates. Keys are emitted as {@code <table path>.<key>}; a repeated array of
     * tables contributes its index to the path so parallel blocks stay distinguishable.
     * A full TOML parser would be dead weight for this shape.
     */
    static Map<String, String> parseModsToml(String content) {
        Map<String, String> values = new LinkedHashMap<>();
        String arrayOfTables = "";
        int arrayIndex = 0;
        String table = "";
        boolean inMultilineString = false;
        for (String rawLine : content.split("\r?\n")) {
            String line = rawLine.trim();
            if (inMultilineString) {
                // A multi-line literal string may close on its own line, so the closing
                // delimiter has to be looked for anywhere in the line.
                if (line.indexOf("'''") >= 0) inMultilineString = false;
                continue;
            }
            if (line.isEmpty() || line.startsWith("#")) continue;
            // The opening delimiter can follow a key on the same line, and the closing one
            // may be on that same line too.
            int literal = line.indexOf("'''");
            if (literal >= 0) {
                inMultilineString = line.indexOf("'''", literal + 3) < 0;
                continue;
            }
            if (line.startsWith("[[") && line.contains("]]")) {
                String name = line.substring(2, line.indexOf("]]")).trim();
                arrayIndex = name.equals(arrayOfTables) ? arrayIndex + 1 : 0;
                arrayOfTables = name;
                table = "";
                continue;
            }
            if (line.startsWith("[") && line.contains("]")) {
                table = line.substring(1, line.indexOf(']')).trim();
                arrayOfTables = "";
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 0) continue;
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (
                value.length() >= 2 &&
                    value.startsWith("\"") &&
                    value.endsWith("\"")
            ) {
                value = value.substring(1, value.length() - 1);
            }
            StringBuilder prefix = new StringBuilder();
            if (!arrayOfTables.isEmpty()) {
                prefix
                    .append(arrayOfTables)
                    .append('.')
                    .append(arrayIndex)
                    .append('.');
            }
            if (!table.isEmpty()) prefix.append(table).append('.');
            values.put(prefix + key, value);
        }
        return values;
    }

    /**
     * Collects the entries of the repeated {@code [[dependencies.<modId>]]} blocks, keyed
     * by the {@code modId} each block declares.
     */
    static Map<String, Map<String, String>> parseDependencies(
        Map<String, String> toml,
        String modId
    ) {
        Map<String, Map<String, String>> byModId = new HashMap<>();
        String prefix = "dependencies." + modId + ".";
        Map<String, String> declaredById = new HashMap<>();
        for (var entry : toml.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) continue;
            String rest = entry.getKey().substring(prefix.length());
            if (!rest.endsWith(".modId")) continue;
            String group = rest.substring(0, rest.length() - ".modId".length());
            declaredById.put(group, entry.getValue());
            byModId.putIfAbsent(entry.getValue(), new HashMap<>());
        }
        for (var entry : toml.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) continue;
            String rest = entry.getKey().substring(prefix.length());
            int lastDot = rest.lastIndexOf('.');
            String declared = declaredById.get(rest.substring(0, lastDot));
            if (declared == null) continue;
            byModId
                .get(declared)
                .put(rest.substring(lastDot + 1), entry.getValue());
        }
        return byModId;
    }

    private static List<String> mixinClassNames(JsonObject config) {
        List<String> names = new ArrayList<>();
        for (String key : List.of("mixins", "server", "client")) {
            JsonElement element = config.get(key);
            if (element == null || !element.isJsonArray()) continue;
            JsonArray array = element.getAsJsonArray();
            for (JsonElement entry : array) {
                if (entry.isJsonPrimitive()) {
                    names.add(entry.getAsString());
                }
            }
        }
        return names;
    }

    @InputFile
    public abstract RegularFileProperty getModJar();

    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getForgeVersion();

    @Input
    public abstract Property<String> getExpectedVersionPrefix();

    @Input
    public abstract ListProperty<String> getRequiredEntries();

    @Input
    public abstract ListProperty<String> getMixinConfigurations();

    @Input
    public abstract MapProperty<String, String> getExpectedManifestAttributes();

    @TaskAction
    public void verify() throws Exception {
        Set<String> classes = new LinkedHashSet<>();
        try (JarFile archive = new JarFile(getModJar().get().getAsFile())) {
            verifyLayout(archive, classes);
            verifyMetadata(archive);
            verifyMixinConfigurations(archive);
        }
    }

    private void verifyLayout(JarFile archive, Set<String> classes) {
        for (String entry : getRequiredEntries().get()) {
            require(
                archive.getEntry(entry) != null,
                "Missing mod JAR entry: " + entry
            );
        }
        for (String entry : FORBIDDEN_ENTRIES) {
            require(
                archive.getEntry(entry) == null,
                "Legacy launcher entry present in the mod JAR: " + entry
            );
        }
        var entries = archive.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            for (String prefix : FORBIDDEN_PREFIXES) {
                require(
                    !name.startsWith(prefix),
                    "Legacy launcher class present in the mod JAR: " + name
                );
            }
            if (
                name.endsWith(".class") && !name.endsWith("module-info.class")
            ) {
                require(
                    classes.add(name),
                    "Duplicate class in the mod JAR: " + name
                );
            }
        }
    }

    private void verifyMetadata(JarFile archive) throws IOException {
        require(archive.getManifest() != null, "Mod JAR has no manifest");
        var attributes = archive.getManifest().getMainAttributes();
        require(
            attributes.getValue("Main-Class") == null,
            "Mod JAR must not declare a Main-Class launcher entry"
        );
        getExpectedManifestAttributes()
            .get()
            .forEach((key, value) ->
                require(
                    value.equals(attributes.getValue(key)),
                    "Manifest attribute " +
                        key +
                        " is " +
                        attributes.getValue(key) +
                        ", expected " +
                        value
                )
            );

        JarEntry modsToml = archive.getJarEntry("META-INF/mods.toml");
        var values = parseModsToml(read(archive, modsToml));
        require(
            "javafml".equals(values.get("modLoader")),
            "mods.toml modLoader is " + values.get("modLoader")
        );
        String loaderVersion = values.get("loaderVersion");
        require(
            loaderVersion != null && loaderVersion.startsWith("["),
            "mods.toml loaderVersion is " + loaderVersion
        );
        String declaredModId = values.get("mods.0.modId");
        require(
            getModId().get().equals(declaredModId),
            "mods.toml modId is " + declaredModId
        );
        String version = values.get("mods.0.version");
        require(
            version != null && version.contains(getExpectedVersionPrefix().get()),
            "mods.toml version is " + version
        );
        var dependencies = parseDependencies(values, getModId().get());
        Map<String, String> minecraft = dependencies.get("minecraft");
        require(
            minecraft != null,
            "mods.toml declares no minecraft dependency"
        );
        String minecraftRange = minecraft.get("versionRange");
        require(
            minecraftRange != null &&
                minecraftRange.contains(getMinecraftVersion().get()),
            "mods.toml Minecraft range is " + minecraftRange
        );
        Map<String, String> forge = dependencies.get("forge");
        require(forge != null, "mods.toml declares no forge dependency");
        String forgeRange = forge.get("versionRange");
        require(
            forgeRange != null &&
                !forgeRange.isBlank() &&
                forgeRange.contains("47"),
            "mods.toml Forge range is " + forgeRange
        );
        String side = minecraft.get("side");
        require(
            "BOTH".equals(side) || "SERVER".equals(side),
            "mods.toml minecraft dependency side is " + side
        );
    }

    private void verifyMixinConfigurations(JarFile archive) throws IOException {
        require(
            archive.getJarEntry("mixins.arclight.refmap.json") != null,
            "Missing mixin refmap"
        );
        for (String name : getMixinConfigurations().get()) {
            JarEntry entry = archive.getJarEntry(name);
            require(entry != null, "Missing mixin configuration: " + name);
            JsonObject config;
            try (
                InputStream input = archive.getInputStream(entry);
                InputStreamReader reader = new InputStreamReader(
                    input,
                    StandardCharsets.UTF_8
                )
            ) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (RuntimeException e) {
                throw new GradleException(
                    "Mixin configuration " + name + " is not valid JSON",
                    e
                );
            }
            String packageName = config.get("package").getAsString();
            List<String> mixins = mixinClassNames(config);
            require(
                !mixins.isEmpty(),
                "Mixin configuration " + name + " declares no mixins"
            );
            for (String mixin : mixins) {
                String path = packageName.replace('.', '/') +
                    '/' +
                    mixin.replace('.', '/') +
                    ".class";
                require(
                    archive.getEntry(path) != null,
                    "Mixin class " +
                        packageName +
                        "." +
                        mixin +
                        " declared by " +
                        name +
                        " is missing from the mod JAR"
                );
            }
            if (config.has("plugin")) {
                String pluginPath = config
                    .get("plugin")
                    .getAsString()
                    .replace('.', '/') +
                    ".class";
                require(
                    archive.getEntry(pluginPath) != null,
                    "Mixin plugin " +
                        config.get("plugin").getAsString() +
                        " declared by " +
                        name +
                        " is missing from the mod JAR"
                );
            }
        }
    }
}
