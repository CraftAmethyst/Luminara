package io.izzel.arclight.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@DisableCachingByDefault(because = "Runs external launcher processes")
public abstract class VerifyLauncherBoundaryTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getDistributionJars();

    @Input
    public abstract Property<String> getJavaExecutable();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    public VerifyLauncherBoundaryTask() {
        getOutputs().upToDateWhen(task -> false);
    }

    @TaskAction
    public void verify() throws IOException, InterruptedException {
        for (var archive : getDistributionJars().getFiles()) {
            var nameParts = archive.getName().split("-");
            if (nameParts.length < 2) throw new GradleException("Cannot determine loader from " + archive.getName());
            var loader = nameParts[1];
            var work = getOutputDirectory().get().getAsFile().toPath().resolve(loader);
            resetDirectory(work);

            var stdout = work.resolve("launcher.stdout.log");
            var stderr = work.resolve("launcher.stderr.log");
            var process = new ProcessBuilder(getJavaExecutable().get(), "-jar", archive.getAbsolutePath(), "nogui")
                .directory(work.toFile())
                .redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile())
                .start();
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroy();
                if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly();
                throw new GradleException(loader + " launcher did not reach the EULA boundary within 60 seconds");
            }
            var output = Files.readString(stdout, StandardCharsets.UTF_8) + "\n"
                + Files.readString(stderr, StandardCharsets.UTF_8);
            if (process.exitValue() == 0) {
                throw new GradleException(loader + " launcher unexpectedly accepted a missing eula.txt");
            }
            if (!output.contains("Minecraft EULA not accepted") || !output.contains("https://aka.ms/MinecraftEULA")) {
                throw new GradleException(loader + " launcher did not report the required EULA boundary; see " + work);
            }
        }
    }

    private static void resetDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                for (var path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
        Files.createDirectories(directory);
    }
}
