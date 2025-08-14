package io.papermc.paper.plugin.manager;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import io.papermc.paper.plugin.loader.PaperPluginLoader;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced plugin manager that supports both traditional Bukkit plugins and Paper plugins.
 * This manager handles the loading order, dependency resolution, and lifecycle management
 * for both plugin types.
 */
public class PaperPluginManagerImpl implements PluginManager {

    private final Server server;
    private final SimpleCommandMap commandMap;
    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
    private final Map<Pattern, PluginLoader> fileAssociations = new HashMap<>();
    private final List<PluginLoader> pluginLoaders = new ArrayList<>();
    private final Map<String, Permission> permissions = new ConcurrentHashMap<>();
    private final Map<Boolean, Set<Permission>> defaultPerms = new HashMap<>();
    private final Map<String, Map<Permissible, Boolean>> permSubs = new HashMap<>();
    private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new HashMap<>();
    private final LoadOrderTree loadOrderTree = new LoadOrderTree();
    private final Logger logger;

    public PaperPluginManagerImpl(@NotNull Server instance, @NotNull SimpleCommandMap commandMap) {
        this.server = instance;
        this.commandMap = commandMap;
        this.logger = Logger.getLogger("PaperPluginManager");

        // Initialize default permission sets
        defaultPerms.put(true, new HashSet<>());
        defaultPerms.put(false, new HashSet<>());
        defSubs.put(true, new HashMap<>());
        defSubs.put(false, new HashMap<>());

        // Register plugin loaders
        pluginLoaders.add(new JavaPluginLoader(server));
        pluginLoaders.add(new PaperPluginLoader(server));

        // Set up file associations
        Pattern[] patterns;
        for (PluginLoader loader : pluginLoaders) {
            patterns = loader.getPluginFileFilters();
            for (Pattern pattern : patterns) {
                fileAssociations.put(pattern, loader);
            }
        }
    }

    @Override
    @Nullable
    public Plugin getPlugin(@NotNull String name) {
        return plugins.get(name.replace(' ', '_'));
    }

