package io.izzel.arclight.gradle.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class GenerateMappingInventoryTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getForgeAccessTransformer();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getForgeAccessLog();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getBukkitAccessTransformer();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getExtraMappings();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getSrgMappings();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getBukkitJar();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getBukkitPatchJar();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getBukkitMappings();

    private final ConfigurableFileCollection minecraftClasspath =
        getProject().files();
    private final ConfigurableFileCollection referenceSources =
        getProject().files();
    private final ConfigurableFileCollection generatedRemapperInputs =
        getProject().files();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getMinecraftClasspath() {
        return minecraftClasspath;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getReferenceSources() {
        return referenceSources;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getGeneratedRemapperInputs() {
        return generatedRemapperInputs;
    }

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        SrgIndex mappings = SrgIndex.read(getSrgMappings().get().getAsFile());
        SrgIndex bukkitMappings = SrgIndex.read(
            getBukkitMappings().get().getAsFile()
        );
        ClassLookup minecraft = new ClassLookup(minecraftClasspath.getFiles());
        List<File> bukkitRoots = new ArrayList<>(minecraftClasspath.getFiles());
        bukkitRoots.add(getBukkitJar().get().getAsFile());
        bukkitRoots.add(getBukkitPatchJar().get().getAsFile());
        ClassLookup bukkit = new ClassLookup(bukkitRoots);
        List<Map<String, Object>> rows = new ArrayList<>();

        inventoryForgeAccessTransformer(rows);
        inventoryBukkitAccessTransformer(rows, bukkit, bukkitMappings);
        inventoryExtraMappings(rows, mappings, minecraft, bukkit);
        for (File generated : generatedRemapperInputs.getFiles()) {
            String status =
                generated.isFile() && generated.length() > 0
                    ? "ACTIVE"
                    : "MISSING_GENERATED_INPUT";
            rows.add(
                row(
                    "generated-remapper",
                    0,
                    "generated",
                    generated.getName(),
                    "",
                    "",
                    status
                )
            );
        }

        rows.sort((left, right) -> rowKey(left).compareTo(rowKey(right)));
        File output = getOutputFile().get().getAsFile();
        Files.createDirectories(output.toPath().getParent());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(
            output.toPath(),
            gson.toJson(rows) + System.lineSeparator(),
            StandardCharsets.UTF_8
        );

        List<String> failures = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if ("ACTIVE".equals(row.get("status"))) continue;
            String symbol = row.get("class") + " " + row.get("member");
            boolean referenced = isReferenced(
                (String) row.get("class"),
                (String) row.get("member"),
                (String) row.get("descriptor")
            );
            row.put(
                "status",
                referenced ? "MISSING_REFERENCED" : "STALE_UNREFERENCED"
            );
            failures.add(
                row.get("source") +
                    ":" +
                    row.get("line") +
                    " " +
                    symbol +
                    " [" +
                    row.get("status") +
                    "]"
            );
        }
        if (!failures.isEmpty()) {
            Files.writeString(
                output.toPath(),
                gson.toJson(rows) + System.lineSeparator(),
                StandardCharsets.UTF_8
            );
            throw new GradleException(
                "Invalid access or mapping entries:\n" +
                    String.join("\n", failures)
            );
        }
    }

    private void inventoryForgeAccessTransformer(List<Map<String, Object>> rows)
        throws IOException {
        File input = getForgeAccessTransformer().get().getAsFile();
        String transformLog = Files.readString(
            getForgeAccessLog().get().getAsFile().toPath(),
            StandardCharsets.UTF_8
        );
        List<String> lines = Files.readAllLines(
            input.toPath(),
            StandardCharsets.UTF_8
        );
        for (int index = 0; index < lines.size(); index++) {
            String content = stripComment(lines.get(index));
            if (content.isBlank()) continue;
            String[] parts = content.split("\\s+");
            if (parts.length < 2) {
                rows.add(
                    row(
                        input.getName(),
                        index + 1,
                        "access",
                        "",
                        content,
                        "",
                        "INVALID_SYNTAX"
                    )
                );
                continue;
            }
            String className = parts[1].replace('.', '/');
            String member = parts.length >= 3 ? parts[2] : "";
            String descriptor = descriptor(member);
            String memberName = memberName(member);
            boolean active;
            if (memberName.isEmpty()) {
                active =
                    transformLog.contains(
                        "Transforming " + className + " CLASS"
                    ) ||
                    transformLog.contains(
                        "Transforming class L" + className + ";"
                    );
            } else if (descriptor.isEmpty()) {
                active = transformLog.contains(
                    "Transforming " + className + " FIELD " + memberName + " "
                );
            } else {
                active = transformLog.contains(
                    "Transforming " + className + " METHOD " + member
                );
            }
            rows.add(
                row(
                    input.getName(),
                    index + 1,
                    descriptor.isEmpty()
                        ? memberName.isEmpty()
                            ? "class"
                            : "field"
                        : "method",
                    className.replace('/', '.'),
                    memberName,
                    descriptor,
                    active ? "ACTIVE" : "MISSING"
                )
            );
        }
    }

    private void inventoryBukkitAccessTransformer(
        List<Map<String, Object>> rows,
        ClassLookup bukkit,
        SrgIndex mappings
    ) throws IOException {
        File input = getBukkitAccessTransformer().get().getAsFile();
        List<String> lines = Files.readAllLines(
            input.toPath(),
            StandardCharsets.UTF_8
        );
        for (int index = 0; index < lines.size(); index++) {
            String content = stripComment(lines.get(index));
            if (content.isBlank()) continue;
            String[] parts = content.split("\\s+");
            if (parts.length < 2) {
                rows.add(
                    row(
                        input.getName(),
                        index + 1,
                        "access",
                        "",
                        content,
                        "",
                        "INVALID_SYNTAX"
                    )
                );
                continue;
            }
            String target = parts[1].replace("/v/", "/v1_20_R1/");
            int memberSeparator = target.indexOf("/<");
            String className =
                memberSeparator < 0
                    ? target
                    : target.substring(0, memberSeparator);
            String member =
                memberSeparator < 0
                    ? ""
                    : target.substring(memberSeparator + 1);
            String descriptor = toBukkitDescriptor(
                descriptor(member).replace("/v/", "/v1_20_R1/"),
                mappings
            );
            String memberName = memberName(member);
            ClassNode targetClass = bukkit.read(className);
            boolean active =
                targetClass != null &&
                (memberName.isEmpty() ||
                    hasMethod(targetClass, memberName, descriptor));
            rows.add(
                row(
                    input.getName(),
                    index + 1,
                    memberName.isEmpty() ? "class" : "method",
                    className.replace('/', '.'),
                    memberName,
                    descriptor,
                    active ? "ACTIVE" : "MISSING"
                )
            );
        }
    }

    private static String toBukkitDescriptor(
        String descriptor,
        SrgIndex mappings
    ) {
        StringBuilder mapped = new StringBuilder();
        for (int index = 0; index < descriptor.length(); index++) {
            char current = descriptor.charAt(index);
            mapped.append(current);
            if (current != 'L') continue;
            int end = descriptor.indexOf(';', index);
            if (end < 0) return descriptor;
            String className = descriptor.substring(index + 1, end);
            SrgClass mapping = mappings.findClass(className);
            mapped
                .append(mapping == null ? className : mapping.namedClass)
                .append(';');
            index = end;
        }
        return mapped.toString();
    }

    private void inventoryExtraMappings(
        List<Map<String, Object>> rows,
        SrgIndex mappings,
        ClassLookup minecraft,
        ClassLookup bukkit
    ) throws IOException {
        File input = getExtraMappings().get().getAsFile();
        List<String> lines = Files.readAllLines(
            input.toPath(),
            StandardCharsets.UTF_8
        );
        String leftClass = null;
        String rightClass = null;
        ClassNode leftNode = null;
        ClassNode rightNode = null;
        for (int index = 0; index < lines.size(); index++) {
            String raw = lines.get(index);
            String content = stripComment(raw);
            if (content.isBlank()) continue;
            String[] parts = content.trim().split("\\s+");
            if (!Character.isWhitespace(raw.charAt(0))) {
                if (parts.length != 2) {
                    rows.add(
                        row(
                            input.getName(),
                            index + 1,
                            "mapping",
                            "",
                            content,
                            "",
                            "INVALID_SYNTAX"
                        )
                    );
                    continue;
                }
                leftClass = parts[0];
                rightClass = parts[1];
                leftNode = firstNonNull(
                    bukkit.read(leftClass),
                    minecraft.read(leftClass)
                );
                SrgClass rightMapping = mappings.findClass(rightClass);
                String namedRight =
                    rightMapping == null ? rightClass : rightMapping.namedClass;
                rightNode = minecraft.read(namedRight);
                boolean hasFieldMappings =
                    index + 1 < lines.size() &&
                    !lines.get(index + 1).isBlank() &&
                    Character.isWhitespace(lines.get(index + 1).charAt(0));
                boolean active =
                    rightNode != null &&
                    (leftNode != null || !hasFieldMappings);
                rows.add(
                    row(
                        input.getName(),
                        index + 1,
                        "class-mapping",
                        leftClass.replace('/', '.'),
                        rightClass.replace('/', '.'),
                        "",
                        active ? "ACTIVE" : "MISSING"
                    )
                );
            } else {
                if (parts.length != 2 || leftClass == null) {
                    rows.add(
                        row(
                            input.getName(),
                            index + 1,
                            "mapping",
                            "",
                            content,
                            "",
                            "INVALID_SYNTAX"
                        )
                    );
                    continue;
                }
                boolean active =
                    leftNode != null &&
                    hasField(leftNode, parts[0]) &&
                    rightNode != null;
                rows.add(
                    row(
                        input.getName(),
                        index + 1,
                        "field-mapping",
                        leftClass.replace('/', '.'),
                        parts[0] + " -> " + parts[1],
                        "",
                        active ? "ACTIVE" : "MISSING"
                    )
                );
            }
        }
    }

    private boolean isReferenced(
        String className,
        String member,
        String descriptor
    ) throws IOException {
        String term;
        if ("<init>".equals(member)) {
            term = descriptor;
        } else {
            term =
                member == null || member.isBlank()
                    ? className
                    : member.split("\\s+| -> ")[0];
            if (term.contains("(")) term = term.substring(0, term.indexOf('('));
        }
        for (File source : referenceSources.getFiles()) {
            if (!source.isFile()) continue;
            String text = Files.readString(
                source.toPath(),
                StandardCharsets.UTF_8
            );
            if (
                !term.isBlank() &&
                (text.contains(term) || text.contains(term.replace('.', '/')))
            ) return true;
        }
        return false;
    }

    private static Map<String, Object> row(
        String source,
        int line,
        String kind,
        String className,
        String member,
        String descriptor,
        String status
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source", source);
        row.put("line", line);
        row.put("kind", kind);
        row.put("class", className);
        row.put("member", member);
        row.put("descriptor", descriptor);
        row.put("status", status);
        return row;
    }

    private static String rowKey(Map<String, Object> row) {
        return (
            row.get("source") +
            "\u0000" +
            String.format("%08d", row.get("line"))
        );
    }

    private static String stripComment(String line) {
        int comment = line.indexOf('#');
        return (comment < 0 ? line : line.substring(0, comment)).trim();
    }

    private static String memberName(String member) {
        int descriptor = member.indexOf('(');
        return descriptor < 0 ? member : member.substring(0, descriptor);
    }

    private static String descriptor(String member) {
        int descriptor = member.indexOf('(');
        return descriptor < 0 ? "" : member.substring(descriptor);
    }

    private static boolean hasField(ClassNode node, String name) {
        return (
            name != null &&
            node.fields.stream().anyMatch(field -> field.name.equals(name))
        );
    }

    private static boolean hasMethod(
        ClassNode node,
        String name,
        String descriptor
    ) {
        if (name == null) return false;
        for (MethodNode method : node.methods) {
            if (
                method.name.equals(name) &&
                (descriptor.isEmpty() || method.desc.equals(descriptor))
            ) return true;
        }
        return false;
    }

    private static <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private static final class SrgIndex {

        private final Map<String, SrgClass> classes = new HashMap<>();

        private static SrgIndex read(File file) throws IOException {
            SrgIndex index = new SrgIndex();
            SrgClass current = null;
            for (String line : Files.readAllLines(
                file.toPath(),
                StandardCharsets.UTF_8
            )) {
                if (
                    line.isBlank() ||
                    line.startsWith("tsrg2") ||
                    line.startsWith("\t\t")
                ) continue;
                String[] parts = line.trim().split("\\s+");
                if (!Character.isWhitespace(line.charAt(0))) {
                    if (parts.length >= 2) {
                        current = new SrgClass(parts[0], parts[1]);
                        index.classes.put(parts[0], current);
                        index.classes.put(parts[1], current);
                    }
                } else if (current != null) {
                    if (parts.length == 2) {
                        current.fields.put(parts[1], parts[0]);
                    } else if (parts.length >= 3) {
                        current.methods.add(
                            new SrgMethod(parts[0], parts[1], parts[2])
                        );
                    }
                }
            }
            return index;
        }

        private SrgClass findClass(String name) {
            return classes.get(name);
        }
    }

    private static final class SrgClass {

        private final String namedClass;
        private final Map<String, String> fields = new HashMap<>();
        private final List<SrgMethod> methods = new ArrayList<>();

        private SrgClass(String namedClass, String ignoredSrgClass) {
            this.namedClass = namedClass;
        }

        private String namedField(String srgName) {
            return fields.getOrDefault(srgName, srgName);
        }

        private String namedMethod(String srgName, String descriptor) {
            return methods
                .stream()
                .filter(
                    method ->
                        method.srgName.equals(srgName) &&
                        method.descriptor.equals(descriptor)
                )
                .map(method -> method.namedName)
                .findFirst()
                .orElseGet(() ->
                    methods
                        .stream()
                        .filter(method -> method.srgName.equals(srgName))
                        .map(method -> method.namedName)
                        .findFirst()
                        .orElse(srgName)
                );
        }
    }

    private record SrgMethod(
        String namedName,
        String descriptor,
        String srgName
    ) {}

    private static final class ClassLookup {

        private final List<File> directories = new ArrayList<>();
        private final Map<String, File> jarsByEntry = new HashMap<>();
        private final Map<String, ClassNode> cache = new HashMap<>();
        private final Set<String> missing = new TreeSet<>();

        private ClassLookup(Collection<File> roots) throws IOException {
            List<File> sortedRoots = new ArrayList<>(roots);
            sortedRoots.sort((left, right) ->
                left.getAbsolutePath().compareTo(right.getAbsolutePath())
            );
            for (File root : sortedRoots) {
                if (root.isDirectory()) {
                    directories.add(root);
                } else if (root.isFile() && root.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(root)) {
                        var entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            var entry = entries.nextElement();
                            if (
                                entry.getName().endsWith(".class")
                            ) jarsByEntry.putIfAbsent(entry.getName(), root);
                        }
                    }
                }
            }
        }

        private ClassNode read(String internalName) throws IOException {
            if (cache.containsKey(internalName)) return cache.get(internalName);
            if (missing.contains(internalName)) return null;
            String entryName = internalName + ".class";
            byte[] bytes = null;
            for (File directory : directories) {
                File candidate = new File(directory, entryName);
                if (candidate.isFile()) {
                    bytes = Files.readAllBytes(candidate.toPath());
                    break;
                }
            }
            File jarFile = jarsByEntry.get(entryName);
            if (bytes == null && jarFile != null) {
                try (JarFile jar = new JarFile(jarFile)) {
                    try (
                        var input = jar.getInputStream(
                            jar.getJarEntry(entryName)
                        )
                    ) {
                        bytes = input.readAllBytes();
                    }
                }
            }
            if (bytes != null) {
                ClassNode node = new ClassNode();
                new ClassReader(bytes).accept(
                    node,
                    ClassReader.SKIP_CODE |
                        ClassReader.SKIP_DEBUG |
                        ClassReader.SKIP_FRAMES
                );
                cache.put(internalName, node);
                return node;
            }
            missing.add(internalName);
            return null;
        }
    }
}
