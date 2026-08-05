package io.izzel.arclight.i18n;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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
            serialized.contains("# Config version number, do not edit.\n_v: 2")
        );
        assertTrue(serialized.contains("# Language/I18n settings\nlocale:"));
        assertTrue(serialized.contains("  cache-plugin-class: true"));
        assertTrue(serialized.contains("  extra-logic-worlds:"));
        assertTrue(serialized.contains("- example.First"));
        assertTrue(serialized.contains("- example.Second"));
        assertFalse(serialized.contains("compatibility: {"));
        assertFalse(serialized.contains("extra-logic-worlds: ["));
    }

    @Test
    void injectsCommentsFromFlattenedLocaleKeys() throws Exception {
        String injected = I18nCommentInjector.injectComments(
            "_v: 2\n",
            "es_es"
        );

        assertTrue(
            injected.contains(
                "# Repositorio: https://github.com/CraftAmethyst/Luminara"
            )
        );
        assertTrue(
            injected.contains(
                "# Versión de la configuración, no editar.\n_v: 2"
            )
        );
    }

    @Test
    void repairsExistingConfigurationWithoutComments() throws Exception {
        Path config = directory.resolve("luminara.yml");
        String template;
        try (
            var stream = ArclightConfig.class.getResourceAsStream(
                "/META-INF/luminara.yml"
            )
        ) {
            template = new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
            );
        }
        String currentLocale = ArclightLocale.getInstance().current();
        assertFalse(
            I18nCommentInjector.hasInjectedComments(template, currentLocale)
        );
        Files.writeString(config, template, StandardCharsets.UTF_8);

        ArclightConfig.loadExistingConfig(config);

        assertTrue(
            I18nCommentInjector.hasInjectedComments(
                Files.readString(config, StandardCharsets.UTF_8),
                currentLocale
            )
        );
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
