package io.izzel.arclight.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class SmokeServerTask extends DefaultTask {

    private static String javaExecutable() {
        return Path.of(
            System.getProperty("java.home"),
            "bin",
            isWindows() ? "java.exe" : "java"
        ).toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase(java.util.Locale.ROOT)
            .contains("win");
    }

    private static void cleanFixture(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.list(directory)) {
                paths
                    .filter(
                        path ->
                            !path
                                .getFileName()
                                .toString()
                                .equals("libraries") &&
                                !path
                                    .getFileName()
                                    .toString()
                                    .equals("forge-installer.jar")
                    )
                    .forEach(SmokeServerTask::deleteRecursively);
            }
        }
        Files.createDirectories(directory);
    }

    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (var nested = Files.walk(path)) {
                    nested
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(SmokeServerTask::deleteSingle);
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static void deleteSingle(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    @InputFile
    public abstract RegularFileProperty getDistributionJar();

    @InputFile
    public abstract RegularFileProperty getPluginJar();

    @InputFile
    public abstract RegularFileProperty getModJar();

    @OutputDirectory
    public abstract DirectoryProperty getFixtureDirectory();

    @Input
    public abstract Property<Integer> getStartupTimeoutSeconds();

    @Input
    public abstract ListProperty<String> getExpectedOutput();

    @TaskAction
    public void smoke() throws Exception {
        Path fixture = getFixtureDirectory().get().getAsFile().toPath();
        cleanFixture(fixture);
        Files.createDirectories(fixture.resolve("plugins"));
        Files.createDirectories(fixture.resolve("mods"));
        Files.copy(
            getPluginJar().get().getAsFile().toPath(),
            fixture.resolve("plugins/luminara-smoke-plugin.jar"),
            StandardCopyOption.REPLACE_EXISTING
        );
        Files.copy(
            getModJar().get().getAsFile().toPath(),
            fixture.resolve("mods/luminara-smoke-mod.jar"),
            StandardCopyOption.REPLACE_EXISTING
        );
        Files.writeString(
            fixture.resolve("eula.txt"),
            "eula=true\n",
            StandardCharsets.UTF_8
        );
        Files.writeString(
            fixture.resolve("server.properties"),
            "online-mode=false\nserver-port=0\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\n",
            StandardCharsets.UTF_8
        );

        Path log = fixture.resolve("smoke-server.log");
        Process process = new ProcessBuilder(
            javaExecutable(),
            "-Xms512m",
            "-Xmx1G",
            "-jar",
            getDistributionJar().get().getAsFile().getAbsolutePath(),
            "nogui"
        )
            .directory(fixture.toFile())
            .redirectErrorStream(true)
            .start();
        List<String> missing = new ArrayList<>(getExpectedOutput().get());
        Instant deadline = Instant.now().plus(
            Duration.ofSeconds(getStartupTimeoutSeconds().get())
        );
        try (
            BufferedReader output = new BufferedReader(
                new InputStreamReader(
                    process.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );
            BufferedWriter logWriter = Files.newBufferedWriter(
                log,
                StandardCharsets.UTF_8
            );
            BufferedWriter input = new BufferedWriter(
                new OutputStreamWriter(
                    process.getOutputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            boolean commandsSent = false;
            String line;
            while (Instant.now().isBefore(deadline)) {
                if (output.ready()) {
                    line = output.readLine();
                    if (line == null) break;
                    logWriter.write(line);
                    logWriter.newLine();
                    logWriter.flush();
                    missing.removeIf(line::contains);
                    if (
                        !commandsSent &&
                            line.contains("Done (") &&
                            line.contains("For help, type \"help\"")
                    ) {
                        input.write("luminara info\n");
                        input.write("luminara-smoke\n");
                        input.flush();
                        commandsSent = true;
                    }
                    if (missing.isEmpty()) {
                        input.write("stop\n");
                        input.flush();
                        break;
                    }
                } else if (!process.isAlive()) {
                    break;
                } else {
                    Thread.sleep(100);
                }
            }
            if (
                missing.isEmpty() &&
                    process.waitFor(30, TimeUnit.SECONDS) &&
                    process.exitValue() == 0
            ) return;
        } finally {
            if (process.isAlive()) {
                process.destroy();
                if (
                    !process.waitFor(10, TimeUnit.SECONDS)
                ) process.destroyForcibly();
            }
        }
        throw new GradleException(
            "Server smoke failed; missing " +
                missing +
                ". Fixture preserved at " +
                fixture
        );
    }
}
