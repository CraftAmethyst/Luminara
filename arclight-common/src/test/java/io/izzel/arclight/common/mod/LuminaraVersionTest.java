package io.izzel.arclight.common.mod;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuminaraVersionTest {

    @Test
    void loadsTheCompleteCompatibilityMatrix() {
        LuminaraVersion version = LuminaraVersion.load(new ByteArrayInputStream("""
                luminara.version=1.0.14
                minecraft.version=1.20.1
                forge.version=47.4.22
                craftbukkit.package=v1_20_R1
                git.commit=abc1234
                """.getBytes(StandardCharsets.ISO_8859_1)));

        assertEquals("1.0.14", version.luminara());
        assertEquals("1.20.1", version.minecraft());
        assertEquals("47.4.22", version.forge());
        assertEquals("v1_20_R1", version.craftBukkitPackage());
        assertEquals("abc1234", version.gitCommit());
        assertEquals("Luminara 1.0.14 (Minecraft 1.20.1, Forge 47.4.22, Bukkit v1_20_R1, commit abc1234)",
                version.compatibilityLine());
    }

    @Test
    void rejectsIncompleteMetadata() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> LuminaraVersion.load(new ByteArrayInputStream(
                        "luminara.version=1.0.14\n".getBytes(StandardCharsets.ISO_8859_1))));

        assertTrue(failure.getMessage().contains("minecraft.version"));
    }
}