    @Override
    @NotNull
    public Plugin[] getPlugins() {
        return plugins.values().toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(@NotNull String name) {
        Plugin plugin = getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean isPluginEnabled(@Nullable Plugin plugin) {
        return plugin != null && plugins.containsValue(plugin) && plugin.isEnabled();
    }

    @Override
    @Nullable
    public Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, UnknownDependencyException {
        if (!file.exists()) {
            return null;
        }

        PluginLoader loader = null;
        for (Pattern filter : fileAssociations.keySet()) {
            Matcher match = filter.matcher(file.getName());
            if (match.find()) {
                loader = fileAssociations.get(filter);
                break;
            }
        }

        if (loader == null) {
            throw new UnknownDependencyException("Unknown plugin type for file: " + file.getName());
        }

        return loadPlugin(file, loader);
    }

    private Plugin loadPlugin(@NotNull File file, @NotNull PluginLoader loader) throws InvalidPluginException {
        Plugin result = loader.loadPlugin(file);

        if (result != null) {
            plugins.put(result.getDescription().getName(), result);

            // If this is a Paper plugin, add it to the load order tree
            if (loader instanceof PaperPluginLoader) {
                try {
                    PaperPluginDescriptionFile desc = ((PaperPluginLoader) loader).getPaperPluginDescription(file);
                    loadOrderTree.addPlugin(desc);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to add plugin to load order tree: " + result.getDescription().getName(), e);
                }
            }
        }

        return result;
    }

    @Override
    @NotNull
    public Plugin[] loadPlugins(@NotNull File directory) {
        if (!directory.isDirectory()) {
            return new Plugin[0];
        }

        List<Plugin> result = new ArrayList<>();
        Set<Pattern> filters = new HashSet<>();

        for (PluginLoader loader : pluginLoaders) {
            filters.addAll(Arrays.asList(loader.getPluginFileFilters()));
        }

        Map<String, File> plugins = new HashMap<>();
        Set<String> loadedPlugins = new HashSet<>();
        Map<String, Collection<String>> dependencies = new HashMap<>();
        Map<String, Collection<String>> softDependencies = new HashMap<>();

        // Discover all plugin files
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isFile()) continue;

                boolean matched = false;
                for (Pattern filter : filters) {
                    if (filter.matcher(file.getName()).find()) {
                        matched = true;
                        break;
                    }
                }

                if (!matched) continue;

                PluginDescriptionFile description;
                try {
                    PluginLoader loader = getPluginLoader(file);
                    if (loader == null) continue;

                    description = loader.getPluginDescription(file);
                    String name = description.getName();

                    if (name.equalsIgnoreCase("bukkit") || name.equalsIgnoreCase("minecraft") || name.equalsIgnoreCase("mojang")) {
                        logger.log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': Restricted Name");
                        continue;
                    } else if (description.getName().indexOf(' ') != -1) {
                        logger.log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': uses the space-character (0x20) in its name");
                        continue;
                    }
                } catch (InvalidDescriptionException ex) {
                    logger.log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex);
                    continue;
                }

                plugins.put(description.getName(), file);
                dependencies.put(description.getName(), description.getDepend());
                softDependencies.put(description.getName(), description.getSoftDepend());
            }
        }

        // Build load order tree for Paper plugins
        try {
            loadOrderTree.build();
            List<String> loadOrder = loadOrderTree.getLoadOrder();

            // Load plugins in the correct order
            for (String pluginName : loadOrder) {
                if (loadedPlugins.contains(pluginName)) continue;

                File file = plugins.get(pluginName);
                if (file == null) continue;

                try {
                    Plugin plugin = loadPlugin(file);
                    if (plugin != null) {
                        result.add(plugin);
                        loadedPlugins.add(pluginName);
                        loadOrderTree.markLoaded(pluginName);
                    }
                } catch (InvalidPluginException ex) {
                    logger.log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex);
                }
            }
        } catch (LoadOrderTree.CircularDependencyException ex) {
            logger.log(Level.SEVERE, "Circular dependency detected in plugin loading", ex);
        }

        // Load remaining plugins (traditional Bukkit plugins)
        for (Map.Entry<String, File> entry : plugins.entrySet()) {
            if (loadedPlugins.contains(entry.getKey())) continue;

            try {
                Plugin plugin = loadPlugin(entry.getValue());
                if (plugin != null) {
                    result.add(plugin);
                    loadedPlugins.add(entry.getKey());
                }
            } catch (InvalidPluginException ex) {
                logger.log(Level.SEVERE, "Could not load '" + entry.getValue().getPath() + "' in folder '" + directory.getPath() + "'", ex);
            }
        }

        return result.toArray(new Plugin[0]);
    }

    @Nullable
    private PluginLoader getPluginLoader(@NotNull File file) {
        for (Pattern filter : fileAssociations.keySet()) {
            if (filter.matcher(file.getName()).find()) {
                return fileAssociations.get(filter);
            }
        }
        return null;
    }

    @Override
    public void disablePlugins() {
        Plugin[] plugins = getPlugins();
        for (int i = plugins.length - 1; i >= 0; i--) {
            disablePlugin(plugins[i]);
        }
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        if (plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().disablePlugin(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getScheduler().cancelTasks(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getServicesManager().unregisterAll(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                HandlerList.unregisterAll(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getMessenger().unregisterIncomingPluginChannel(plugin);
                server.getMessenger().unregisterOutgoingPluginChannel(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        if (!plugin.isEnabled()) {
            List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);

            if (!pluginCommands.isEmpty()) {
                commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);
            }

            try {
                plugin.getPluginLoader().enablePlugin(plugin);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
    }

    @Override
    public void clearPlugins() {
        synchronized (this) {
            disablePlugins();
            plugins.clear();
            fileAssociations.clear();
            permissions.clear();
            defaultPerms.get(true).clear();
            defaultPerms.get(false).clear();
        }
    }

    @Override
    public void callEvent(@NotNull Event event) throws IllegalStateException {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);

                    logger.log(Level.SEVERE, String.format(
                            "Nag author(s): '%s' of '%s' about the following: %s",
                            plugin.getDescription().getAuthors(),
                            plugin.getDescription().getFullName(),
                            ex.getMessage()
                    ));
                }
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
            }
        }
    }

    @Override
    public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener, @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener, @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin, boolean ignoreCancelled) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }

        getEventListeners(event).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
    }

    private HandlerList getEventListeners(@NotNull Class<? extends Event> type) {
        try {
            java.lang.reflect.Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private Class<? extends Event> getRegistrationClass(@NotNull Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    // Permission management methods
    @Override
    public void addPermission(@NotNull Permission perm) {
        addPermission(perm, true);
    }

    public void addPermission(@NotNull Permission perm, boolean dirty) {
        String name = perm.getName().toLowerCase(java.util.Locale.ENGLISH);

        if (permissions.containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }

        permissions.put(name, perm);
        calculatePermissionDefault(perm, dirty);
    }

    @Override
    @NotNull
    public Set<Permission> getDefaultPermissions(boolean op) {
        return Collections.unmodifiableSet(defaultPerms.get(op));
    }

    @Override
    public void removePermission(@NotNull Permission perm) {
        removePermission(perm.getName());
    }

    @Override
    public void removePermission(@NotNull String name) {
        permissions.remove(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission perm) {
        if (permissions.containsValue(perm)) {
            defaultPerms.get(true).remove(perm);
            defaultPerms.get(false).remove(perm);
            calculatePermissionDefault(perm, true);
        }
    }

    private void calculatePermissionDefault(@NotNull Permission perm, boolean dirty) {
        if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(true).add(perm);
            if (dirty) {
                dirtyPermissibles(true);
            }
        }
        if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(false).add(perm);
            if (dirty) {
                dirtyPermissibles(false);
            }
        }
    }

    private void dirtyPermissibles(boolean op) {
        Set<Permissible> permissibles = defSubs.get(op).keySet();

        for (Permissible p : permissibles) {
            p.recalculatePermissions();
        }
    }

    @Override
    @Nullable
    public Permission getPermission(@NotNull String name) {
        return permissions.get(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @Override
    @NotNull
    public Set<Permission> getPermissions() {
        return new HashSet<>(permissions.values());
    }

    @Override
    public boolean useTimings() {
        return false; // Timings v2 is deprecated, always return false
    }

    // Subscription management methods
    @Override
    public void subscribeToPermission(@NotNull String permission, @NotNull Permissible permissible) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = permSubs.computeIfAbsent(name, k -> new HashMap<>());
        map.put(permissible, true);
    }

    @Override
    public void unsubscribeFromPermission(@NotNull String permission, @NotNull Permissible permissible) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = permSubs.get(name);
        if (map != null) {
            map.remove(permissible);
            if (map.isEmpty()) {
                permSubs.remove(name);
            }
        }
    }

    @Override
    @NotNull
    public Set<Permissible> getPermissionSubscriptions(@NotNull String permission) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = permSubs.get(name);
        if (map == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(map.keySet());
        }
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);
        map.put(permissible, true);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);
        map.remove(permissible);
    }

    @Override
    @NotNull
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> map = defSubs.get(op);
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Gets the Paper-specific load order tree.
     */
    @NotNull
    public LoadOrderTree getLoadOrderTree() {
        return loadOrderTree;
    }

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> loader) throws IllegalArgumentException {
        // Implementation for registering plugin loader interfaces
        // This is required by the PluginManager interface
    }
}
