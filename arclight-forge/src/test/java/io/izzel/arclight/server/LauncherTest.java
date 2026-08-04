package io.izzel.arclight.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LauncherTest {

    @TempDir
    Path directory;

    @Test
    void requiresAnExistingEulaFile() throws Exception {
        assertFalse(Launcher.checkEula(directory.resolve("eula.txt")));
    }

    @Test
    void acceptsOnlyTheExactEulaSetting() throws Exception {
        Path eula = directory.resolve("eula.txt");
        Files.writeString(eula, "# Minecraft EULA\neula=true\n");
        assertTrue(Launcher.checkEula(eula));

        Files.writeString(eula, "eula=TRUE\n");
        assertFalse(Launcher.checkEula(eula));

        Files.writeString(eula, " eula=true\n");
        assertFalse(Launcher.checkEula(eula));

        Files.writeString(eula, "eula=false\n");
        assertFalse(Launcher.checkEula(eula));
    }
}
