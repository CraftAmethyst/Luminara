package io.izzel.arclight.common.mod.util.remapper.generated;

import com.google.common.io.ByteStreams;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapConfig;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapper;
import io.izzel.arclight.common.mod.util.remapper.ClassLoaderRemapper;
import io.izzel.arclight.common.mod.util.remapper.RemappingClassLoader;
import io.izzel.tools.product.Product2;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.CodeSource;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;

public class RemappingURLClassLoader
  extends URLClassLoader
  implements RemappingClassLoader
{

  static {
    ClassLoader.registerAsParallelCapable();
  }

  // Sample using remap config
  public ArclightRemapConfig config = new ArclightRemapConfig(
    RemappingClassLoader.needRemap(this)
  );
  private ClassLoaderRemapper remapper;

  public RemappingURLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, RemappingClassLoader.tryRedirect(parent));
  }

  public RemappingURLClassLoader(URL[] urls) {
    super(urls, RemappingClassLoader.tryRedirect(null));
  }

  public RemappingURLClassLoader(
    URL[] urls,
    ClassLoader parent,
    URLStreamHandlerFactory factory
  ) {
    super(urls, RemappingClassLoader.tryRedirect(parent), factory);
  }

  public RemappingURLClassLoader(String name, URL[] urls, ClassLoader parent) {
    super(name, urls, RemappingClassLoader.tryRedirect(parent));
  }

  public RemappingURLClassLoader(
    String name,
    URL[] urls,
    ClassLoader parent,
    URLStreamHandlerFactory factory
  ) {
    super(name, urls, RemappingClassLoader.tryRedirect(parent), factory);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> result = null;
    String path = name.replace('.', '/').concat(".class");
    URL resource = this.findResource(path);
    if (resource != null) {
      URLConnection connection;
      Callable<byte[]> byteSource;
      Manifest manifest;
      try {
        connection = resource.openConnection();
        connection.connect();
        if (
          connection instanceof JarURLConnection &&
          ((JarURLConnection) connection).getManifest() != null
        ) {
          manifest = ((JarURLConnection) connection).getManifest();
        } else {
          manifest = null;
        }
        byteSource = () -> {
          try (InputStream is = connection.getInputStream()) {
            byte[] classBytes = ByteStreams.toByteArray(is);
            classBytes = ArclightRemapper.SWITCH_TABLE_FIXER.apply(classBytes);
            return classBytes;
          }
        };
      } catch (IOException e) {
        throw new ClassNotFoundException(name, e);
      }

      Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(
        name,
        byteSource,
        connection,
        config
      );

      int i = name.lastIndexOf('.');
      if (i != -1) {
        String pkgName = name.substring(0, i);
        if (getPackage(pkgName) == null) {
          if (manifest != null) {
            this.definePackage(
              pkgName,
              manifest,
              ((JarURLConnection) connection).getJarFileURL()
            );
          } else {
            this.definePackage(
              pkgName,
              null,
              null,
              null,
              null,
              null,
              null,
              null
            );
          }
        }
      }
      result = this.defineClass(
        name,
        classBytes._1,
        0,
        classBytes._1.length,
        classBytes._2
      );
    }
    if (result == null) {
      Class<?> fallback = tryFallbackLoad(name);
      if (fallback != null) {
        return fallback;
      }
      throw new ClassNotFoundException(name);
    }
    return result;
  }

  private Class<?> tryFallbackLoad(String name) {
    ClassLoader parent = getParent();
    if (parent != null && parent != this) {
      try {
        return Class.forName(name, false, parent);
      } catch (ClassNotFoundException ignored) {}
    }
    ClassLoader context = Thread.currentThread().getContextClassLoader();
    if (context != null && context != this && context != parent) {
      try {
        return Class.forName(name, false, context);
      } catch (ClassNotFoundException ignored) {}
    }
    ClassLoader arclight = RemappingURLClassLoader.class.getClassLoader();
    if (
      arclight != null &&
      arclight != this &&
      arclight != parent &&
      arclight != context
    ) {
      try {
        return Class.forName(name, false, arclight);
      } catch (ClassNotFoundException ignored) {}
    }
    ClassLoader system = ClassLoader.getSystemClassLoader();
    if (
      system != null &&
      system != this &&
      system != parent &&
      system != context &&
      system != arclight
    ) {
      try {
        return Class.forName(name, false, system);
      } catch (ClassNotFoundException ignored) {}
    }
    return null;
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
    return config;
  }
}
