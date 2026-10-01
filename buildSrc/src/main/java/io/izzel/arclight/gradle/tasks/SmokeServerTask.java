package io.izzel.arclight.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Installs a clean Forge dedicated server with the plain Forge installer, drops the built
 * mod JAR and the fixture plugin into it, starts it with the {@code java @args} command
 * Forge generated, and asserts on the log.
 * <p>
 * The fixture directory is regenerated on every run, so the test never touches the
 * workspace world or configuration and never reuses server state between runs.
 */
public abstract class SmokeServerTask extends DefaultTask {

    private static final String INSTALLER_LOG = "forge-installer.log";
    private static final String SERVER_LOG = "smoke-server.log";

    private static String javaExecutable() {
        return Path.of(
            System.getProperty("java.home"),
            "bin",
            isWindows() ? "java.exe" : "java"
        ).toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase(Locale.ROOT)
            .contains("win");
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var nested = Files.walk(path)) {
            for (Path entry : nested.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(entry);
            }
        }
    }

    /**
     * Tokenizes a Forge argument file. The file is a shell command body rather than a
     * one-argument-per-line list, so quoting and line continuations have to be honoured:
     * the module path and the system properties each arrive as a single argument.
     */
    private static List<String> readArguments(Path argsFile) throws IOException {
        String content = Files.readString(argsFile, StandardCharsets.UTF_8);
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean started = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\\' && i + 1 < content.length()) {
                char next = content.charAt(i + 1);
                if (next == '\n' || next == '\r') {
                    i++;
                    continue;
                }
                if (inDoubleQuotes || (!inSingleQuotes && !inDoubleQuotes)) {
                    if (next == '"' || next == '\'' || next == '\\' || Character.isWhitespace(next)) {
                        current.append(next);
                        started = true;
                        i++;
                        continue;
                    }
                }
            }
            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                started = true;
                continue;
            }
            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                started = true;
                continue;
            }
            if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                if (started) {
                    arguments.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
                continue;
            }
            current.append(c);
            started = true;
        }
        if (started) arguments.add(current.toString());
        return arguments;
    }

    private static void runInstaller(Path installer, Path fixture) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.add("-jar");
        command.add(installer.toAbsolutePath().toString());
        command.add("--installServer");
        command.add(fixture.toAbsolutePath().toString());
        Process process = new ProcessBuilder(command)
            .directory(fixture.toFile())
            .redirectErrorStream(true)
            .redirectOutput(fixture.resolve(INSTALLER_LOG).toFile())
            .start();
        if (!process.waitFor(20, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new GradleException(
                "The Forge installer did not finish within 20 minutes; see " +
                    fixture.resolve(INSTALLER_LOG)
            );
        }
        if (process.exitValue() != 0) {
            throw new GradleException(
                "The Forge installer failed with exit code " +
                    process.exitValue() +
                    "; see " +
                    fixture.resolve(INSTALLER_LOG)
            );
        }
    }

    @InputFile
    public abstract RegularFileProperty getModJar();

    @InputFile
    public abstract RegularFileProperty getPluginJar();

    @InputFile
    public abstract RegularFileProperty getFixtureModJar();

    @InputFile
    public abstract RegularFileProperty getForgeInstallerJar();

    /**
     * Not declared as an output: the fixture holds a running server's world, session locks
     * and logs, which cannot be hashed reliably for up-to-date checks. The task always runs.
     */
    @Internal
    public abstract DirectoryProperty getFixtureDirectory();

    @Input
    public abstract Property<Integer> getStartupTimeoutSeconds();

    @Input
    public abstract ListProperty<String> getExpectedOutput();

    @TaskAction
    public void smoke() throws Exception {
        Path fixture = getFixtureDirectory().get().getAsFile().toPath();
        Path libraries = fixture.resolve("libraries");
        boolean installed = Files.isDirectory(libraries);
        if (installed) {
            // The installer output is expensive and deterministic, so only the server
            // state created by a previous run is discarded.
            for (Path entry : Files.list(fixture).toList()) {
                if (entry.getFileName().toString().equals("libraries")) continue;
                if (entry.getFileName().toString().equals("forge-installer.jar")) continue;
                deleteRecursively(entry);
            }
        } else {
            deleteRecursively(fixture);
            Files.createDirectories(fixture);
        }
        Files.createDirectories(fixture.resolve("mods"));
        Files.createDirectories(fixture.resolve("plugins"));

        Files.copy(
            getForgeInstallerJar().get().getAsFile().toPath(),
            fixture.resolve("forge-installer.jar"),
            StandardCopyOption.REPLACE_EXISTING
        );
        if (!installed) {
            runInstaller(fixture.resolve("forge-installer.jar"), fixture);
        }

        Path argsFile = fixture
            .resolve("libraries")
            .resolve("net/minecraftforge/forge")
            .resolve(getForgeCoordinates().get())
            .resolve(isWindows() ? "win_args.txt" : "unix_args.txt");
        if (!Files.isRegularFile(argsFile)) {
            throw new GradleException(
                "The Forge installer produced no launch arguments at " + argsFile
            );
        }

        Files.copy(
            getModJar().get().getAsFile().toPath(),
            fixture.resolve("mods").resolve(getModJar().get().getAsFile().getName()),
            StandardCopyOption.REPLACE_EXISTING
        );
        Files.copy(
            getFixtureModJar().get().getAsFile().toPath(),
            fixture.resolve("mods/luminara-smoke-mod.jar"),
            StandardCopyOption.REPLACE_EXISTING
        );
        Files.copy(
            getPluginJar().get().getAsFile().toPath(),
            fixture.resolve("plugins/luminara-smoke-plugin.jar"),
            StandardCopyOption.REPLACE_EXISTING
        );
        // Auto-accepting the EULA is required to run an unattended server. It applies only
        // to this throwaway fixture directory, never to the workspace.
        Files.writeString(fixture.resolve("eula.txt"), "eula=true\n", StandardCharsets.UTF_8);
        Files.writeString(
            fixture.resolve("server.properties"),
            "online-mode=false\nserver-port=0\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\n",
            StandardCharsets.UTF_8
        );

        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.add("-Xms512m");
        command.add("-Xmx2G");
        command.addAll(readArguments(argsFile));
        command.add("nogui");
        launch(command, fixture);
    }

    private void launch(List<String> command, Path fixture) throws Exception {
        Path log = fixture.resolve(SERVER_LOG);
        Process process = new ProcessBuilder(command)
            .directory(fixture.toFile())
            .redirectErrorStream(true)
            .start();
        List<String> missing = new ArrayList<>(getExpectedOutput().get());
        Instant deadline = Instant.now().plus(
            Duration.ofSeconds(getStartupTimeoutSeconds().get())
        );
        try (
            BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
            BufferedWriter logWriter = Files.newBufferedWriter(log, StandardCharsets.UTF_8);
            BufferedWriter input = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
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
                    process.waitFor(60, TimeUnit.SECONDS) &&
                    process.exitValue() == 0
            ) return;
        } finally {
            if (process.isAlive()) {
                process.destroy();
                if (!process.waitFor(30, TimeUnit.SECONDS)) process.destroyForcibly();
            }
        }
        throw new GradleException(
            "Native Forge server smoke failed; missing " +
                missing +
                ". Fixture preserved at " +
                fixture
        );
    }

    @Input
    public abstract Property<String> getForgeCoordinates();
}
