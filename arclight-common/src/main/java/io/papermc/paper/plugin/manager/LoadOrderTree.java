package io.papermc.paper.plugin.manager;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages plugin loading order and dependency resolution for Paper plugins.
 * This class handles the enhanced dependency system with loadbefore/loadafter
 * and detects circular dependencies.
 */
public class LoadOrderTree {

    private static final Logger LOGGER = Logger.getLogger("LoadOrderTree");

    private final Map<String, PluginNode> nodes = new HashMap<>();
    private final Set<String> loadedPlugins = new HashSet<>();
    private boolean built = false;

    /**
     * Adds a plugin to the dependency tree.
     */
    public void addPlugin(@NotNull PaperPluginDescriptionFile description) {
        if (built) {
            throw new IllegalStateException("Cannot add plugins after tree is built");
        }

        String name = description.getName();
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Plugin " + name + " is already registered");
        }

        nodes.put(name, new PluginNode(name, description));
    }

    /**
     * Builds the dependency tree and resolves all relationships.
     */
    public void build() throws CircularDependencyException {
        if (built) {
            return;
        }

        // Build dependency relationships
        for (PluginNode node : nodes.values()) {
            buildDependencies(node);
        }

        // Check for circular dependencies
        checkCircularDependencies();

        built = true;
    }

    private void buildDependencies(@NotNull PluginNode node) {
        // Handle traditional dependencies
        for (String depName : node.dependencies) {
            PluginNode depNode = nodes.get(depName);
            if (depNode != null) {
                depNode.dependents.add(node);
            }
        }

        for (String depName : node.softDependencies) {
            PluginNode depNode = nodes.get(depName);
            if (depNode != null) {
                depNode.softDependents.add(node);
            }
        }

        // Handle Paper's enhanced dependency system
        for (String beforeName : node.loadBefore) {
            PluginNode beforeNode = nodes.get(beforeName);
            if (beforeNode != null) {
                // This plugin should load before beforeNode
                node.dependents.add(beforeNode);
            }
        }

        for (String afterName : node.loadAfter) {
            PluginNode afterNode = nodes.get(afterName);
            if (afterNode != null) {
                // This plugin should load after afterNode
                afterNode.dependents.add(node);
            }
        }
    }

    /**
     * Gets the loading order for all plugins.
     */
    @NotNull
    public List<String> getLoadOrder() throws CircularDependencyException {
        if (!built) {
            build();
        }

        List<String> loadOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();

        for (PluginNode node : nodes.values()) {
            if (!visited.contains(node.name)) {
                topologicalSort(node, visited, inStack, loadOrder);
            }
        }

        Collections.reverse(loadOrder);
        return loadOrder;
    }

    private void topologicalSort(@NotNull PluginNode node, @NotNull Set<String> visited,
                                 @NotNull Set<String> inStack, @NotNull List<String> result)
            throws CircularDependencyException {
        if (inStack.contains(node.name)) {
            throw new CircularDependencyException("Circular dependency detected involving " + node.name);
        }

        if (visited.contains(node.name)) {
            return;
        }

        inStack.add(node.name);

        // Visit all dependents first
        for (PluginNode dependent : node.dependents) {
            topologicalSort(dependent, visited, inStack, result);
        }

        for (PluginNode dependent : node.softDependents) {
            if (nodes.containsKey(dependent.name)) {
                topologicalSort(dependent, visited, inStack, result);
            }
        }

        inStack.remove(node.name);
        visited.add(node.name);
        result.add(node.name);
    }

    /**
     * Checks for circular dependencies in the plugin tree.
     */
    private void checkCircularDependencies() throws CircularDependencyException {
        for (PluginNode node : nodes.values()) {
            node.visited = false;
            node.inStack = false;
        }

        for (PluginNode node : nodes.values()) {
            if (!node.visited) {
                checkCircularDependenciesRecursive(node, new ArrayList<>());
            }
        }
    }

    private void checkCircularDependenciesRecursive(@NotNull PluginNode node, @NotNull List<String> path)
            throws CircularDependencyException {
        if (node.inStack) {
            // Found a cycle
            int cycleStart = path.indexOf(node.name);
            List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
            cycle.add(node.name);

            StringBuilder error = new StringBuilder();
            error.append("Circular plugin loading detected:\n");

            for (int i = 0; i < cycle.size() - 1; i++) {
                String current = cycle.get(i);
                String next = cycle.get(i + 1);
                error.append(String.format("%d) %s -> %s\n", i + 1, current, next));

                PluginNode currentNode = nodes.get(current);
                if (currentNode != null) {
                    if (currentNode.loadBefore.contains(next)) {
                        error.append(String.format("   %s loadbefore: [%s]\n", current, next));
                    }
                    if (currentNode.loadAfter.contains(next)) {
                        error.append(String.format("   %s loadafter: [%s]\n", current, next));
                    }
                    if (currentNode.dependencies.contains(next)) {
                        error.append(String.format("   %s depend: [%s]\n", current, next));
                    }
                }
            }

            error.append("Please report this to the plugin authors of the first plugin of each loop or join the PaperMC Discord server for further help.");

            throw new CircularDependencyException(error.toString());
        }

        if (node.visited) {
            return;
        }

        node.inStack = true;
        path.add(node.name);

        // Check all dependencies
        for (PluginNode dependent : node.dependents) {
            checkCircularDependenciesRecursive(dependent, path);
        }

        path.remove(path.size() - 1);
        node.inStack = false;
        node.visited = true;
    }

    /**
     * Checks if a plugin can be loaded based on its dependencies.
     */
    public boolean canLoad(@NotNull String pluginName) {
        PluginNode node = nodes.get(pluginName);
        if (node == null) {
            return false;
        }

        // Check hard dependencies
        for (String dep : node.dependencies) {
            if (!loadedPlugins.contains(dep)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Marks a plugin as loaded.
     */
    public void markLoaded(@NotNull String pluginName) {
        loadedPlugins.add(pluginName);
    }

    /**
     * Gets missing dependencies for a plugin.
     */
    @NotNull
    public Set<String> getMissingDependencies(@NotNull String pluginName) {
        PluginNode node = nodes.get(pluginName);
        if (node == null) {
            return Collections.emptySet();
        }

        Set<String> missing = new HashSet<>();
        for (String dep : node.dependencies) {
            if (!nodes.containsKey(dep)) {
                missing.add(dep);
            }
        }

        return missing;
    }

    /**
     * Gets the plugin description for a given plugin name.
     */
    @Nullable
    public PaperPluginDescriptionFile getPluginDescription(@NotNull String pluginName) {
        PluginNode node = nodes.get(pluginName);
        return node != null ? node.description : null;
    }

    /**
     * Represents a plugin node in the dependency tree.
     */
    private static class PluginNode {
        final String name;
        final PaperPluginDescriptionFile description;
        final Set<String> dependencies = new HashSet<>();
        final Set<String> softDependencies = new HashSet<>();
        final Set<String> loadBefore = new HashSet<>();
        final Set<String> loadAfter = new HashSet<>();
        final Set<PluginNode> dependents = new HashSet<>();
        final Set<PluginNode> softDependents = new HashSet<>();

        boolean visited = false;
        boolean inStack = false;

        PluginNode(@NotNull String name, @NotNull PaperPluginDescriptionFile description) {
            this.name = name;
            this.description = description;
            this.dependencies.addAll(description.getDepend());
            this.softDependencies.addAll(description.getSoftDepend());
            this.loadBefore.addAll(description.getLoadBefore());
            this.loadAfter.addAll(description.getLoadAfter());
        }
    }

    /**
     * Exception thrown when circular dependencies are detected.
     */
    public static class CircularDependencyException extends Exception {
        public CircularDependencyException(@NotNull String message) {
            super(message);
        }
    }
}
