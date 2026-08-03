package io.izzel.arclight.i18n;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArclightConfigPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void atomicallyPersistsAMigratedConfiguration() throws Exception {
        Path config = directory.resolve("luminara.yml");
        ConfigurationNode node = YAMLConfigurationLoader.builder().build().createEmptyNode();
        node.getNode("_v").setValue(2);
        node.getNode("locale", "current").setValue("en_us");

        ArclightConfig.saveAtomically(config, node);

        ConfigurationNode reloaded = YAMLConfigurationLoader.builder().setPath(config).build().load();
        assertEquals(2, reloaded.getNode("_v").getInt());
        assertEquals("en_us", reloaded.getNode("locale", "current").getString());
    }

    @Test
    void invalidYamlLeavesOriginalBytesUntouched() throws Exception {
        Path config = directory.resolve("luminara.yml");
        String original = "_v: 1\ncompatibility: [\n";
        Files.writeString(config, original, StandardCharsets.UTF_8);

        assertThrows(Exception.class, () -> ArclightConfig.loadExistingConfig(config));

        assertEquals(original, Files.readString(config, StandardCharsets.UTF_8));
        assertTrue(Files.list(directory).noneMatch(path -> path.getFileName().toString().endsWith(".tmp")));
    }
}
