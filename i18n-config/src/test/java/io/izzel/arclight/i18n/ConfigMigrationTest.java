package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.PermissionForwarding;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        "false, DISABLED",
    })
    void migratesPermissionForwardingAndRemovesUnsafeSections(
        String legacy,
        PermissionForwarding expected
    ) throws Exception {
        ConfigurationNode root = load(
            """
                _v: 1
                locale:
                  fallback: en_us
                optimization:
                  cache-plugin-class: true
                  async-system:
                    enabled: true
                  entity-optimization:
                    reduce-entity-updates: true
                  memory-optimization:
                    cache-cleanup-enabled: true
                  world-creation:
                    skip-spawn-chunk-loading: true
                  chunk-optimization:
                    aggressive-chunk-unloading: true
                compatibility:
                  forward-permission: %s
                async-world-save:
                  enabled: true
                """.formatted(legacy)
        );

        ConfigMigration.Result result = ConfigMigration.migrate(root);

        assertEquals(2, root.getNode("_v").getInt());
        assertEquals(
            expected.name(),
            root.getNode("compatibility", "permission-forwarding").getString()
        );
        assertTrue(
            root.getNode("compatibility", "forward-permission").isVirtual()
        );
        assertEquals(6, result.removedUnsafeSettings().size());
        for (String path : result.removedUnsafeSettings()) {
            assertTrue(
                root.getNode((Object[]) path.split("\\.")).isVirtual(),
                path
            );
        }
    }
}
