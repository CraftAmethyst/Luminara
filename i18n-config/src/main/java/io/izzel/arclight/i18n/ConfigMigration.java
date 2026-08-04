package io.izzel.arclight.i18n;

import io.izzel.arclight.i18n.conf.PermissionForwarding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ninja.leaping.configurate.ConfigurationNode;

final class ConfigMigration {

    static final int CURRENT_VERSION = 2;
    private static final List<String> UNSAFE_SETTINGS = List.of(
        "optimization.async-system",
        "optimization.entity-optimization",
        "optimization.memory-optimization",
        "optimization.world-creation",
        "optimization.chunk-optimization",
        "async-world-save"
    );

    private ConfigMigration() {}

    static Result migrate(ConfigurationNode root) {
        int version = root.getNode("_v").getInt(1);
        if (version > CURRENT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported configuration version at _v: " + version
            );
        }
        List<String> removed = new ArrayList<>();
        if (version < 2) {
            migratePermissionForwarding(root);
            for (String path : UNSAFE_SETTINGS) {
                if (remove(root, path)) {
                    removed.add(path);
                }
            }
            root.getNode("_v").setValue(CURRENT_VERSION);
        }
        return new Result(
            root,
            List.copyOf(removed),
            version != CURRENT_VERSION
        );
    }

    private static void migratePermissionForwarding(ConfigurationNode root) {
        ConfigurationNode compatibility = root.getNode("compatibility");
        ConfigurationNode legacy = compatibility.getNode("forward-permission");
        Object raw = legacy.getValue();
        PermissionForwarding forwarding;
        if (raw instanceof Boolean enabled) {
            forwarding = enabled
                ? PermissionForwarding.FORGE_TO_BUKKIT
                : PermissionForwarding.DISABLED;
        } else {
            String value = raw == null
                ? ""
                : raw.toString().trim().toLowerCase(Locale.ROOT);
            forwarding = switch (value) {
                case "true" -> PermissionForwarding.FORGE_TO_BUKKIT;
                case "reverse" -> PermissionForwarding.BUKKIT_TO_FORGE;
                default -> PermissionForwarding.DISABLED;
            };
        }
        compatibility
            .getNode("permission-forwarding")
            .setValue(forwarding.name());
        compatibility.removeChild("forward-permission");
    }

    private static boolean remove(ConfigurationNode root, String path) {
        String[] elements = path.split("\\.");
        ConfigurationNode parent = root;
        for (int index = 0; index < elements.length - 1; index++) {
            parent = parent.getNode(elements[index]);
            if (parent.isVirtual()) {
                return false;
            }
        }
        ConfigurationNode candidate = parent.getNode(
            elements[elements.length - 1]
        );
        if (candidate.isVirtual()) {
            return false;
        }
        parent.removeChild(elements[elements.length - 1]);
        return true;
    }

    record Result(
        ConfigurationNode root,
        List<String> removedUnsafeSettings,
        boolean changed
    ) {}
}
