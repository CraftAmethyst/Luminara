package io.izzel.arclight.i18n;

import com.google.common.reflect.TypeToken;
import io.izzel.arclight.i18n.conf.ConfigSpec;
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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

public class ArclightConfig {

    private static ArclightConfig instance;

    static {
        try {
            load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ConfigurationNode node;
    private final ConfigSpec spec;

    public ArclightConfig(ConfigurationNode node)
        throws ObjectMappingException {
        this.node = node;
        this.spec = this.node.getValue(TypeToken.of(ConfigSpec.class));
        if (
            spec == null || spec.getVersion() != ConfigMigration.CURRENT_VERSION
        ) {
            throw new ObjectMappingException(
                "Invalid configuration key _v: expected " +
                    ConfigMigration.CURRENT_VERSION
            );
        }
    }

    public static ConfigSpec spec() {
        return instance.spec;
    }

    private static void load() throws Exception {
        Path path = Paths.get("luminara.yml");
        boolean fileExisted = Files.exists(path);

        if (!fileExisted) {
            instance = createInitialConfig(path);
        } else {
            instance = loadExistingConfig(path);
        }

        LoggingConfigurator.apply(instance.spec);
    }

    private static ArclightConfig createInitialConfig(Path path)
        throws Exception {
        String currentLocale = ArclightLocale.getInstance().current();
        String processed;
        try (
            InputStream stream = ArclightConfig.class.getResourceAsStream(
                "/META-INF/luminara.yml"
            )
        ) {
            if (stream == null) throw new IOException(
                "Missing /META-INF/luminara.yml"
            );
            String content = new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
            );
            processed = I18nCommentInjector.injectComments(
                content,
                currentLocale
            );
        }
        Path temporary = Files.createTempFile(
            path.toAbsolutePath().getParent(),
            "luminara",
            ".yml"
        );
        try {
            Files.writeString(temporary, processed, StandardCharsets.UTF_8);
            ConfigurationNode node = loader(temporary).load();
            ConfigMigration.migrate(node);
            ArclightConfig config = new ArclightConfig(node);
            saveAtomically(path, node);
            return config;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static ArclightConfig loadExistingConfig(Path path) throws Exception {
        YAMLConfigurationLoader loader = loader(path);
        ConfigurationNode original = loader.load();
        String originalContent = Files.readString(path, StandardCharsets.UTF_8);
        ConfigurationNode migrated = original.copy();
        ConfigMigration.Result migration = ConfigMigration.migrate(migrated);
        migrated
            .getNode("locale", "current")
            .setValue(ArclightLocale.getInstance().current());
        String currentLocale = migrated
            .getNode("locale", "current")
            .getString("zh_cn");
        ArclightConfig config = new ArclightConfig(migrated);
        if (
            migration.changed() ||
            !currentLocale.equals(
                original.getNode("locale", "current").getString("")
            ) ||
            !I18nCommentInjector.hasInjectedComments(
                originalContent,
                currentLocale
            )
        ) {
            saveAtomically(path, migrated);
        }
        migration
            .removedUnsafeSettings()
            .forEach(setting ->
                System.err.println(
                    "Removed unsupported unsafe setting: " + setting
                )
            );
        return config;
    }

    private static YAMLConfigurationLoader loader(Path path) {
        return YAMLConfigurationLoader.builder()
            .setPath(path)
            .setIndent(2)
            .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
            .build();
    }

    static void saveAtomically(Path path, ConfigurationNode node)
        throws IOException {
        Path absolute = path.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(
            parent,
            absolute.getFileName().toString(),
            ".tmp"
        );
        try {
            loader(temporary).save(node);
            String currentLocale = node
                .getNode("locale", "current")
                .getString("zh_cn");
            String localized;
            try {
                localized = I18nCommentInjector.injectComments(
                    Files.readString(temporary, StandardCharsets.UTF_8),
                    currentLocale
                );
            } catch (Exception exception) {
                throw new IOException(
                    "Unable to inject localized configuration comments",
                    exception
                );
            }
            Files.writeString(temporary, localized, StandardCharsets.UTF_8);
            try (
                FileChannel channel = FileChannel.open(
                    temporary,
                    StandardOpenOption.WRITE
                )
            ) {
                channel.force(true);
            }
            try {
                Files.move(
                    temporary,
                    absolute,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                    temporary,
                    absolute,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public ConfigSpec getSpec() {
        return spec;
    }

    public ConfigurationNode getNode() {
        return node;
    }

    public ConfigurationNode get(String path) {
        return this.node.getNode((Object) path.split("\\."));
    }
}
