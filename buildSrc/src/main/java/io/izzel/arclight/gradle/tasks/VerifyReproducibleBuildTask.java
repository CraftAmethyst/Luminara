package io.izzel.arclight.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.work.DisableCachingByDefault;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.LocalState;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@DisableCachingByDefault(because = "Runs two isolated nested builds and compares their distributions")
public abstract class VerifyReproducibleBuildTask extends DefaultTask {

    @Internal
    public abstract DirectoryProperty getSourceDirectory();

    @LocalState
    public abstract DirectoryProperty getWorkspaceDirectory();

    @Input
    public abstract Property<String> getDistributionRelativePath();

    @TaskAction
    public void verify() throws Exception {
        Path source = getSourceDirectory().get().getAsFile().toPath().toAbsolutePath().normalize();
        Path workspace = getWorkspaceDirectory().get().getAsFile().toPath().toAbsolutePath().normalize();
        String sourceDateEpoch = runGit(source, "show", "-s", "--format=%ct", "HEAD").trim();
        String gitHash = runGit(source, "rev-parse", "--short", "HEAD").trim();
        require(sourceDateEpoch.matches("[0-9]+"), "Git returned an invalid commit timestamp: " + sourceDateEpoch);
        require(gitHash.matches("[0-9a-f]{7,40}"), "Git returned an invalid commit hash: " + gitHash);

        List<String> trackedFiles = trackedFiles(source);
        require(!trackedFiles.isEmpty(), "Git returned no tracked source files");
        recreateDirectory(workspace);

        Path first = workspace.resolve("first");
        Path second = workspace.resolve("second");
        copyTrackedSource(source, first, trackedFiles);
        copyTrackedSource(source, second, trackedFiles);

        runBuild("first", first, sourceDateEpoch, gitHash);
        runBuild("second", second, sourceDateEpoch, gitHash);

        Path firstJar = first.resolve(getDistributionRelativePath().get()).normalize();
        Path secondJar = second.resolve(getDistributionRelativePath().get()).normalize();
        require(firstJar.startsWith(first) && secondJar.startsWith(second), "Distribution path escapes a build workspace");
        require(Files.isRegularFile(firstJar), "First build did not produce " + firstJar);
        require(Files.isRegularFile(secondJar), "Second build did not produce " + secondJar);

        String firstHash = sha256(firstJar);
        String secondHash = sha256(secondJar);
        require(firstHash.equals(secondHash), "Distribution is not reproducible: " + firstHash + " != " + secondHash);
        getLogger().lifecycle("Reproducible distribution SHA-256: {}", firstHash);
    }

    private void runBuild(String label, Path directory, String sourceDateEpoch, String gitHash) throws Exception {
        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("cmd");
            command.add("/d");
            command.add("/c");
            command.add(directory.resolve("gradlew.bat").toString());
        } else {
            command.add(directory.resolve("gradlew").toString());
        }
        command.add("--no-daemon");
        command.add("assembleDistribution");
        command.add("-PluminaraGitHash=" + gitHash);

        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectErrorStream(true);
        builder.environment().put("SOURCE_DATE_EPOCH", sourceDateEpoch);
        String javaHome = System.getProperty("java.home");
        String pathKey = isWindows() ? "Path" : "PATH";
        String currentPath = builder.environment().getOrDefault(pathKey, "");
        builder.environment().put("JAVA_HOME", javaHome);
        builder.environment().put(pathKey, Path.of(javaHome, "bin") + File.pathSeparator + currentPath);
        Process process = builder.start();
        try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = output.readLine()) != null) {
                getLogger().lifecycle("[reproducible:{}] {}", label, line);
            }
        }
        int exitCode = process.waitFor();
        require(exitCode == 0, "Isolated " + label + " build failed with exit code " + exitCode);
    }

    private static List<String> trackedFiles(Path source) throws Exception {
        byte[] output = run(source, List.of("git", "ls-files", "-z"));
        String[] entries = new String(output, StandardCharsets.UTF_8).split("\u0000", -1);
        List<String> result = new ArrayList<>(entries.length);
        for (String entry : entries) {
            if (!entry.isEmpty()) result.add(entry);
        }
        return result;
    }

    private static void copyTrackedSource(Path source, Path destination, List<String> trackedFiles) throws IOException {
        Files.createDirectories(destination);
        for (String relativeName : trackedFiles) {
            Path relative = Path.of(relativeName);
            Path input = source.resolve(relative).normalize();
            Path output = destination.resolve(relative).normalize();
            require(input.startsWith(source) && output.startsWith(destination), "Tracked path escapes repository: " + relativeName);
            require(Files.exists(input, LinkOption.NOFOLLOW_LINKS), "Tracked source file is missing: " + relativeName);
            if (Files.isDirectory(input, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(output);
                continue;
            }
            Files.createDirectories(output.getParent());
            Files.copy(input, output, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private static String runGit(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(arguments.length + 1);
        command.add("git");
        command.addAll(List.of(arguments));
        return new String(run(directory, command), StandardCharsets.UTF_8);
    }

    private static byte[] run(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .start();
        byte[] output = process.getInputStream().readAllBytes();
        int exitCode = process.waitFor();
        require(exitCode == 0, String.join(" ", command) + " failed with exit code " + exitCode + ": "
                + new String(output, StandardCharsets.UTF_8));
        return output;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0) digest.update(buffer, 0, length);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void recreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(VerifyReproducibleBuildTask::delete);
            }
        }
        Files.createDirectories(directory);
    }

    private static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GradleException(message);
    }
}
