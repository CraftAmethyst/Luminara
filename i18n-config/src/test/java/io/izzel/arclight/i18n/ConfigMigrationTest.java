package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.PermissionForwarding;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigMigrationTest {

    private static ConfigurationNode load(String yaml) throws Exception {
        return YAMLConfigurationLoader.builder()
            .setSource(() -> new BufferedReader(new StringReader(yaml)))
            .build()
            .load();
    }

    @ParameterizedTest
    @CsvSource({
        "true, FORGE_TO_BUKKIT",
        "'true', FORGE_TO_BUKKIT",
        "reverse, BUKKIT_TO_FORGE",
        "false, DISABLED"
    })
    void migratesPermissionForwardingAndPreservesUnknownKeys(String legacy, PermissionForwarding expected) throws Exception {
        ConfigurationNode root = load("""
            _v: 1
            compatibility:
              forward-permission: %s
              custom-key: retained
            optimization:
              async-system:
                enabled: true
            """.formatted(legacy));

        ConfigMigration.Result result = ConfigMigration.migrate(root);

        assertEquals(2, root.getNode("_v").getInt());
        assertEquals(expected.name(), root.getNode("compatibility", "permission-forwarding").getString());
        assertTrue(root.getNode("compatibility", "forward-permission").isVirtual());
        assertEquals("retained", root.getNode("compatibility", "custom-key").getString());
        assertEquals(1, result.removedUnsafeSettings().size());
        assertTrue(root.getNode("optimization", "async-system").isVirtual());
    }

    @Test
    void rejectsFutureConfigurationVersionsWithoutMutation() throws Exception {
        ConfigurationNode root = load("""
            _v: 3
            custom-key: retained
            """);

        assertThrows(IllegalArgumentException.class, () -> ConfigMigration.migrate(root));
        assertEquals(3, root.getNode("_v").getInt());
        assertEquals("retained", root.getNode("custom-key").getString());
    }
}
