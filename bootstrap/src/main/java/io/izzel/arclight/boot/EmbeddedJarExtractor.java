package io.izzel.arclight.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class EmbeddedJarExtractor {

    private EmbeddedJarExtractor() {
    }

    public static Path extract(InputStream source, Path directory, String fileName, boolean force) throws IOException {
        Files.createDirectories(directory);
        Path target = directory.resolve(fileName);
        Path temporary = Files.createTempFile(directory, fileName, ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            if (force || !Files.exists(target) || Files.mismatch(temporary, target) != -1) {
                replace(temporary, target);
            }
            try (var files = Files.list(directory)) {
                for (Path old : files.toList()) {
                    if (!old.equals(target) && !old.equals(temporary)) {
                        Files.delete(old);
                    }
                }
            }
            return target;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void replace(Path source, Path target) throws IOException {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
