package io.izzel.arclight.common.mod.util.remapper.generated;

import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.mod.util.remapper.*;
import io.izzel.arclight.common.util.Enumerations;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

@SuppressWarnings("unused")
public class ArclightReflectionHandler extends ClassLoader {

  private static final String PREFIX = "net.minecraft.";
  public static ClassLoaderRemapper remapper;

  public static Method[] redirectGetDeclaredMethods(Class<?> cl) {
    try {
      return cl.getDeclaredMethods();
    } catch (TypeNotPresentException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        remapper.tryDefineClass(e.getCause().getMessage().replace('.', '/'));
        return redirectGetDeclaredMethods(cl);
      } else throw e;
    } catch (NoClassDefFoundError error) {
      remapper.tryDefineClass(error.getMessage());
      return redirectGetDeclaredMethods(cl);
    }
  }

  public static Method[] redirectGetMethods(Class<?> cl) {
    try {
      return cl.getMethods();
    } catch (TypeNotPresentException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        remapper.tryDefineClass(e.getCause().getMessage().replace('.', '/'));
        return redirectGetMethods(cl);
      } else throw e;
    } catch (NoClassDefFoundError error) {
      remapper.tryDefineClass(error.getMessage());
      return redirectGetMethods(cl);
    }
  }

  public static Field[] redirectGetDeclaredFields(Class<?> cl) {
    try {
      return cl.getDeclaredFields();
    } catch (TypeNotPresentException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        remapper.tryDefineClass(e.getCause().getMessage().replace('.', '/'));
        return redirectGetDeclaredFields(cl);
      } else throw e;
    } catch (NoClassDefFoundError error) {
      remapper.tryDefineClass(error.getMessage());
      return redirectGetDeclaredFields(cl);
    }
  }

  public static Field[] redirectGetFields(Class<?> cl) {
    try {
      return cl.getFields();
    } catch (TypeNotPresentException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        remapper.tryDefineClass(e.getCause().getMessage().replace('.', '/'));
        return redirectGetFields(cl);
      } else throw e;
    } catch (NoClassDefFoundError error) {
      remapper.tryDefineClass(error.getMessage());
      return redirectGetFields(cl);
    }
  }

  // srg -> bukkit
  public static String redirectFieldGetName(Field field) {
    return remapper.tryMapFieldToBukkit(
      field.getDeclaringClass(),
      field.getName(),
      field
    );
  }

  // srg -> bukkit
  public static String redirectMethodGetName(Method method) {
    return remapper.tryMapMethodToBukkit(method.getDeclaringClass(), method);
  }

  // srg -> bukkit
  public static String redirectClassGetCanonicalName(Class<?> cl) {
    if (cl.isArray()) {
      String name = redirectClassGetCanonicalName(cl.getComponentType());
      if (name == null) return null;
      return name + "[]";
    }
    if (cl.isLocalClass() || cl.isAnonymousClass()) {
      return null;
    }
    String canonicalName = cl.getCanonicalName();
    if (canonicalName == null) {
      return null;
    }
    Class<?> enclosingClass = cl.getEnclosingClass();
    if (enclosingClass == null) {
      return redirectClassGetName(cl);
    } else {
      String name = redirectClassGetCanonicalName(enclosingClass);
      if (name == null) return null;
      return name + "." + redirectClassGetSimpleName(cl);
    }
  }

  // srg -> bukkit
  public static String redirectClassGetSimpleName(Class<?> cl) {
    String simpleName = cl.getSimpleName();
    if (simpleName.length() == 0) {
      return simpleName; // anon class
    }
    Class<?> enclosingClass = cl.getEnclosingClass();
    if (enclosingClass == null) {
      // simple class / lambdas
      String mapped = redirectClassGetName(cl);
      return mapped.substring(mapped.lastIndexOf('.') + 1);
    } else {
      // nested class
      String outer = redirectClassGetName(enclosingClass);
      String inner = redirectClassGetName(cl);
      return inner.substring(outer.length() + 1);
    }
  }

  // srg -> bukkit
  public static String handleClassGetName(String cl) {
    String mapped = remapper
      .toBukkitRemapper()
      .mapType(cl.replace('.', '/'))
      .replace('/', '.');
    return CraftBukkitVersionRemapper.toVersionedBinaryName(mapped);
  }

  // srg -> bukkit
  public static String redirectClassGetName(Class<?> cl) {
    String internalName = Type.getInternalName(cl);
    Type type = Type.getObjectType(
      remapper.toBukkitRemapper().mapType(internalName)
    );
    return CraftBukkitVersionRemapper.toVersionedBinaryName(
      type.getInternalName().replace('/', '.')
    );
  }

  // srg -> bukkit
  public static String handlePackageGetName(String name) {
    if (name.startsWith(PREFIX)) {
      return PREFIX + "server." + ArclightVersion.current().packageName();
    } else if (name.startsWith("org.bukkit.craftbukkit.")) {
      return CraftBukkitVersionRemapper.toVersionedBinaryName(name);
    } else {
      return name;
    }
  }

  // srg -> bukkit
  public static String redirectPackageGetName(Package pkg) {
    return handlePackageGetName(pkg.getName());
  }

  // srg -> bukkit
  public static String redirectTypeGetName(java.lang.reflect.Type type) {
    if (type instanceof Class<?> cl) {
      if (cl.isArray()) {
        return redirectTypeGetName(cl.getComponentType()) + "[]";
      }
      return redirectClassGetName(cl);
    } else if (type instanceof WildcardType wType) {
      StringBuilder sb;
      java.lang.reflect.Type[] bounds;
      if (wType.getLowerBounds().length == 0) {
        if (
          wType.getUpperBounds().length == 0 ||
          Object.class == wType.getUpperBounds()[0]
        ) {
          return "?";
        }
        bounds = wType.getUpperBounds();
        sb = new StringBuilder("? extends ");
      } else {
        bounds = wType.getLowerBounds();
        sb = new StringBuilder("? super ");
      }
      for (int i = 0; i < bounds.length; i++) {
        if (i > 0) {
          sb.append(" & ");
        }
        sb.append(redirectTypeGetName(bounds[i]));
      }
      return sb.toString();
    } else if (type instanceof ParameterizedType pType) {
      var sb = new StringBuilder();
      if (pType.getOwnerType() != null) {
        sb.append(redirectTypeGetName(pType.getOwnerType()));
        sb.append("$");
        sb.append(redirectClassGetSimpleName((Class<?>) pType.getRawType()));
      } else {
        sb.append(redirectTypeGetName(pType.getRawType()));
      }
      if (pType.getActualTypeArguments() != null) {
        var sj = new StringJoiner(", ", "<", ">");
        sj.setEmptyValue("");
        for (var t : pType.getActualTypeArguments()) {
          sj.add(redirectTypeGetName(t));
        }
        sb.append(sj);
      }
      return sb.toString();
    } else if (type instanceof GenericArrayType arrayType) {
      return redirectTypeGetName(arrayType.getGenericComponentType()) + "[]";
    }
    return type.getTypeName();
  }

  // bukkit -> srg
  public static Class<?> redirectClassForName(String cl)
    throws ClassNotFoundException {
    return redirectClassForName(
      cl,
      true,
      Unsafe.getCallerClass().getClassLoader()
    );
  }

  private static String normalizeReflectionClassName(String className) {
    if (className == null || className.isEmpty()) {
      return className;
    }
    if (className.startsWith("L") && className.endsWith(";")) {
      String elementType = className.substring(1, className.length() - 1);
      return (
        "L" + CraftBukkitVersionRemapper.remapBinaryName(elementType) + ";"
      );
    }
    // Array binary name, e.g. [Lorg.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
    if (className.startsWith("[L") && className.endsWith(";")) {
      String elementType = className.substring(2, className.length() - 1);
      return (
        "[L" + CraftBukkitVersionRemapper.remapBinaryName(elementType) + ";"
      );
    }
    return CraftBukkitVersionRemapper.remapBinaryName(className);
  }

  private static Class<?> tryLoadClass(
    String binaryName,
    boolean initialize,
    ClassLoader preferredLoader
  ) throws ClassNotFoundException {
    Set<ClassLoader> loaders = new LinkedHashSet<>();
    loaders.add(preferredLoader);
    loaders.add(ArclightReflectionHandler.class.getClassLoader());
    loaders.add(Thread.currentThread().getContextClassLoader());
    loaders.add(ClassLoader.getSystemClassLoader());

    Throwable firstFailure = null;
    for (ClassLoader loader : loaders) {
      if (loader == null) {
        continue;
      }
      try {
        return Class.forName(binaryName, initialize, loader);
      } catch (ClassNotFoundException | LinkageError ex) {
        if (firstFailure == null) {
          firstFailure = ex;
        }
      }
    }

    if (firstFailure instanceof ClassNotFoundException cnfe) {
      throw cnfe;
    }
    throw new ClassNotFoundException(binaryName, firstFailure);
  }

  private static boolean shouldMapTypeName(String binaryName) {
    return (
      binaryName.startsWith("net.minecraft.") ||
      binaryName.startsWith("org.bukkit.") ||
      binaryName.startsWith("com.mojang.")
    );
  }

  private static String mapTypeForReflection(String binaryName) {
    if (binaryName == null || binaryName.isEmpty()) {
      return binaryName;
    }
    if (binaryName.startsWith("[L") && binaryName.endsWith(";")) {
      String elementType = binaryName.substring(2, binaryName.length() - 1);
      return "[L" + mapTypeForReflection(elementType) + ";";
    }
    if (binaryName.startsWith("L") && binaryName.endsWith(";")) {
      String elementType = binaryName.substring(1, binaryName.length() - 1);
      return "L" + mapTypeForReflection(elementType) + ";";
    }
    if (!shouldMapTypeName(binaryName)) {
      return binaryName;
    }
    return remapper.mapType(binaryName.replace('.', '/')).replace('/', '.');
  }

  // bukkit -> srg
  public static Class<?> redirectClassForName(
    String cl,
    boolean initialize,
    ClassLoader classLoader
  ) throws ClassNotFoundException {
    String normalizedName = normalizeReflectionClassName(cl);
    String mappedName = mapTypeForReflection(normalizedName);
    try {
      return tryLoadClass(mappedName, initialize, classLoader);
    } catch (ClassNotFoundException e) {
      // nested/inner class
      if (!mappedName.equals(normalizedName)) {
        try {
          return tryLoadClass(normalizedName, initialize, classLoader);
        } catch (ClassNotFoundException ignored) {}
      }
      // For non-NMS/Bukkit plugin classes, '$' can be used incorrectly as a package separator.
      // Try dotted fallback first and skip nested-class probing for these names.
      if (
        normalizedName.indexOf('$') >= 0 && !shouldMapTypeName(normalizedName)
      ) {
        try {
          return tryLoadClass(
            normalizedName.replace('$', '.'),
            initialize,
            classLoader
          );
        } catch (ClassNotFoundException ignored) {
          throw e;
        }
      }
      int i = normalizedName.lastIndexOf('.');
      if (i > 0) {
        String simpleName = normalizedName.substring(i + 1);
        // Keep plugin package-style version classes as dotted names:
        // e.g. com.Zrips.CMI.NBT.v1_20_R1
        if (simpleName.matches("v\\d+_\\d+_R\\d+")) {
          throw e;
        }
        String nestedName =
          normalizedName.substring(0, i).replace('.', '/') +
          "$" +
          normalizedName.substring(i + 1);
        String mappedNestedName = mapTypeForReflection(
          nestedName.replace('/', '.')
        );
        try {
          return tryLoadClass(mappedNestedName, initialize, classLoader);
        } catch (ClassNotFoundException ignored) {
          return tryLoadClass(
            nestedName.replace('/', '.'),
            initialize,
            classLoader
          );
        }
      } else throw e;
    }
  }

  // bukkit -> srg
  public static Object[] handleClassGetField(Class<?> cl, String bukkitName) {
    return new Object[] { cl, remapper.tryMapFieldToSrg(cl, bukkitName) };
  }

  // bukkit -> srg
  public static Field redirectClassGetField(Class<?> cl, String bukkitName)
    throws NoSuchFieldException {
    String field = remapper.tryMapFieldToSrg(cl, bukkitName);
    return cl.getField(field);
  }

  // bukkit -> srg
  public static Object[] handleClassGetDeclaredField(
    Class<?> cl,
    String bukkitName
  ) {
    return handleClassGetField(cl, bukkitName);
  }

  // bukkit -> srg
  public static Field redirectClassGetDeclaredField(
    Class<?> cl,
    String bukkitName
  ) throws NoSuchFieldException {
    String field = remapper.tryMapDecFieldToSrg(cl, bukkitName);
    return cl.getDeclaredField(field);
  }

  // bukkit -> srg
  public static Object[] handleClassGetMethod(
    Class<?> cl,
    String bukkitName,
    Class<?>... pTypes
  ) {
    Method method = remapper.tryMapMethodToSrg(cl, bukkitName, pTypes);
    return new Object[] {
      cl,
      method == null ? bukkitName : method.getName(),
      pTypes,
    };
  }

  // bukkit -> srg
  public static Method redirectClassGetMethod(
    Class<?> cl,
    String bukkitName,
    Class<?>... pTypes
  ) throws NoSuchMethodException {
    Method method = remapper.tryMapMethodToSrg(cl, bukkitName, pTypes);
    if (method != null) {
      return method;
    } else {
      return cl.getMethod(bukkitName, pTypes);
    }
  }

  // bukkit -> srg
  public static Object[] handleClassGetDeclaredMethod(
    Class<?> cl,
    String bukkitName,
    Class<?>... pTypes
  ) {
    return handleClassGetMethod(cl, bukkitName, pTypes);
  }

  // bukkit -> srg
  public static Method redirectClassGetDeclaredMethod(
    Class<?> cl,
    String bukkitName,
    Class<?>... pTypes
  ) throws NoSuchMethodException {
    Method method = remapper.tryMapMethodToSrg(cl, bukkitName, pTypes);
    if (method != null) {
      return method;
    } else {
      return cl.getDeclaredMethod(bukkitName, pTypes);
    }
  }

  // bukkit -> srg
  public static Object[] handleFromDescStr(
    String desc,
    ClassLoader classLoader
  ) {
    return new Object[] { remapper.mapMethodDesc(desc), classLoader };
  }

  // bukkit -> srg
  public static MethodType redirectFromDescStr(
    String desc,
    ClassLoader classLoader
  ) {
    String methodDesc = remapper.mapMethodDesc(desc);
    return MethodType.fromMethodDescriptorString(methodDesc, classLoader);
  }

  // bukkit -> srg
  public static Object[] handleLookupFindStatic(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType
  ) {
    Method method = remapper.tryMapMethodToSrg(
      cl,
      name,
      methodType.parameterArray()
    );
    return new Object[] {
      lookup,
      cl,
      method == null ? name : method.getName(),
      methodType,
    };
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindStatic(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType
  ) throws NoSuchMethodException, IllegalAccessException {
    Method method = remapper.tryMapMethodToSrg(
      cl,
      name,
      methodType.parameterArray()
    );
    if (method != null) {
      return lookup.findStatic(cl, method.getName(), methodType);
    } else {
      return lookup.findStatic(cl, name, methodType);
    }
  }

  // bukkit -> srg
  public static Object[] handleLookupFindVirtual(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType
  ) {
    return handleLookupFindStatic(lookup, cl, name, methodType);
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindVirtual(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType
  ) throws NoSuchMethodException, IllegalAccessException {
    Method method = remapper.tryMapMethodToSrg(
      cl,
      name,
      methodType.parameterArray()
    );
    if (method != null) {
      return lookup.findVirtual(cl, method.getName(), methodType);
    } else {
      return lookup.findVirtual(cl, name, methodType);
    }
  }

  // bukkit -> srg
  public static Object[] handleLookupFindSpecial(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType,
    Class<?> spec
  ) {
    Method method = remapper.tryMapMethodToSrg(
      cl,
      name,
      methodType.parameterArray()
    );
    return new Object[] {
      lookup,
      cl,
      method == null ? name : method.getName(),
      methodType,
      spec,
    };
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindSpecial(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    MethodType methodType,
    Class<?> spec
  ) throws NoSuchMethodException, IllegalAccessException {
    Method method = remapper.tryMapMethodToSrg(
      cl,
      name,
      methodType.parameterArray()
    );
    if (method != null) {
      return lookup.findSpecial(cl, method.getName(), methodType, spec);
    } else {
      return lookup.findSpecial(cl, name, methodType, spec);
    }
  }

  // bukkit -> srg
  public static Object[] handleLookupFindGetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return new Object[] { lookup, cl, field, type };
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindGetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws IllegalAccessException, NoSuchFieldException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findGetter(cl, field, type);
  }

  // bukkit -> srg
  public static Object[] handleLookupFindSetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    return handleLookupFindGetter(lookup, cl, name, type);
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindSetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws IllegalAccessException, NoSuchFieldException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findSetter(cl, field, type);
  }

  // bukkit -> srg
  public static Object[] handleLookupFindStaticGetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    return handleLookupFindGetter(lookup, cl, name, type);
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindStaticGetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws IllegalAccessException, NoSuchFieldException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findStaticGetter(cl, field, type);
  }

  // bukkit -> srg
  public static Object[] handleLookupFindStaticSetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    return handleLookupFindGetter(lookup, cl, name, type);
  }

  // bukkit -> srg
  public static MethodHandle redirectLookupFindStaticSetter(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws IllegalAccessException, NoSuchFieldException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findStaticSetter(cl, field, type);
  }

  // bukkit -> srg
  public static Class<?> redirectLookupFindClass(
    MethodHandles.Lookup lookup,
    String name
  ) throws ClassNotFoundException {
    return redirectClassForName(
      name,
      false,
      lookup.lookupClass().getClassLoader()
    );
  }

  // bukkit -> srg
  public static Object[] handleLookupFindVarHandle(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    return handleLookupFindGetter(lookup, cl, name, type);
  }

  // bukkit -> srg
  public static VarHandle redirectLookupFindVarHandle(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws NoSuchFieldException, IllegalAccessException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findVarHandle(cl, field, type);
  }

  // bukkit -> srg
  public static Object[] handleLookupFindStaticVarHandle(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) {
    return handleLookupFindGetter(lookup, cl, name, type);
  }

  // bukkit -> srg
  public static VarHandle redirectLookupFindStaticVarHandle(
    MethodHandles.Lookup lookup,
    Class<?> cl,
    String name,
    Class<?> type
  ) throws NoSuchFieldException, IllegalAccessException {
    String field = remapper.tryMapFieldToSrg(cl, name);
    return lookup.findStaticVarHandle(cl, field, type);
  }

  public static Object[] handleClassLoaderLoadClass(
    ClassLoader loader,
    String binaryName
  ) {
    String normalizedName = normalizeReflectionClassName(binaryName);
    return new Object[] { loader, mapTypeForReflection(normalizedName) };
  }

  // bukkit -> srg
  public static Class<?> redirectClassLoaderLoadClass(
    ClassLoader loader,
    String binaryName
  ) throws ClassNotFoundException {
    String normalizedName = normalizeReflectionClassName(binaryName);
    String mappedName = mapTypeForReflection(normalizedName);
    try {
      return loader.loadClass(mappedName);
    } catch (ClassNotFoundException | LinkageError e) {
      if (!mappedName.equals(normalizedName)) {
        try {
          return loader.loadClass(normalizedName);
        } catch (ClassNotFoundException | LinkageError ignored) {
          return tryLoadClass(normalizedName, false, loader);
        }
      }
      if (e instanceof ClassNotFoundException cnfe) {
        throw cnfe;
      }
      throw new ClassNotFoundException(mappedName, e);
    }
  }

  public static String findMappedResource(Class<?> cl, String name) {
    if (name.isEmpty() || !name.endsWith(".class")) return null;
    name = name.substring(0, name.length() - 6);
    String className;
    if (cl != null) {
      if (name.charAt(0) == '/') {
        className = name.substring(1);
      } else {
        Class<?> c = cl;
        while (c.isArray()) c = c.getComponentType();
        String mapped = remapper
          .toBukkitRemapper()
          .mapType(c.getName().replace('.', '/'));
        int index = mapped.lastIndexOf('/');
        if (index == -1) {
          className = name;
        } else {
          className = mapped.substring(0, index) + '/' + name;
        }
      }
    } else {
      className = name;
    }
    className = remapper.mapType(
      CraftBukkitVersionRemapper.remapInternalName(className)
    );
    if (
      className.startsWith("java/") ||
      className.startsWith("jdk/") ||
      className.startsWith("javax/")
    ) {
      return null;
    } else if (cl != null) return "/" + className + ".class";
    else return className + ".class";
  }

  public static URL redirectClassGetResource(Class<?> cl, String name)
    throws MalformedURLException {
    String mappedResource = findMappedResource(cl, name);
    if (mappedResource == null) {
      if (
        name.startsWith("/java/") ||
        name.startsWith("/jdk/") ||
        name.startsWith("/javax/")
      ) {
        return ClassLoader.getPlatformClassLoader().getResource(name);
      }
      return cl.getResource(name);
    } else {
      URL resource = cl.getResource(mappedResource);
      return resource == null ? null : new URL("remap:" + resource);
    }
  }

  public static InputStream redirectClassGetResourceAsStream(
    Class<?> cl,
    String name
  ) throws IOException {
    String mappedResource = findMappedResource(cl, name);
    if (mappedResource == null) {
      if (
        name.startsWith("/java/") ||
        name.startsWith("/jdk/") ||
        name.startsWith("/javax/")
      ) {
        return ClassLoader.getPlatformClassLoader().getResourceAsStream(name);
      }
      return cl.getResourceAsStream(name);
    } else {
      URL resource = cl.getResource(mappedResource);
      if (resource == null) return null;
      return new URL("remap:" + resource).openStream();
    }
  }

  public static URL redirectClassLoaderGetResource(
    ClassLoader loader,
    String name
  ) throws MalformedURLException {
    String mappedResource = findMappedResource(null, name);
    if (mappedResource == null) {
      if (
        name.startsWith("java/") ||
        name.startsWith("jdk/") ||
        name.startsWith("javax/")
      ) {
        return ClassLoader.getPlatformClassLoader().getResource(name);
      }
      return loader.getResource(name);
    } else {
      URL resource = loader.getResource(mappedResource);
      return resource == null ? null : new URL("remap:" + resource);
    }
  }

  public static Enumeration<URL> redirectClassLoaderGetResources(
    ClassLoader loader,
    String name
  ) throws IOException {
    String mappedResource = findMappedResource(null, name);
    if (mappedResource == null) {
      if (
        name.startsWith("java/") ||
        name.startsWith("jdk/") ||
        name.startsWith("javax/")
      ) {
        return ClassLoader.getPlatformClassLoader().getResources(name);
      }
      return loader.getResources(name);
    } else {
      Enumeration<URL> resources = loader.getResources(mappedResource);
      return Enumerations.remapped(resources);
    }
  }

  public static InputStream redirectClassLoaderGetResourceAsStream(
    ClassLoader loader,
    String name
  ) throws IOException {
    URL url = redirectClassLoaderGetResource(loader, name);
    if (url == null) {
      return null;
    } else {
      return url.openStream();
    }
  }

  public static Object[] handleMethodInvoke(
    Method method,
    Object src,
    Object[] param
  ) throws Throwable {
    Object[] ret = ArclightRedirectAdapter.runHandle(
      remapper,
      method,
      src,
      param
    );
    if (ret != null) {
      return ret;
    } else {
      return new Object[] { method, src, param };
    }
  }

  public static Object redirectMethodInvoke(
    Method method,
    Object src,
    Object[] param
  ) throws Throwable {
    Object ret = ArclightRedirectAdapter.runRedirect(
      remapper,
      method,
      src,
      param
    );
    if (ret != remapper) {
      return ret;
    } else {
      return method.invoke(src, param);
    }
  }

  public static Object[] handleDefineClass(
    ClassLoader loader,
    byte[] b,
    int off,
    int len
  ) {
    byte[] bytes = transformOrAdd(loader, b);
    return new Object[] { loader, bytes, 0, bytes.length };
  }

  public static Class<?> redirectDefineClass(
    ClassLoader loader,
    byte[] b,
    int off,
    int len
  ) {
    return redirectDefineClass(loader, null, b, off, len);
  }

  public static Object[] handleDefineClass(
    ClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len
  ) {
    byte[] bytes = transformOrAdd(loader, b);
    return new Object[] {
      loader,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      bytes,
      0,
      bytes.length,
    };
  }

  public static Class<?> redirectDefineClass(
    ClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len
  ) {
    return redirectDefineClass(loader, name, b, off, len, null);
  }

  public static Object[] handleDefineClass(
    ClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len,
    ProtectionDomain pd
  ) {
    byte[] bytes = transformOrAdd(loader, b);
    return new Object[] {
      loader,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      bytes,
      0,
      bytes.length,
      pd,
    };
  }

  public static Class<?> redirectDefineClass(
    ClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len,
    ProtectionDomain pd
  ) {
    byte[] bytes = transformOrAdd(loader, b);
    return Unsafe.defineClass(
      new ClassReader(bytes).getClassName().replace('/', '.'),
      bytes,
      0,
      bytes.length,
      loader,
      pd
    );
  }

  public static Object[] handleDefineClass(
    ClassLoader loader,
    String name,
    ByteBuffer b,
    ProtectionDomain pd
  ) {
    byte[] bytes = new byte[b.remaining()];
    b.get(bytes);
    bytes = transformOrAdd(loader, bytes);
    return new Object[] {
      loader,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      ByteBuffer.wrap(bytes),
      pd,
    };
  }

  public static Class<?> redirectDefineClass(
    ClassLoader loader,
    String name,
    ByteBuffer b,
    ProtectionDomain pd
  ) {
    byte[] bytes = new byte[b.remaining()];
    b.get(bytes);
    return redirectDefineClass(loader, name, bytes, 0, bytes.length, pd);
  }

  public static Object[] handleDefineClass(
    SecureClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len,
    CodeSource cs
  ) {
    byte[] bytes = transformOrAdd(loader, b);
    return new Object[] {
      loader,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      bytes,
      0,
      bytes.length,
      cs,
    };
  }

  public static Class<?> redirectDefineClass(
    SecureClassLoader loader,
    String name,
    byte[] b,
    int off,
    int len,
    CodeSource cs
  ) {
    return redirectDefineClass(
      loader,
      name,
      b,
      off,
      len,
      new ProtectionDomain(cs, new Permissions())
    );
  }

  public static Object[] handleDefineClass(
    SecureClassLoader loader,
    String name,
    ByteBuffer b,
    CodeSource cs
  ) {
    byte[] bytes = new byte[b.remaining()];
    b.get(bytes);
    bytes = transformOrAdd(loader, bytes);
    return new Object[] {
      loader,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      ByteBuffer.wrap(bytes),
      cs,
    };
  }

  public static Class<?> redirectDefineClass(
    SecureClassLoader loader,
    String name,
    ByteBuffer b,
    CodeSource cs
  ) {
    return redirectDefineClass(
      loader,
      name,
      b,
      new ProtectionDomain(cs, new Permissions())
    );
  }

  public static Object[] handleUnsafeDefineClass(
    Object unsafe,
    String name,
    byte[] bytes,
    int off,
    int len,
    ClassLoader loader,
    ProtectionDomain pd
  ) {
    bytes = transformOrAdd(loader, bytes);
    return new Object[] {
      unsafe,
      new ClassReader(bytes).getClassName().replace('/', '.'),
      bytes,
      0,
      bytes.length,
      loader,
      pd,
    };
  }

  public static Class<?> redirectUnsafeDefineClass(
    Object unsafe,
    String name,
    byte[] bytes,
    int off,
    int len,
    ClassLoader loader,
    ProtectionDomain pd
  ) {
    return redirectDefineClass(loader, name, bytes, off, len, pd);
  }

  public static Object[] handleLookupDefineClass(
    MethodHandles.Lookup lookup,
    byte[] bytes
  ) {
    return new Object[] {
      lookup,
      transformOrAdd(lookup.lookupClass().getClassLoader(), bytes),
    };
  }

  public static Class<?> redirectLookupDefineClass(
    MethodHandles.Lookup lookup,
    byte[] bytes
  ) throws Throwable {
    byte[] transform = transformOrAdd(
      lookup.lookupClass().getClassLoader(),
      bytes
    );
    MethodHandle mh = Unsafe.lookup().findVirtual(
      MethodHandles.Lookup.class,
      "defineClass",
      MethodType.methodType(Class.class, byte[].class)
    );
    return (Class<?>) mh.invokeExact(lookup, transform);
  }

  public static Object[] handleLookupDefineHiddenClass(
    MethodHandles.Lookup lookup,
    byte[] bytes,
    boolean initialize,
    MethodHandles.Lookup.ClassOption[] options
  ) {
    return new Object[] {
      lookup,
      transformOrAdd(lookup.lookupClass().getClassLoader(), bytes),
      initialize,
      options,
    };
  }

  public static MethodHandles.Lookup redirectLookupDefineHiddenClass(
    MethodHandles.Lookup lookup,
    byte[] bytes,
    boolean initialize,
    MethodHandles.Lookup.ClassOption[] options
  ) throws IllegalAccessException {
    byte[] transform = transformOrAdd(
      lookup.lookupClass().getClassLoader(),
      bytes
    );
    return lookup.defineHiddenClass(transform, initialize, options);
  }

  public static Object[] handleLookupDefineHiddenClassWithClassData(
    MethodHandles.Lookup lookup,
    byte[] bytes,
    Object classData,
    boolean initialize,
    MethodHandles.Lookup.ClassOption[] options
  ) {
    byte[] transform = transformOrAdd(
      lookup.lookupClass().getClassLoader(),
      bytes
    );
    return new Object[] { lookup, transform, classData, initialize, options };
  }

  public static MethodHandles.Lookup redirectLookupDefineHiddenClassWithClassData(
    MethodHandles.Lookup lookup,
    byte[] bytes,
    Object classData,
    boolean initialize,
    MethodHandles.Lookup.ClassOption[] options
  ) throws IllegalAccessException {
    byte[] transform = transformOrAdd(
      lookup.lookupClass().getClassLoader(),
      bytes
    );
    return lookup.defineHiddenClassWithClassData(
      transform,
      classData,
      initialize,
      options
    );
  }

  public static byte[] transformOrAdd(ClassLoader loader, byte[] bytes) {
    RemappingClassLoader rcl = null;
    while (loader != null) {
      if (loader instanceof RemappingClassLoader) {
        rcl = (RemappingClassLoader) loader;
        break;
      } else {
        loader = loader.getParent();
      }
    }
    if (rcl != null) {
      // Don't transform for remap=false.
      // We don't have ReflectionHandler in their ClassLoader.
      var repo = new ClassRepoWrapper(
        GlobalClassRepo.INSTANCE,
        rcl.getRemapConfig()
      );
      return rcl.getRemapper().remapClassFile(bytes, repo, true);
    } else {
      ArclightRedirectAdapter.scanMethod(bytes);
      return bytes;
    }
  }
}
