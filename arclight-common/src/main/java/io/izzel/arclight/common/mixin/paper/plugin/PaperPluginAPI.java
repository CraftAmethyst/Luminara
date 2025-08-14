package io.izzel.arclight.common.mixin.paper.plugin;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import io.papermc.paper.plugin.loader.PaperPluginClassLoader;

/**
 * Interface defining Paper plugin API methods that will be mixed into JavaPlugin.
 * This interface provides all the Paper-specific functionality that plugins can use.
 */
public interface PaperPluginAPI {
    
    /**
     * Gets the Paper plugin description file for this plugin.
     * 
     * @return the Paper plugin description, or null if this is not a Paper plugin
     */
    PaperPluginDescriptionFile getPaperDescription();
    
    /**
     * Checks if this plugin is loaded by the Paper plugin system.
     * 
     * @return true if this is a Paper plugin, false otherwise
     */
    boolean isPaperPlugin();
    
    /**
     * Gets the Paper plugin class loader if this plugin is loaded by the Paper system.
     * 
     * @return the Paper plugin class loader, or null if not a Paper plugin
     */
    PaperPluginClassLoader getPaperClassLoader();
    
    /**
     * Checks if this plugin has an open classloader that allows other plugins to access its classes.
     * 
     * @return true if the classloader is open, false otherwise
     */
    boolean hasOpenClassloader();
    
    /**
     * Gets information about the plugin's classloader configuration.
     * 
     * @return classloader information string
     */
    String getClassLoaderInfo();
    
    /**
     * Gets the Paper plugin loader name.
     * 
     * @return the loader name
     */
    String getPaperLoaderName();
    
    /**
     * Gets the Paper plugin loader version.
     * 
     * @return the loader version
     */
    String getPaperLoaderVersion();
    
    /**
     * Checks if this loader supports the given plugin type.
     * 
     * @param pluginType the plugin type to check
     * @return true if supported, false otherwise
     */
    boolean supportsPaperPluginType(String pluginType);
    
    /**
     * Gets additional information about this loader.
     * 
     * @return additional loader information
     */
    String getPaperAdditionalInfo();
    
    /**
     * Checks if this loader provides classloader isolation.
     * 
     * @return true if classloader isolation is provided
     */
    boolean providesPaperClassloaderIsolation();
    
    /**
     * Gets the priority of this loader.
     * 
     * @return the loader priority
     */
    int getPaperLoaderPriority();
}
