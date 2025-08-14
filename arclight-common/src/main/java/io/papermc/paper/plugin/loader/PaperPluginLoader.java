package io.papermc.paper.plugin.loader;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Paper plugin loader that supports classloader isolation and enhanced dependency management.
 * This loader handles paper-plugin.yml files and provides better plugin isolation.
 */
public class PaperPluginLoader implements PluginLoader {
    
    private final Server server;
    private final Pattern[] fileFilters = new Pattern[]{Pattern.compile("\\.jar$")};
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Map<String, PaperPluginClassLoader> loaders = new ConcurrentHashMap<>();
    private final Logger logger;
    
    public PaperPluginLoader(@NotNull Server instance) {
        this.server = instance;
        this.logger = Logger.getLogger("PaperPluginLoader");
    }
    
    @Override
    @NotNull
    public Plugin loadPlugin(@NotNull File file) throws InvalidPluginException {
        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
        }
        
        final PaperPluginDescriptionFile description;
        try {
            description = getPaperPluginDescription(file);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidPluginException(ex);
        }
        
        final File parentFile = file.getParentFile();
        final File dataFolder = new File(parentFile, description.getName());
        
        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(String.format(
                    "Projected datafolder: '%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getName(),
                    file
            ));
        }
        
        // Check for conflicting plugin names
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getName().equalsIgnoreCase(description.getName())) {
                throw new InvalidPluginException(String.format(
                        "Plugin with name '%s' already loaded!",
                        description.getName()
                ));
            }
        }
        
        PaperPluginClassLoader loader;
        try {
            loader = new PaperPluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
        } catch (InvalidPluginException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvalidPluginException(ex);
        }
        
        loaders.put(description.getName(), loader);
        
        return loader.getPlugin();
    }
    
    @Override
    @NotNull
    public PluginDescriptionFile getPluginDescription(@NotNull File file) throws InvalidDescriptionException {
        // For compatibility, we'll throw an exception since this loader is specifically for Paper plugins
        throw new InvalidDescriptionException("Use getPaperPluginDescription() for Paper plugins");
    }

    @NotNull
    public PaperPluginDescriptionFile getPaperPluginDescription(@NotNull File file) throws InvalidDescriptionException {
        if (!file.exists()) {
            throw new InvalidDescriptionException(new FileNotFoundException(file.getPath() + " does not exist"));
        }

        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("paper-plugin.yml");
            if (entry == null) {
                // Fallback to plugin.yml for compatibility
                entry = jar.getJarEntry("plugin.yml");
                if (entry == null) {
                    throw new InvalidDescriptionException("Jar does not contain paper-plugin.yml or plugin.yml");
                }
                // If using plugin.yml, we need to create a compatible description
                // For now, throw an exception to indicate this is not a Paper plugin
                throw new InvalidDescriptionException("File contains plugin.yml but not paper-plugin.yml - use JavaPluginLoader instead");
            }

            try (InputStream stream = jar.getInputStream(entry)) {
                return new PaperPluginDescriptionFile(stream);
            }
        } catch (IOException | PaperPluginDescriptionFile.InvalidDescriptionException ex) {
            throw new InvalidDescriptionException(ex);
        }
    }
    
    @Override
    @NotNull
    public Pattern[] getPluginFileFilters() {
        return fileFilters.clone();
    }
    
    @Override
    @NotNull
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull Plugin plugin) {
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
        
        for (java.lang.reflect.Method method : listener.getClass().getMethods()) {
            EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            
            // Check method signature
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }
            
            final Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);
            
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());
            
            EventExecutor executor = (listener1, event) -> {
                try {
                    if (!eventClass.isAssignableFrom(event.getClass())) {
                        return;
                    }
                    method.invoke(listener1, event);
                } catch (Exception ex) {
                    throw new EventException(ex);
                }
            };
            
            eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
        }
        
        return ret;
    }
    
    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        if (!(plugin instanceof JavaPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }
        
        if (!plugin.isEnabled()) {
            plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
            
            JavaPlugin jPlugin = (JavaPlugin) plugin;
            
            try {
                // Use reflection to access protected setEnabled method
                java.lang.reflect.Method setEnabledMethod = JavaPlugin.class.getDeclaredMethod("setEnabled", boolean.class);
                setEnabledMethod.setAccessible(true);
                setEnabledMethod.invoke(jPlugin, true);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
    }
    
    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        if (!(plugin instanceof JavaPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }
        
        if (plugin.isEnabled()) {
            String message = String.format("Disabling %s", plugin.getDescription().getFullName());
            plugin.getLogger().info(message);
            
            // server.getPluginManager().callEvent(new PluginDisableEvent(plugin));
            
            JavaPlugin jPlugin = (JavaPlugin) plugin;
            ClassLoader cloader = jPlugin.getClass().getClassLoader();
            
            try {
                // Use reflection to access protected setEnabled method
                java.lang.reflect.Method setEnabledMethod = JavaPlugin.class.getDeclaredMethod("setEnabled", boolean.class);
                setEnabledMethod.setAccessible(true);
                setEnabledMethod.invoke(jPlugin, false);
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
            
            if (cloader instanceof PaperPluginClassLoader) {
                PaperPluginClassLoader loader = (PaperPluginClassLoader) cloader;
                loaders.remove(plugin.getDescription().getName());
                
                try {
                    loader.close();
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.WARNING, "Error closing the Plugin Class Loader for " + plugin.getDescription().getFullName(), ex);
                }
            }
        }
    }
    
    /**
     * Gets the class loader for a specific plugin.
     */
    @Nullable
    public PaperPluginClassLoader getPluginClassLoader(@NotNull String pluginName) {
        return loaders.get(pluginName);
    }
    
    /**
     * Gets all loaded plugin class loaders.
     */
    @NotNull
    public Collection<PaperPluginClassLoader> getPluginClassLoaders() {
        return Collections.unmodifiableCollection(loaders.values());
    }
    
    /**
     * Checks if a class is available in any loaded plugin.
     */
    @Nullable
    public Class<?> getClassByName(@NotNull String name) {
        return classes.get(name);
    }
    
    /**
     * Sets a class in the global class cache.
     */
    void setClass(@NotNull String name, @NotNull Class<?> clazz) {
        classes.put(name, clazz);
    }
    
    /**
     * Removes a class from the global class cache.
     */
    void removeClass(@NotNull String name) {
        classes.remove(name);
    }
    
    /**
     * Gets the server instance.
     */
    @NotNull
    public Server getServer() {
        return server;
    }
}
