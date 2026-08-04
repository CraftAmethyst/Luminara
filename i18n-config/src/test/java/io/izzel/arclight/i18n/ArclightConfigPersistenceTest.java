package io.izzel.arclight.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArclightConfigPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void atomicallyPersistsAMigratedConfiguration() throws Exception {
        Path config = directory.resolve("luminara.yml");
        ConfigurationNode node = YAMLConfigurationLoader.builder()
            .build()
            .createEmptyNode();
        node.getNode("_v").setValue(2);
        node.getNode("locale", "current").setValue("en_us");

        node.getNode("optimization", "cache-plugin-class").setValue(true);
        node.getNode("compatibility", "extra-logic-worlds").setValue(
            java.util.List.of("example.First", "example.Second")
        );
        ArclightConfig.saveAtomically(config, node);

        ConfigurationNode reloaded = YAMLConfigurationLoader.builder()
            .setPath(config)
            .build()
            .load();
        assertEquals(2, reloaded.getNode("_v").getInt());
        assertEquals(
            "en_us",
            reloaded.getNode("locale", "current").getString()
        );
        String serialized = Files.readString(config, StandardCharsets.UTF_8);
        assertTrue(
            serialized.contains("optimization:\n  cache-plugin-class: true")
        );
        assertTrue(
            serialized.contains(
                "extra-logic-worlds:\n  - example.First\n  - example.Second"
            )
        );
        assertFalse(serialized.contains("{"));
        assertFalse(serialized.contains("["));
        assertFalse(serialized.contains(","));
    }

    @Test
    void invalidYamlLeavesOriginalBytesUntouched() throws Exception {
        Path config = directory.resolve("luminara.yml");
        String original = "_v: 1\ncompatibility: [\n";
        Files.writeString(config, original, StandardCharsets.UTF_8);

        assertThrows(Exception.class, () ->
            ArclightConfig.loadExistingConfig(config)
        );

        assertEquals(
            original,
            Files.readString(config, StandardCharsets.UTF_8)
        );
        assertTrue(
            Files.list(directory).noneMatch(path ->
                path.getFileName().toString().endsWith(".tmp")
            )
        );
    }
}
