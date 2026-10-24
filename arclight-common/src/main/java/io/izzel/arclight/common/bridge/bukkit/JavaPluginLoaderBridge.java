package io.izzel.arclight.common.bridge.bukkit;

import java.net.URLClassLoader;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;

public interface JavaPluginLoaderBridge {

  Server arclight$server();

  <T extends URLClassLoader & PluginClassLoaderBridge> List<T> arclight$getLoaders();

  void bridge$setClass(final String name, final Class<?> clazz);

  Class<?> arclight$getClassByName(String name, boolean resolve, PluginDescriptionFile description);
}
