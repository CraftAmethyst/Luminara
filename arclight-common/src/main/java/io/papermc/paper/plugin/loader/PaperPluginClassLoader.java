package io.papermc.paper.plugin.loader;

import io.papermc.paper.plugin.configuration.PaperPluginDescriptionFile;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Paper plugin class loader with enhanced isolation and dependency management.
 * This class loader provides better isolation between plugins while still allowing
 * controlled access when needed.
 */
public class PaperPluginClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final PaperPluginLoader loader;
    private final PaperPluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final JavaPlugin plugin;
    private JavaPlugin pluginInit;

    PaperPluginClassLoader(@NotNull PaperPluginLoader loader, @NotNull ClassLoader parent,
                           @NotNull PaperPluginDescriptionFile description, @NotNull File dataFolder,
                           @NotNull File file) throws IOException, InvalidPluginException {
        super(new URL[]{file.toURI().toURL()}, parent);

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
            }

            Class<? extends JavaPlugin> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
            }

            plugin = pluginClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidPluginException("No public constructor", ex);
        } catch (Exception ex) {
            throw new InvalidPluginException("Abnormal plugin type", ex);
        }
    }

    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @NotNull
    Class<?> findClass(@NotNull String name, boolean checkGlobal) throws ClassNotFoundException {
        if (name.startsWith("org.bukkit.") || name.startsWith("net.minecraft.")) {
            throw new ClassNotFoundException(name);
        }

        Class<?> result = classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                result = loader.getClassByName(name);
            }

            if (result == null) {
                result = findClassInJar(name);
                if (result != null) {
                    loader.setClass(name, result);
                }
            }

            if (result == null) {
                throw new ClassNotFoundException(name);
            }

            classes.put(name, result);
        }

        return result;
    }

    @Nullable
    private Class<?> findClassInJar(@NotNull String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = jar.getJarEntry(path);

        if (entry != null) {
            byte[] classBytes;
            try (InputStream is = jar.getInputStream(entry)) {
                classBytes = readAllBytes(is);
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }

            int dot = name.lastIndexOf('.');
            if (dot != -1) {
                String pkgName = name.substring(0, dot);
                if (getPackage(pkgName) == null) {
                    try {
                        if (manifest != null) {
                            definePackage(pkgName, manifest, url);
                        } else {
                            definePackage(pkgName, null, null, null, null, null, null, null);
                        }
                    } catch (IllegalArgumentException ex) {
                        if (getPackage(pkgName) == null) {
                            throw new IllegalStateException("Cannot find package " + pkgName);
                        }
                    }
                }
            }

            CodeSigner[] signers = entry.getCodeSigners();
            CodeSource source = new CodeSource(url, signers);

            return defineClass(name, classBytes, 0, classBytes.length, source);
        }

        return null;
    }

    private byte[] readAllBytes(@NotNull InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        while ((bytesRead = is.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        return output.toByteArray();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public PaperPluginDescriptionFile getDescription() {
        return description;
    }

    @NotNull
    public File getDataFolder() {
        return dataFolder;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    /**
     * Gets the plugin loader that created this class loader.
     */
    @NotNull
    public PaperPluginLoader getPluginLoader() {
        return loader;
    }

    /**
     * Checks if this plugin has an open classloader that allows other plugins to access its classes.
     */
    public boolean hasOpenClassloader() {
        return description.hasOpenClassloader();
    }

    /**
     * Attempts to load a class from this plugin's classloader.
     * This respects the classloader isolation settings.
     */
    @Nullable
    public Class<?> findPluginClass(@NotNull String name) throws ClassNotFoundException {
        if (!hasOpenClassloader()) {
            // Check if the requesting class is from the same plugin
            Class<?>[] stack = getClassContext();
            boolean samePlugin = false;

            for (Class<?> clazz : stack) {
                if (clazz.getClassLoader() == this) {
                    samePlugin = true;
                    break;
                }
            }

            if (!samePlugin) {
                String caller = stack.length > 1 ? stack[1].getName() : "unknown";
                if (seenIllegalAccess.add(name + ":" + caller)) {
                    plugin.getLogger().warning("Plugin " + caller + " attempted to access class " + name +
                            " from plugin " + description.getName() + " which does not have an open classloader");
                }
                return null;
            }
        }

        return findClass(name, false);
    }

    /**
     * Gets the class context for security checks.
     */
    private Class<?>[] getClassContext() {
        return new SecurityManager() {
            @Override
            public Class<?>[] getClassContext() {
                return super.getClassContext();
            }
        }.getClassContext();
    }
}
