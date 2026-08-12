package io.izzel.arclight.installer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoforgeInstallerTest {

    @TempDir
    Path directory;

    @Test
    void removesGeneratedInstallerArtifacts() throws Exception {
        var installer = Files.createFile(directory.resolve("neoforge-21.1.248-installer.jar"));
        var runBat = Files.createFile(directory.resolve("run.bat"));
        var runSh = Files.createFile(directory.resolve("run.sh"));
        var arclightDirectory = Files.createDirectory(directory.resolve(".arclight"));
        var installerLog = Files.createFile(arclightDirectory.resolve("installer_stripped.jar.log"));
        var userJvmArgs = Files.createFile(directory.resolve("user_jvm_args.txt"));
        var unrelated = Files.createFile(directory.resolve("server.properties"));
        var messages = new ArrayList<String>();

        NeoforgeInstaller.cleanupInstallerArtifacts(directory, "21.1.248", messages::add);

        assertFalse(Files.exists(installer));
        assertFalse(Files.exists(runBat));
        assertFalse(Files.exists(runSh));
        assertFalse(Files.exists(installerLog));
        assertFalse(Files.exists(userJvmArgs));
        assertTrue(Files.exists(unrelated));
        assertTrue(messages.isEmpty());
    }
}
