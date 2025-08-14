package io.izzel.arclight.common.adventure;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class for Adventure integration in Luminara.
 * This class manages Adventure-specific settings and configurations.
 */
public final class AdventureConfig {

    private static final Logger LOGGER = ArclightI18nLogger.getLogger("AdventureConfig");

    // Adventure feature flags
    private static boolean enableMiniMessage = true;
    private static boolean enableLegacySupport = true;
    private static boolean enableColorDownsampling = false;
    private static boolean enableStrictMode = false;

    // MiniMessage configuration
    private static TagResolver miniMessageTagResolver = createDefaultTagResolver();

    private AdventureConfig() {
    }

    /**
     * Initialize Adventure configuration.
     */
    public static void initialize() {
        LOGGER.info("Initializing Adventure configuration for Luminara");

        // Load configuration from system properties or config files
        loadConfiguration();

        LOGGER.debug("Adventure configuration initialized - MiniMessage: {}, Legacy: {}, ColorDownsampling: {}, Strict: {}",
                enableMiniMessage, enableLegacySupport, enableColorDownsampling, enableStrictMode);
    }

    /**
     * Load configuration from system properties.
     */
    private static void loadConfiguration() {
        enableMiniMessage = Boolean.parseBoolean(System.getProperty("arclight.adventure.minimessage", "true"));
        enableLegacySupport = Boolean.parseBoolean(System.getProperty("arclight.adventure.legacy", "true"));
        enableColorDownsampling = Boolean.parseBoolean(System.getProperty("arclight.adventure.colordownsampling", "false"));
        enableStrictMode = Boolean.parseBoolean(System.getProperty("arclight.adventure.strict", "false"));
    }

    /**
     * Create the default tag resolver for MiniMessage.
     */
    private static TagResolver createDefaultTagResolver() {
        TagResolver.Builder builder = TagResolver.builder();

        // Add standard tags
        builder.resolver(StandardTags.color());
        builder.resolver(StandardTags.decorations());
        builder.resolver(StandardTags.gradient());
        builder.resolver(StandardTags.rainbow());
        builder.resolver(StandardTags.reset());
        builder.resolver(StandardTags.newline());

        if (!enableStrictMode) {
            // Add additional tags in non-strict mode
            builder.resolver(StandardTags.clickEvent());
            builder.resolver(StandardTags.hoverEvent());
            builder.resolver(StandardTags.insertion());
            builder.resolver(StandardTags.font());
        }

        return builder.build();
    }

    /**
     * Create a MiniMessage instance with current configuration.
     */
    public static MiniMessage createMiniMessage() {
        if (!enableMiniMessage) {
            LOGGER.warn("MiniMessage is disabled, returning basic instance");
            return MiniMessage.miniMessage();
        }

        MiniMessage.Builder builder = MiniMessage.builder();
        builder.tags(miniMessageTagResolver);

        if (enableStrictMode) {
            builder.strict(true);
        }

        return builder.build();
    }

    /**
     * Check if MiniMessage is enabled.
     */
    public static boolean isMiniMessageEnabled() {
        return enableMiniMessage;
    }

    /**
     * Set MiniMessage enabled state.
     */
    public static void setMiniMessageEnabled(boolean enabled) {
        enableMiniMessage = enabled;
        LOGGER.debug("MiniMessage enabled state changed to: {}", enabled);
    }

    /**
     * Check if legacy support is enabled.
     */
    public static boolean isLegacySupportEnabled() {
        return enableLegacySupport;
    }

    /**
     * Set legacy support enabled state.
     */
    public static void setLegacySupportEnabled(boolean enabled) {
        enableLegacySupport = enabled;
        LOGGER.debug("Legacy support enabled state changed to: {}", enabled);
    }

    /**
     * Check if color downsampling is enabled.
     */
    public static boolean isColorDownsamplingEnabled() {
        return enableColorDownsampling;
    }

    /**
     * Set color downsampling enabled state.
     */
    public static void setColorDownsamplingEnabled(boolean enabled) {
        enableColorDownsampling = enabled;
        LOGGER.debug("Color downsampling enabled state changed to: {}", enabled);
    }

    /**
     * Check if strict mode is enabled.
     */
    public static boolean isStrictModeEnabled() {
        return enableStrictMode;
    }

    /**
     * Set strict mode enabled state.
     */
    public static void setStrictModeEnabled(boolean enabled) {
        enableStrictMode = enabled;
        miniMessageTagResolver = createDefaultTagResolver(); // Recreate resolver
        LOGGER.debug("Strict mode enabled state changed to: {}", enabled);
    }

    /**
     * Get the current tag resolver.
     */
    public static TagResolver getTagResolver() {
        return miniMessageTagResolver;
    }

    /**
     * Update the tag resolver.
     */
    public static void setTagResolver(TagResolver resolver) {
        miniMessageTagResolver = resolver;
        LOGGER.debug("Tag resolver updated");
    }

    /**
     * Reset configuration to defaults.
     */
    public static void resetToDefaults() {
        enableMiniMessage = true;
        enableLegacySupport = true;
        enableColorDownsampling = false;
        enableStrictMode = false;
        miniMessageTagResolver = createDefaultTagResolver();
        LOGGER.info("Adventure configuration reset to defaults");
    }

    /**
     * Get configuration summary.
     */
    public static String getConfigurationSummary() {
        return String.format("Adventure Config: MiniMessage=%s, Legacy=%s, ColorDownsampling=%s, Strict=%s",
                enableMiniMessage, enableLegacySupport, enableColorDownsampling, enableStrictMode);
    }
}
