package io.izzel.arclight.i18n;

import com.google.common.reflect.TypeToken;
import io.izzel.arclight.i18n.conf.ConfigSpec;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class ArclightConfig {

    private static ArclightConfig instance;

    private final ConfigurationNode node;
    private final ConfigSpec spec;

    public ArclightConfig(ConfigurationNode node) throws ObjectMappingException {
        this.node = node;
        this.spec = this.node.getValue(TypeToken.of(ConfigSpec.class));
        if (this.spec == null || this.spec.getVersion() != ConfigMigration.CURRENT_VERSION) {
            throw new ObjectMappingException("Invalid configuration key _v: expected " + ConfigMigration.CURRENT_VERSION);
        }
    }

    public ConfigurationNode getNode() {
        return node;
    }

    public ConfigSpec getSpec() {
        return spec;
    }

    public ConfigurationNode get(String path) {
        return this.node.getNode((Object[]) path.split("\\."));
    }

    public static ConfigSpec spec() {
        return instance.spec;
    }

    private static void load() throws Exception {
        Path path = Paths.get("luminara.yml");
        ConfigurationNode defaults = loadDefaults();
        ConfigurationNode configured;
        ConfigMigration.Result migration;

        if (Files.isRegularFile(path)) {
            configured = loader(path).load();
            migration = ConfigMigration.migrate(configured);
            configured.mergeValuesFrom(defaults);
        } else {
            configured = defaults;
            migration = ConfigMigration.migrate(configured);
        }

        configured.getNode("locale", "current").setValue(ArclightLocale.getInstance().getCurrent());
        instance = new ArclightConfig(configured);
        saveAtomically(path, configured);
        migration.removedUnsafeSettings().forEach(setting ->
            System.err.println("Removed unsupported unsafe setting: " + setting)
        );
    }

    private static ConfigurationNode loadDefaults() throws IOException {
        try (InputStream stream = ArclightConfig.class.getResourceAsStream("/META-INF/luminara.yml")) {
            if (stream == null) {
                throw new IOException("Missing /META-INF/luminara.yml");
            }
            Path temporary = Files.createTempFile("luminara-defaults", ".yml");
            try {
                Files.writeString(temporary, new String(stream.readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                return loader(temporary).load();
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private static YAMLConfigurationLoader loader(Path path) {
        return YAMLConfigurationLoader.builder()
            .setPath(path)
            .setIndent(2)
            .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
            .build();
    }

    static void saveAtomically(Path path, ConfigurationNode node) throws IOException {
        Path absolute = path.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
        try {
            loader(temporary).save(node);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static {
        try {
            load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
