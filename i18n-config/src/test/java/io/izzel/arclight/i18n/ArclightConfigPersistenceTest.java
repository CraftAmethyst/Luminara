package io.izzel.arclight.i18n;

import com.google.common.reflect.TypeToken;
import io.izzel.arclight.i18n.conf.ConfigSpec;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArclightConfigPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void persistsBlockYamlWithLocalizedComments() throws Exception {
        Path config = directory.resolve("luminara.yml");
        var node = YAMLConfigurationLoader.builder().build().createEmptyNode();
        node.getNode("_v").setValue(2);
        node.getNode("locale", "current").setValue("en_us");
        node.getNode("compatibility", "extra-logic-worlds").setValue(java.util.List.of("example.First", "example.Second"));

        ArclightConfig.saveAtomically(config, node);

        String serialized = Files.readString(config, StandardCharsets.UTF_8);
        assertTrue(serialized.contains("# Config version number, do not edit.\n_v: 2"));
        assertTrue(serialized.contains("extra-logic-worlds:"));
        assertTrue(serialized.contains("- example.First"));
        assertFalse(serialized.contains("compatibility: {"));
    }

    @Test
    void bundledDefaultsDeserializeEmptyMaps() throws Exception {
        try (var stream = ArclightConfigPersistenceTest.class.getResourceAsStream("/META-INF/luminara.yml")) {
            assertNotNull(stream);
            var node = YAMLConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
                .build()
                .load();
            ConfigSpec spec = node.getValue(TypeToken.of(ConfigSpec.class));

            assertNotNull(spec);
            assertTrue(spec.getAsyncCatcher().getOverrides().isEmpty());
            assertTrue(spec.getCompat().getMaterials().isEmpty());
            assertTrue(spec.getCompat().getEntities().isEmpty());
        }
    }

    @Test
    void injectsCommentsFromFlattenedLocaleKeys() throws Exception {
        String injected = I18nCommentInjector.injectComments("_v: 2\n", "es_es");
        assertTrue(injected.contains("# Versión de la configuración, no editar.\n_v: 2"));
    }
}
