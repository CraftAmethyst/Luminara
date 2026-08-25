package io.izzel.arclight.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class AssembleDistributionTask extends DefaultTask {

    // Gradle normalises archive entries to this timestamp, keep appended entries reproducible too.
    private static final long CONSTANT_ENTRY_TIME =
        new GregorianCalendar(1980, Calendar.FEBRUARY, 1, 0, 0, 0).getTimeInMillis();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getInputJar();

    /**
     * Libraries that ship as jar-in-jar entries. They are appended after the shadow and remap
     * pipeline because those stages merge every archive they are handed.
     */
    @InputDirectory
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getNestedLibraries();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @TaskAction
    public void assemble() throws IOException {
        var input = getInputJar().get().getAsFile().toPath();
        var output = getOutputJar().get().getAsFile().toPath();
        Files.createDirectories(output.getParent());

        var nestedLibraries = getNestedLibraries().getAsFile().getOrNull();
        if (nestedLibraries == null) {
            Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Files.deleteIfExists(output);
        try (var source = new ZipFile(input.toFile()); var target = new ZipOutputStream(Files.newOutputStream(output))) {
            var written = new HashSet<String>();
            var entries = source.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                written.add(entry.getName());
                target.putNextEntry(copyOf(entry));
                if (!entry.isDirectory()) {
                    try (var content = source.getInputStream(entry)) {
                        content.transferTo(target);
                    }
                }
                target.closeEntry();
            }
            append(nestedLibraries.toPath(), target, written);
        }
    }

    private ZipEntry copyOf(ZipEntry entry) {
        var copy = new ZipEntry(entry.getName());
        copy.setTime(entry.getTime());
        copy.setMethod(entry.getMethod());
        if (entry.getMethod() == ZipEntry.STORED) {
            copy.setSize(entry.getSize());
            copy.setCrc(entry.getCrc());
        }
        return copy;
    }

    private void append(Path root, ZipOutputStream target, Set<String> written) throws IOException {
        try (var paths = Files.walk(root)) {
            for (var path : paths.sorted(Comparator.naturalOrder()).toList()) {
                if (path.equals(root)) continue;
                var directory = Files.isDirectory(path);
                var name = root.relativize(path).toString().replace('\\', '/') + (directory ? "/" : "");
                if (!written.add(name)) {
                    if (directory) continue;
                    throw new GradleException(getName() + ": " + name + " is already part of "
                        + getInputJar().get().getAsFile().getName());
                }
                var entry = new ZipEntry(name);
                entry.setTime(CONSTANT_ENTRY_TIME);
                target.putNextEntry(entry);
                if (!directory) {
                    copy(path, target);
                }
                target.closeEntry();
            }
        }
    }

    private void copy(Path path, OutputStream target) throws IOException {
        try (var content = Files.newInputStream(path)) {
            content.transferTo(target);
        }
    }
}
