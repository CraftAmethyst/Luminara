package io.papermc.paper.plugin.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Paper plugin description file parser for paper-plugin.yml files.
 * This class handles the new Paper plugin format with enhanced dependency management
 * and classloader isolation features.
 */
public class PaperPluginDescriptionFile {
    
    private final String name;
    private final String version;
    private final String main;
    private final String description;
    private final List<String> authors;
    private final String website;
    private final String prefix;
    private final PluginLoadOrder load;
    private final List<String> loadBefore;
    private final List<String> loadAfter;
    private final List<String> dependencies;
    private final List<String> softDependencies;
    private final String apiVersion;
    private final Map<String, Object> extra;
    private final boolean hasOpenClassloader;
    
    /**
     * Creates a PaperPluginDescriptionFile from an InputStream containing paper-plugin.yml content.
     */
    public PaperPluginDescriptionFile(@NotNull InputStream stream) throws InvalidDescriptionException {
        YamlConfiguration config;
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        } catch (Exception e) {
            throw new InvalidDescriptionException("Failed to parse paper-plugin.yml", e);
        }
        
        // Required fields
        this.name = config.getString("name");
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new InvalidDescriptionException("Plugin name is required");
        }
        
        this.version = config.getString("version");
        if (this.version == null || this.version.trim().isEmpty()) {
            throw new InvalidDescriptionException("Plugin version is required");
        }
        
        this.main = config.getString("main");
        if (this.main == null || this.main.trim().isEmpty()) {
            throw new InvalidDescriptionException("Plugin main class is required");
        }
        
        // Optional fields
        this.description = config.getString("description", "");
        this.website = config.getString("website");
        this.prefix = config.getString("prefix");
        this.apiVersion = config.getString("api-version");
        
        // Authors
        List<String> authorList = new ArrayList<>();
        if (config.isList("authors")) {
            authorList.addAll(config.getStringList("authors"));
        } else if (config.isString("author")) {
            authorList.add(config.getString("author"));
        }
        this.authors = Collections.unmodifiableList(authorList);
        
        // Load order
        String loadStr = config.getString("load", "POSTWORLD");
        try {
            this.load = PluginLoadOrder.valueOf(loadStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDescriptionException("Invalid load order: " + loadStr);
        }
        
        // Paper-specific dependency management
        this.loadBefore = Collections.unmodifiableList(config.getStringList("loadbefore"));
        this.loadAfter = Collections.unmodifiableList(config.getStringList("loadafter"));
        
        // Traditional dependencies (for compatibility)
        this.dependencies = Collections.unmodifiableList(config.getStringList("depend"));
        this.softDependencies = Collections.unmodifiableList(config.getStringList("softdepend"));
        
        // Classloader configuration
        this.hasOpenClassloader = config.getBoolean("has-open-classloader", false);
        
        // Store extra configuration for future use
        this.extra = new HashMap<>();
        for (String key : config.getKeys(false)) {
            if (!isKnownKey(key)) {
                this.extra.put(key, config.get(key));
            }
        }
    }
    
    private boolean isKnownKey(String key) {
        return Set.of("name", "version", "main", "description", "authors", "author", 
                     "website", "prefix", "load", "loadbefore", "loadafter", 
                     "depend", "softdepend", "api-version", "has-open-classloader").contains(key);
    }
    
    @NotNull
    public String getName() {
        return name;
    }
    
    @NotNull
    public String getVersion() {
        return version;
    }
    
    @NotNull
    public String getMain() {
        return main;
    }
    
    @NotNull
    public String getDescription() {
        return description;
    }
    
    @NotNull
    public List<String> getAuthors() {
        return authors;
    }
    
    @Nullable
    public String getWebsite() {
        return website;
    }
    
    @Nullable
    public String getPrefix() {
        return prefix;
    }
    
    @NotNull
    public PluginLoadOrder getLoad() {
        return load;
    }
    
    /**
     * Gets the list of plugins that should be loaded before this plugin.
     * This is Paper's enhanced dependency management system.
     */
    @NotNull
    public List<String> getLoadBefore() {
        return loadBefore;
    }
    
    /**
     * Gets the list of plugins that should be loaded after this plugin.
     * This is Paper's enhanced dependency management system.
     */
    @NotNull
    public List<String> getLoadAfter() {
        return loadAfter;
    }
    
    /**
     * Gets traditional hard dependencies for compatibility.
     */
    @NotNull
    public List<String> getDepend() {
        return dependencies;
    }
    
    /**
     * Gets traditional soft dependencies for compatibility.
     */
    @NotNull
    public List<String> getSoftDepend() {
        return softDependencies;
    }
    
    @Nullable
    public String getAPIVersion() {
        return apiVersion;
    }
    
    /**
     * Returns whether this plugin has an open classloader.
     * Open classloaders allow other plugins to access this plugin's classes.
     */
    public boolean hasOpenClassloader() {
        return hasOpenClassloader;
    }
    
    /**
     * Gets extra configuration values not handled by standard fields.
     */
    @NotNull
    public Map<String, Object> getExtra() {
        return Collections.unmodifiableMap(extra);
    }
    
    /**
     * Exception thrown when a paper-plugin.yml file is invalid.
     */
    public static class InvalidDescriptionException extends Exception {
        public InvalidDescriptionException(String message) {
            super(message);
        }
        
        public InvalidDescriptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
