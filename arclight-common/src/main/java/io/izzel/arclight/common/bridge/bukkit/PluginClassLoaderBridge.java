package io.izzel.arclight.common.bridge.bukkit;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;

public interface PluginClassLoaderBridge {
  PluginDescriptionFile arclight$desc();

  Class<?> arclight$loadFromExternal(String name, boolean initialize, boolean checkLibraries)
      throws ClassNotFoundException;

  SimplePluginManager arclight$getPluginManager();

  Logger arclight$systemLogger();
}
