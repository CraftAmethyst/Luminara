package io.izzel.arclight.gradle.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
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
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class GenerateMixinInventoryTask extends DefaultTask {

    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String OVERWRITE =
        "Lorg/spongepowered/asm/mixin/Overwrite;";
    private static final String LOAD_IF_MOD =
        "Lio/izzel/arclight/common/mod/mixins/annotation/LoadIfMod;";
    private static final Set<String> INJECTORS = Set.of(
        "Lorg/spongepowered/asm/mixin/injection/Inject;",
        "Lorg/spongepowered/asm/mixin/injection/Redirect;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
        "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
        "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;",
        "Lcom/llamalad7/mixinextras/injector/ModifyReceiver;",
        "Lcom/llamalad7/mixinextras/injector/WrapWithCondition;",
        "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;"
    );

    private final ConfigurableFileCollection configFiles = getProject().files();
    private final ConfigurableFileCollection mixinClasses =
        getProject().files();
    private final ConfigurableFileCollection targetClasspath =
        getProject().files();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getConfigFiles() {
        return configFiles;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getMixinClasses() {
        return mixinClasses;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getTargetClasspath() {
        return targetClasspath;
    }

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        Gson parser = new Gson();
        List<Map<String, Object>> rows = new ArrayList<>();
        ClassLookup mixins = new ClassLookup(mixinClasses.getFiles());
        ClassLookup targets = new ClassLookup(targetClasspath.getFiles());

        List<File> configs = new ArrayList<>(configFiles.getFiles());
        configs.sort((left, right) ->
            left.getName().compareTo(right.getName())
        );
        for (File configFile : configs) {
            JsonObject config;
            try (
                Reader reader = Files.newBufferedReader(
                    configFile.toPath(),
                    StandardCharsets.UTF_8
                )
            ) {
                config = parser.fromJson(reader, JsonObject.class);
            }
            String mixinPackage = config.get("package").getAsString();
            int defaultRequire = 1;
            if (
                config.has("injectors") &&
                config.getAsJsonObject("injectors").has("defaultRequire")
            ) {
                defaultRequire = config
                    .getAsJsonObject("injectors")
                    .get("defaultRequire")
                    .getAsInt();
            }
            for (JsonElement element : config.getAsJsonArray("mixins")) {
                String mixinClass = mixinPackage + "." + element.getAsString();
                ClassNode mixinNode = mixins.read(mixinClass.replace('.', '/'));
                if (mixinNode == null) {
                    rows.add(
                        row(
                            configFile.getName(),
                            mixinClass,
                            "",
                            "",
                            "",
                            "",
                            defaultRequire,
                            null,
                            "MISSING_MIXIN"
                        )
                    );
                    continue;
                }
                List<String> targetClasses = mixinTargets(mixinNode);
                String loadIfMod = loadIfMod(mixinNode);
                for (MethodNode handler : mixinNode.methods) {
                    List<AnnotationNode> annotations = annotations(
                        handler.visibleAnnotations,
                        handler.invisibleAnnotations
                    );
                    for (AnnotationNode annotation : annotations) {
                        if (OVERWRITE.equals(annotation.desc)) {
                            addRows(
                                rows,
                                targets,
                                configFile.getName(),
                                mixinClass,
                                targetClasses,
                                handler,
                                List.of(handler.name + handler.desc),
                                defaultRequire,
                                loadIfMod
                            );
                        } else if (INJECTORS.contains(annotation.desc)) {
                            Map<String, Object> values = annotationValues(
                                annotation
                            );
                            List<String> selectors = strings(
                                values.get("method")
                            );
                            int require =
                                values.get("require") instanceof Number number
                                    ? number.intValue()
                                    : defaultRequire;
                            if (selectors.isEmpty()) selectors = List.of("");
                            addRows(
                                rows,
                                targets,
                                configFile.getName(),
                                mixinClass,
                                targetClasses,
                                handler,
                                selectors,
                                require,
                                loadIfMod
                            );
                        }
                    }
                }
            }
        }

        rows.sort((left, right) -> rowKey(left).compareTo(rowKey(right)));
        File output = getOutputFile().get().getAsFile();
        Files.createDirectories(output.toPath().getParent());
        Gson writer = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
        Files.writeString(
            output.toPath(),
            writer.toJson(rows) + System.lineSeparator(),
            StandardCharsets.UTF_8
        );

        List<Map<String, Object>> failures = rows
            .stream()
            .filter(row -> ((Number) row.get("require")).intValue() > 0)
            .filter(row -> row.get("loadIfMod") == null)
            .filter(row -> !"ACTIVE".equals(row.get("status")))
            .toList();
        if (!failures.isEmpty()) {
            TreeSet<String> details = new TreeSet<>();
            for (Map<String, Object> failure : failures) {
                details.add(
                    failure.get("mixinClass") +
                        "#" +
                        failure.get("handler") +
                        " -> " +
                        failure.get("targetClass") +
                        "#" +
                        failure.get("targetMethod") +
                        failure.get("targetDescriptor") +
                        " [" +
                        failure.get("status") +
                        "]"
                );
            }
            throw new GradleException(
                "Required mixin targets are missing:\n" +
                    String.join("\n", details)
            );
        }
    }

    private static void addRows(
        List<Map<String, Object>> rows,
        ClassLookup targets,
        String config,
        String mixinClass,
        List<String> targetClasses,
        MethodNode handler,
        List<String> selectors,
        int require,
        String loadIfMod
    ) throws IOException {
        if (targetClasses.isEmpty()) {
            rows.add(
                row(
                    config,
                    mixinClass,
                    "",
                    handler.name + handler.desc,
                    "",
                    "",
                    require,
                    loadIfMod,
                    loadIfMod == null ? "MISSING_TARGET" : "CONDITIONAL"
                )
            );
            return;
        }
        for (String targetClass : targetClasses) {
            List<ClassNode> targetNodes = targets.readAll(
                targetClass.replace('.', '/')
            );
            for (String selector : selectors) {
                TargetMethod targetMethod = TargetMethod.parse(
                    selector,
                    handler
                );
                String status;
                if (loadIfMod != null) {
                    status = "CONDITIONAL";
                } else if (targetNodes.isEmpty()) {
                    status = "MISSING_CLASS";
                } else if (
                    targetNodes.stream().anyMatch(targetMethod::matches)
                ) {
                    status = "ACTIVE";
                } else {
                    status = "MISSING_METHOD";
                }
                rows.add(
                    row(
                        config,
                        mixinClass,
                        targetClass,
                        handler.name + handler.desc,
                        targetMethod.name(),
                        targetMethod.descriptor(),
                        require,
                        loadIfMod,
                        status
                    )
                );
            }
        }
    }

    private static Map<String, Object> row(
        String config,
        String mixinClass,
        String targetClass,
        String handler,
        String targetMethod,
        String targetDescriptor,
        int require,
        String loadIfMod,
        String status
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("config", config);
        row.put("mixinClass", mixinClass);
        row.put("targetClass", targetClass);
        row.put("handler", handler);
        row.put("targetMethod", targetMethod);
        row.put("targetDescriptor", targetDescriptor);
        row.put("require", require);
        row.put("loadIfMod", loadIfMod);
        row.put("status", status);
        return row;
    }

    private static String rowKey(Map<String, Object> row) {
        return (
            row.get("config") +
            "\u0000" +
            row.get("mixinClass") +
            "\u0000" +
            row.get("handler") +
            "\u0000" +
            row.get("targetClass") +
            "\u0000" +
            row.get("targetMethod") +
            "\u0000" +
            row.get("targetDescriptor")
        );
    }

    private static List<String> mixinTargets(ClassNode node) {
        for (AnnotationNode annotation : annotations(
            node.visibleAnnotations,
            node.invisibleAnnotations
        )) {
            if (!MIXIN.equals(annotation.desc)) continue;
            Map<String, Object> values = annotationValues(annotation);
            List<String> targets = new ArrayList<>();
            Object value = values.get("value");
            if (value instanceof Collection<?> collection) {
                for (Object target : collection) {
                    if (target instanceof Type type) targets.add(
                        type.getClassName()
                    );
                }
            } else if (value instanceof Type type) {
                targets.add(type.getClassName());
            }
            targets.addAll(strings(values.get("targets")));
            return targets;
        }
        return List.of();
    }

    private static String loadIfMod(ClassNode node) {
        for (AnnotationNode annotation : annotations(
            node.visibleAnnotations,
            node.invisibleAnnotations
        )) {
            if (!LOAD_IF_MOD.equals(annotation.desc)) continue;
            Map<String, Object> values = annotationValues(annotation);
            String condition = "UNKNOWN";
            Object rawCondition = values.get("condition");
            if (
                rawCondition instanceof String[] enumValue &&
                enumValue.length == 2
            ) condition = enumValue[1];
            return (
                condition + ":" + String.join(",", strings(values.get("modid")))
            );
        }
        return null;
    }

    private static Map<String, Object> annotationValues(
        AnnotationNode annotation
    ) {
        if (annotation.values == null) return Map.of();
        Map<String, Object> values = new HashMap<>();
        for (int index = 0; index < annotation.values.size(); index += 2) {
            values.put(
                (String) annotation.values.get(index),
                annotation.values.get(index + 1)
            );
        }
        return values;
    }

    private static List<String> strings(Object value) {
        if (value instanceof String string) return List.of(string);
        if (!(value instanceof Collection<?> collection)) return List.of();
        List<String> strings = new ArrayList<>();
        for (Object element : collection)
            if (element instanceof String string) strings.add(string);
        return strings;
    }

    private static <T> List<T> annotations(List<T> visible, List<T> invisible) {
        if (visible == null && invisible == null) return List.of();
        List<T> annotations = new ArrayList<>();
        if (visible != null) annotations.addAll(visible);
        if (invisible != null) annotations.addAll(invisible);
        return annotations;
    }

    private record TargetMethod(String name, String descriptor) {
        private static TargetMethod parse(String selector, MethodNode handler) {
            if (selector == null || selector.isBlank()) return new TargetMethod(
                handler.name,
                handler.desc
            );
            String value = selector.trim();
            if (
                value.equals("*") ||
                (value.startsWith("desc=/") && value.endsWith("/"))
            ) {
                return new TargetMethod(value, "");
            }
            int ownerEnd = value.startsWith("L") ? value.indexOf(';') : -1;
            if (ownerEnd >= 0) value = value.substring(ownerEnd + 1);
            int descriptorStart = value.indexOf('(');
            String name =
                descriptorStart >= 0
                    ? value.substring(0, descriptorStart)
                    : value;
            String descriptor =
                descriptorStart >= 0 ? value.substring(descriptorStart) : "";
            return new TargetMethod(name, descriptor);
        }

        private boolean matches(ClassNode target) {
            if (name.equals("*")) return !target.methods.isEmpty();
            if (name.startsWith("desc=/") && name.endsWith("/")) {
                String expression = name.substring(
                    "desc=/".length(),
                    name.length() - 1
                );
                return target.methods
                    .stream()
                    .anyMatch(method -> method.desc.matches(".*" + expression));
            }
            boolean prefix = name.endsWith("*");
            String expectedName = prefix
                ? name.substring(0, name.length() - 1)
                : name;
            for (MethodNode method : target.methods) {
                if (
                    prefix
                        ? !method.name.startsWith(expectedName)
                        : !expectedName.equals(method.name)
                ) continue;
                if (
                    descriptor.isEmpty() || descriptor.equals(method.desc)
                ) return true;
            }
            return false;
        }
    }

    private static final class ClassLookup {

        private final List<File> roots;
        private final Map<String, List<ClassNode>> cache = new HashMap<>();

        private ClassLookup(Collection<File> roots) {
            this.roots = new ArrayList<>(roots);
            this.roots.sort((left, right) ->
                left.getAbsolutePath().compareTo(right.getAbsolutePath())
            );
        }

        private ClassNode read(String internalName) throws IOException {
            List<ClassNode> nodes = readAll(internalName);
            return nodes.isEmpty() ? null : nodes.get(0);
        }

        private List<ClassNode> readAll(String internalName)
            throws IOException {
            if (cache.containsKey(internalName)) return cache.get(internalName);
            String entryName = internalName + ".class";
            List<ClassNode> nodes = new ArrayList<>();
            for (File root : roots) {
                byte[] bytes = null;
                if (root.isDirectory()) {
                    File candidate = new File(root, entryName);
                    if (candidate.isFile()) bytes = Files.readAllBytes(
                        candidate.toPath()
                    );
                } else if (root.isFile() && root.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(root)) {
                        var entry = jar.getJarEntry(entryName);
                        if (entry != null) {
                            try (var input = jar.getInputStream(entry)) {
                                bytes = input.readAllBytes();
                            }
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
                    nodes.add(node);
                }
            }
            List<ClassNode> result = List.copyOf(nodes);
            cache.put(internalName, result);
            return result;
        }
    }
}
