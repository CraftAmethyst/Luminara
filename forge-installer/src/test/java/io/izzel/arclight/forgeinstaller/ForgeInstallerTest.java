package io.izzel.arclight.forgeinstaller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForgeInstallerTest {

    @TempDir
    Path directory;

    @Test
    void readsCompleteInstallerMetadata() {
        InstallInfo info = ForgeInstaller.readInstallInfo(new StringReader("""
            {"installer":{"minecraft":"1.20.1","forge":"47.4.22","hash":"abc"},"libraries":{}}
            """));

        assertEquals("1.20.1", info.installer.minecraft);
        assertEquals("47.4.22", info.installer.forge);
        assertTrue(info.libraries.isEmpty());
    }

    @Test
    void rejectsIncompleteInstallerMetadata() {
        IllegalArgumentException missingMinecraft = assertThrows(IllegalArgumentException.class,
            () -> ForgeInstaller.readInstallInfo(new StringReader("""
                {"installer":{"forge":"47.4.22","hash":"abc"},"libraries":{}}
                """)));
        assertTrue(missingMinecraft.getMessage().contains("Minecraft"));

        IllegalArgumentException missingLibraries = assertThrows(IllegalArgumentException.class,
            () -> ForgeInstaller.readInstallInfo(new StringReader("""
                {"installer":{"minecraft":"1.20.1","forge":"47.4.22","hash":"abc"}}
                """)));
        assertTrue(missingLibraries.getMessage().contains("library"));
    }

    @Test
    void parsesOrderedForgeArguments() throws Exception {
        Path module = Files.createFile(directory.resolve("module.jar"));
        Path legacy = Files.createFile(directory.resolve("legacy.jar"));
        InstallInfo info = installInfo();

        ForgeArguments arguments = ForgeInstaller.parseArguments(List.of(
            "-p " + module,
            "--add-opens java.base/java.util.jar=cpw.mods.securejarhandler",
            "--add-exports java.base/sun.security.util=cpw.mods.securejarhandler",
            "-Dexample=value",
            "-DlegacyClassPath=" + legacy,
            "cpw.mods.bootstraplauncher.BootstrapLauncher",
            "--launchTarget forgeserver"
        ), info, directory);

        assertEquals("cpw.mods.bootstraplauncher.BootstrapLauncher", arguments.mainClass());
        assertEquals(List.of("--launchTarget", "forgeserver"), arguments.gameArguments());
        assertEquals(List.of(module), arguments.modulePath());
        assertTrue(arguments.legacyClassPath().contains(legacy));
        assertEquals("value", arguments.systemProperties().get("example"));
        assertTrue(arguments.systemProperties().get("legacyClassPath").contains(legacy.toString()));
        assertEquals(List.of("java.base/java.util.jar=cpw.mods.securejarhandler"), arguments.opens());
        assertEquals(List.of(
            "cpw.mods.bootstraplauncher/cpw.mods.bootstraplauncher=ALL-UNNAMED",
            "java.base/sun.security.util=cpw.mods.securejarhandler"
        ), arguments.exports());
    }

    @Test
    void rejectsMalformedPropertiesAndMissingMainClass() {
        InstallInfo info = installInfo();
        IllegalArgumentException malformed = assertThrows(IllegalArgumentException.class,
            () -> ForgeInstaller.parseArguments(List.of("-Dbroken"), info, directory));
        assertTrue(malformed.getMessage().contains("-Dbroken"));

        IllegalArgumentException missingMain = assertThrows(IllegalArgumentException.class,
            () -> ForgeInstaller.parseArguments(List.of("-Dvalid=value"), info, directory));
        assertTrue(missingMain.getMessage().contains("main class"));
    }

    @Test
    void rejectsMissingClasspathFiles() {
        InstallInfo info = installInfo();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ForgeInstaller.parseArguments(List.of(
                "-DlegacyClassPath=" + directory.resolve("missing.jar"),
                "example.Main"
            ), info, directory));
        assertTrue(exception.getMessage().contains("missing.jar"));
    }

    @Test
    void detectsMissingForgeInstallation() throws Exception {
        Path argsFile = directory.resolve("unix_args.txt");
        assertTrue(ForgeInstaller.isForgeInstallRequired(argsFile));

        Path module = Files.createFile(directory.resolve("module.jar"));
        Files.writeString(argsFile, "-p " + module + System.lineSeparator() + "example.Main");
        assertFalse(ForgeInstaller.isForgeInstallRequired(argsFile));

        Files.writeString(argsFile, "-p " + directory.resolve("missing.jar") + System.lineSeparator() + "example.Main");
        assertTrue(ForgeInstaller.isForgeInstallRequired(argsFile));
    }

    private static InstallInfo installInfo() {
        InstallInfo info = new InstallInfo();
        info.installer = new InstallInfo.Installer();
        info.installer.minecraft = "1.20.1";
        info.installer.forge = "47.4.22";
        info.installer.hash = "abc";
        info.libraries = new LinkedHashMap<>();
        return info;
    }
}
