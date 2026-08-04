package io.izzel.arclight.forgeinstaller;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

record ForgeArguments(
    String mainClass,
    List<String> gameArguments,
    List<Path> modulePath,
    List<Path> legacyClassPath,
    Map<String, String> systemProperties,
    List<String> opens,
    List<String> exports
) {
    ForgeArguments {
        gameArguments = List.copyOf(gameArguments);
        modulePath = List.copyOf(modulePath);
        legacyClassPath = List.copyOf(legacyClassPath);
        systemProperties = Collections.unmodifiableMap(
            new LinkedHashMap<>(systemProperties)
        );
        opens = List.copyOf(opens);
        exports = List.copyOf(exports);
    }
}
