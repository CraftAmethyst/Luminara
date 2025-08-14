package io.papermc.paper.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Paper plugin loader interface that allows plugins to identify their own plugin loader.
 * This interface provides methods for plugins to interact with their loading environment.
 */
public interface PluginLoader {
    
    /**
     * Gets the name of this plugin loader.
     * 
     * @return the loader name
     */
    @NotNull
    String getName();
    
    /**
     * Gets the version of this plugin loader.
     * 
     * @return the loader version
     */
    @NotNull
    String getVersion();
    
    /**
     * Checks if this loader supports the given plugin type.
     * 
     * @param pluginType the plugin type to check
     * @return true if supported, false otherwise
     */
    boolean supports(@NotNull String pluginType);
    
    /**
     * Gets additional information about this loader.
     * 
     * @return additional loader information, or null if none
     */
    @Nullable
    String getAdditionalInfo();
    
    /**
     * Checks if this loader provides classloader isolation.
     * 
     * @return true if classloader isolation is provided
     */
    boolean providesClassloaderIsolation();
    
    /**
     * Gets the priority of this loader when multiple loaders can handle the same plugin type.
     * Higher values indicate higher priority.
     * 
     * @return the loader priority
     */
    int getPriority();
}
