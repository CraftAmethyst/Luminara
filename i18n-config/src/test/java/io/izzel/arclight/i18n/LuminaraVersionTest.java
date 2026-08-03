package io.izzel.arclight.i18n;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuminaraVersionTest {

    @Test
    void loadsTheCompleteCompatibilityMatrix() {
        Properties properties = LuminaraVersion.load(new ByteArrayInputStream("""
                minecraftVersion=1.20.1
                forgeVersion=47.4.22
                javaVersion=17
                bukkitPackage=v1_20_R1
                version=1.0.14
                gitCommit=abc1234
                """.getBytes(StandardCharsets.ISO_8859_1)));

        assertEquals("1.20.1", LuminaraVersion.required(properties, "minecraftVersion"));
        assertEquals("47.4.22", LuminaraVersion.required(properties, "forgeVersion"));
        assertEquals("17", LuminaraVersion.required(properties, "javaVersion"));
        assertEquals("v1_20_R1", LuminaraVersion.required(properties, "bukkitPackage"));
        assertEquals("1.0.14", LuminaraVersion.required(properties, "version"));
        assertEquals("abc1234", LuminaraVersion.required(properties, "gitCommit"));
    }

    @Test
    void rejectsIncompleteMetadata() {
        Properties properties = new Properties();
        properties.setProperty("version", "1.0.14");

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> LuminaraVersion.required(properties, "minecraftVersion"));

        assertTrue(failure.getMessage().contains("minecraftVersion"));
    }

    @Test
    void rejectsAnUnsupportedInstallerMatrixBeforeInstallation() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> LuminaraVersion.verify("1.20.2", "48.0.0"));

        assertTrue(failure.getMessage().contains("expected Minecraft 1.20.1 / Forge 47.4.22"));
    }
}
