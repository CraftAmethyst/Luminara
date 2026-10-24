package io.izzel.arclight.common.mixin.bukkit.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;
import io.izzel.arclight.common.bridge.bukkit.JavaPluginLoaderBridge;
import io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge;
import io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor;
import io.izzel.arclight.common.mod.mixins.annotation.ShadowConstructor;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapConfig;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapper;
import io.izzel.arclight.common.mod.util.remapper.ClassLoaderRemapper;
import io.izzel.arclight.common.mod.util.remapper.RemappingClassLoader;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.tools.product.Product2;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader
    implements RemappingClassLoader, PluginClassLoaderBridge {

  // @formatter:off
  @Mutable @Shadow @Final private Map<String, Class<?>> classes;
  @Mutable @Shadow @Final private JavaPluginLoader loader;
  @Mutable @Shadow @Final private PluginDescriptionFile description;
  @Mutable @Shadow @Final private Manifest manifest;
  @Mutable @Shadow @Final private URL url;
  @Mutable @Shadow @Final private File dataFolder;
  @Mutable @Shadow @Final private File file;
  @Mutable @Shadow @Final private JarFile jar;
  @Mutable @Shadow @Final private ClassLoader libraryLoader;
  @Mutable @Shadow @Final private JavaPlugin plugin;
  @Mutable @Shadow @Final private Set<String> seenIllegalAccess;

  // @formatter:on

  @Shadow
  abstract void initialize(@NotNull JavaPlugin javaPlugin);

  private ClassLoaderRemapper remapper;

  @ShadowConstructor.Super
  public void arclight$constructor$super(String name, URL[] urls, ClassLoader parent) {
    throw new AbstractMethodError();
  }

  @CreateConstructor
  public void arclight$constructor(
      String name,
      @NotNull JavaPluginLoader loader,
      @Nullable ClassLoader parent,
      @NotNull PluginDescriptionFile description,
      @NotNull File dataFolder,
      @NotNull File file,
      @Nullable ClassLoader libraryLoader)
      throws IOException, InvalidPluginException {
    arclight$constructor$super(name, new URL[] {file.toURI().toURL()}, parent);
    Preconditions.checkArgument(loader != null, "Loader cannot be null");
    this.classes = new ConcurrentHashMap<>();
    this.seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.loader = loader;
    this.description = description;
    this.dataFolder = dataFolder;
    this.file = file;
    this.jar = new JarFile(file);
    this.manifest = this.jar.getManifest();
    this.url = file.toURI().toURL();
    this.libraryLoader = libraryLoader;

    Class<?> jarClass;
    try {
      jarClass = Class.forName(description.getMain(), true, this);
    } catch (ClassNotFoundException ex) {
      throw new InvalidPluginException(
          "Cannot find main class `" + description.getMain() + "'", ex);
    }

    Class<? extends JavaPlugin> pluginClass;
    try {
      pluginClass = jarClass.asSubclass(JavaPlugin.class);
    } catch (ClassCastException ex) {
      throw new InvalidPluginException(
          "main class `" + description.getMain() + "' must extend JavaPlugin", ex);
    }

    Constructor<? extends JavaPlugin> pluginConstructor;
    try {
      pluginConstructor = pluginClass.getDeclaredConstructor();
    } catch (NoSuchMethodException ex) {
      throw new InvalidPluginException(
          "main class `" + description.getMain() + "' must have a public no-args constructor", ex);
    }

    try {
      this.plugin = pluginConstructor.newInstance();
    } catch (IllegalAccessException ex) {
      throw new InvalidPluginException(
          "main class `" + description.getMain() + "' constructor must be public", ex);
    } catch (InstantiationException ex) {
      throw new InvalidPluginException(
          "main class `" + description.getMain() + "' must not be abstract", ex);
    } catch (IllegalArgumentException ex) {
      throw new InvalidPluginException(
          "Could not invoke main class `" + description.getMain() + "' constructor", ex);
    } catch (InvocationTargetException | ExceptionInInitializerError ex) {
      throw new InvalidPluginException(
          "Exception initializing main class `" + description.getMain() + "'", ex);
    }
  }

  @Override
  public ClassLoaderRemapper getRemapper() {
    if (remapper == null) {
      remapper = ArclightRemapper.createClassLoaderRemapper(this);
    }
    return remapper;
  }

  @Override
  public ArclightRemapConfig getRemapConfig() {
    return ArclightRemapConfig.PLUGIN;
  }

  @Override
  public PluginDescriptionFile arclight$desc() {
    return description;
  }

  @Override
  public Class<?> arclight$loadFromExternal(String name, boolean initialize, boolean checkLibraries)
      throws ClassNotFoundException {
    return loadClass0(name, initialize, false, checkLibraries);
  }

  @Override
  public SimplePluginManager arclight$getPluginManager() {
    return (SimplePluginManager)
        ((JavaPluginLoaderBridge) (Object) loader).arclight$server().getPluginManager();
  }

  @Override
  public Logger arclight$systemLogger() {
    return ((JavaPluginLoaderBridge) (Object) loader).arclight$server().getLogger();
  }

  public PluginClassLoaderMixin(URL[] urls) {
    super(urls);
  }

  /**
   * @author InitAuther97
   * @reason add skip super for Adventure
   */
  @Overwrite
  Class<?> loadClass0(String name, boolean resolve, boolean checkGlobal, boolean checkLibraries)
      throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c != null) {
        if (resolve) resolveClass(c);
        return c;
      }
      if (!ArclightConfig.spec().getCompat().isAdventureIsolatedFromML()
          || !name.startsWith("net.kyori.adventure.")) {
        try {
          c = getParent().loadClass(name);
          if (resolve) resolveClass(c);
          return c;
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found
          // from the non-null parent class loader
        }
      }

      if (c == null) {
        // If still not found, then invoke findClass in order
        // to find the class.
        try {
          c = findClass(name);
          if (resolve) resolveClass(c);
          return c;
        } catch (ClassNotFoundException e) {
          // There's no such class
        }
      }
    }

    if (checkLibraries && this.libraryLoader != null) {
      try {
        return this.libraryLoader.loadClass(name);
      } catch (ClassNotFoundException var7) {
      }
    }

    if (checkGlobal) {
      Class<?> result =
          ((JavaPluginLoaderBridge) (Object) loader)
              .arclight$getClassByName(name, resolve, this.description);
      if (result != null) {
        if (result.getClassLoader() instanceof PluginClassLoaderBridge cl) {
          PluginDescriptionFile provider = cl.arclight$desc();
          if (provider != this.description
              && !this.seenIllegalAccess.contains(provider.getName())
              && !cl.arclight$getPluginManager().isTransitiveDepend(this.description, provider)) {
            this.seenIllegalAccess.add(provider.getName());
            if (this.plugin != null) {
              this.plugin
                  .getLogger()
                  .log(
                      Level.WARNING,
                      "Loaded class {0} from {1} which is not a depend or softdepend of this"
                          + " plugin.",
                      new Object[] {name, provider.getFullName()});
            } else {
              arclight$systemLogger()
                  .log(
                      Level.WARNING,
                      "[{0}] Loaded class {1} from {2} which is not a depend or softdepend of this"
                          + " plugin.",
                      new Object[] {this.description.getName(), name, provider.getFullName()});
            }
          }
        }

        return result;
      }
    }

    throw new ClassNotFoundException(
        String.format("Plugin %s cannot load class %s", description.getName(), name));
  }

  /**
   * @author IzzelAliz
   * @reason
   */
  @Overwrite
  @Override
  public URL getResource(String name) {
    Objects.requireNonNull(name);
    URL url = findResource(name);
    if (url == null) {
      if (getParent() != null) {
        url = getParent().getResource(name);
      }
    }
    return url;
  }

  /**
   * @author IzzelAliz
   * @reason
   */
  @Overwrite
  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    Objects.requireNonNull(name);
    @SuppressWarnings("unchecked")
    Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
    if (getParent() != null) {
      tmp[1] = getParent().getResources(name);
    }
    tmp[0] = findResources(name);
    return Iterators.asEnumeration(
        Iterators.concat(Iterators.forEnumeration(tmp[0]), Iterators.forEnumeration(tmp[1])));
  }

  /**
   * @author IzzelAliz
   * @reason
   */
  @Overwrite
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (name.startsWith("org.bukkit.") || name.startsWith("net.minecraft.")) {
      throw new ClassNotFoundException(name);
    }
    Class<?> result = classes.get(name);

    if (result == null) {
      String path = name.replace('.', '/').concat(".class");
      URL url = this.findResource(path);

      if (url != null) {

        URLConnection connection;
        Callable<byte[]> byteSource;
        try {
          connection = url.openConnection();
          connection.connect();
          byteSource =
              () -> {
                try (InputStream is = connection.getInputStream()) {
                  byte[] classBytes = ByteStreams.toByteArray(is);
                  classBytes = ArclightRemapper.SWITCH_TABLE_FIXER.apply(classBytes);
                  classBytes = Bukkit.getUnsafe().processClass(description, path, classBytes);
                  return classBytes;
                }
              };
        } catch (IOException e) {
          throw new ClassNotFoundException(name, e);
        }

        Product2<byte[], CodeSource> classBytes =
            this.getRemapper().remapClass(name, byteSource, connection, ArclightRemapConfig.PLUGIN);

        int dot = name.lastIndexOf('.');
        if (dot != -1) {
          String pkgName = name.substring(0, dot);
          if (getPackage(pkgName) == null) {
            try {
              if (manifest != null) {
                definePackage(pkgName, manifest, this.url);
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

        result = defineClass(name, classBytes._1, 0, classBytes._1.length, classBytes._2);
      }

      if (result == null) {
        result = super.findClass(name);
      }

      ((JavaPluginLoaderBridge) (Object) loader).bridge$setClass(name, result);
      classes.put(name, result);
    }

    return result;
  }
}
