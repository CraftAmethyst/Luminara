package io.izzel.arclight.common.mod.util.remapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.izzel.arclight.api.PluginPatcher;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.common.mod.util.remapper.patcher.ArclightPluginPatcher;
import io.izzel.arclight.common.mod.util.remapper.patcher.PluginLoggerTransformer;
import io.izzel.arclight.common.mod.util.remapper.resource.RemapSourceHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("unchecked")
public class ArclightRemapper {

  public static final ArclightRemapper INSTANCE;
  public static final File DUMP;
  public static final Function<byte[], byte[]> SWITCH_TABLE_FIXER;

  static {
    ArclightI18nLogger.getLogger("Arclight").info("loading-mapping");
    try {
      INSTANCE = new ArclightRemapper();
      String property = System.getProperty("arclight.remapper.dump");
      if (property != null) {
        DUMP = new File(property);
        if (!DUMP.exists()) {
          DUMP.mkdirs();
        }
        try {
          FileUtils.forceDelete(DUMP);
        } catch (IOException ignored) {
        }
      } else {
        DUMP = null;
      }
      SWITCH_TABLE_FIXER =
          (Function<byte[], byte[]>)
              Class.forName("io.izzel.arclight.common.mod.boot.asm.SwitchTableFixer")
                  .getField("INSTANCE")
                  .get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final JarMapping toNmsMapping;
  private final JarMapping toBukkitMapping;
  private final JarMapping fromMojMapping;
  public final InheritanceMap inheritanceMap;
  private final List<PluginTransformer> transformerList = new ArrayList<>();
  private final JarRemapper toBukkitRemapper;
  private final JarRemapper toNmsRemapper;
  private final JarRemapper fromMojRemapper;
  private final List<PluginPatcher> patchers;

  /** Spigot class name to Mojang class name, derived from the reversed Mojang mapping. */
  private final Map<String, String> bukkitToMojangClasses = new HashMap<>();

  /** {@code mojangOwner/name} to parameter count to the Spigot method names. */
  private final Map<String, Map<Integer, Set<String>>> mojangMethods = new HashMap<>();

  public ArclightRemapper() throws Exception {
    this.toNmsMapping = new JarMapping();
    this.toBukkitMapping = new JarMapping();
    this.fromMojMapping = new JarMapping();
    this.inheritanceMap = new InheritanceMap();
    this.toNmsMapping.loadMappings(
        new BufferedReader(
            new InputStreamReader(ArclightRemapper.class.getResourceAsStream("/bukkit_srg.srg"))),
        null,
        null,
        false);
    this.toBukkitMapping.loadMappings(
        new BufferedReader(
            new InputStreamReader(ArclightRemapper.class.getResourceAsStream("/bukkit_srg.srg"))),
        null,
        null,
        true);
    this.fromMojMapping.loadMappings(
        new BufferedReader(
            new InputStreamReader(ArclightRemapper.class.getResourceAsStream("/bukkit_moj.srg"))),
        null,
        null,
        true);
    indexMojangMappings();
    BiMap<String, String> inverseClassMap = HashBiMap.create(toNmsMapping.classes).inverse();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                ArclightRemapper.class.getResourceAsStream("/inheritanceMap.txt")))) {
      inheritanceMap.load(reader, inverseClassMap);
    }
    JointProvider inheritanceProvider = new JointProvider();
    inheritanceProvider.add(inheritanceMap);
    inheritanceProvider.add(new ClassLoaderProvider(ClassLoader.getSystemClassLoader()));
    this.toNmsMapping.setFallbackInheritanceProvider(inheritanceProvider);
    this.toBukkitMapping.setFallbackInheritanceProvider(inheritanceProvider);
    this.fromMojMapping.setFallbackInheritanceProvider(inheritanceProvider);
    this.transformerList.add(ArclightInterfaceInvokerGen.INSTANCE);
    this.transformerList.add(ArclightRedirectAdapter.INSTANCE);
    this.transformerList.add(ClassLoaderAdapter.INSTANCE);
    this.transformerList.add(CraftBukkitVersionRemapper.INSTANCE);
    if (!(java.util.logging.LogManager.getLogManager()
        instanceof org.apache.logging.log4j.jul.LogManager)) {
      this.transformerList.add(new PluginLoggerTransformer());
    }
    this.patchers = ArclightPluginPatcher.load(this.transformerList);
    toBukkitMapping.setFallbackInheritanceProvider(GlobalClassRepo.inheritanceProvider());
    this.toBukkitRemapper = new LenientJarRemapper(toBukkitMapping);
    this.toNmsRemapper = new LenientJarRemapper(toNmsMapping);
    this.fromMojRemapper = new LenientJarRemapper(fromMojMapping);
    RemapSourceHandler.register();
  }

  public static ClassLoaderRemapper createClassLoaderRemapper(ClassLoader classLoader) {
    return new ClassLoaderRemapper(
        INSTANCE.copyOf(INSTANCE.toNmsMapping),
        INSTANCE.copyOf(INSTANCE.toBukkitMapping),
        classLoader);
  }

  public static JarRemapper getResourceMapper() {
    return INSTANCE.toBukkitRemapper;
  }

  public static JarRemapper getNmsMapper() {
    return INSTANCE.toNmsRemapper;
  }

  /**
   * Mojang name to Spigot name. Plugins built against Paper/Paperweight reference NMS types and
   * members by their Mojang names, while the mapping sources use the Spigot names (for example
   * {@code EntityType} vs {@code EntityTypes}).
   */
  public static JarRemapper getMojMapper() {
    return INSTANCE.fromMojRemapper;
  }

  /** Spigot class name to Mojang class name, or the input when the names are equal. */
  public static String bukkitToMojangClassName(String internalName) {
    String mapped = INSTANCE.bukkitToMojangClasses.get(internalName);
    return mapped == null ? internalName : mapped;
  }

  /**
   * Translates a Mojang method name to the Spigot name the other mappings use. Returns {@code null}
   * when the name is unknown or ambiguous for that arity.
   */
  public static String matchMojangMethodName(
      String mojangOwner, String mojangName, int paramCount) {
    Map<Integer, Set<String>> byCount = INSTANCE.mojangMethods.get(mojangOwner + "/" + mojangName);
    if (byCount == null) {
      return null;
    }
    Set<String> candidates = byCount.get(paramCount);
    return candidates != null && candidates.size() == 1 ? candidates.iterator().next() : null;
  }

  private void indexMojangMappings() {
    fromMojMapping.classes.forEach(
        (mojang, bukkit) -> bukkitToMojangClasses.putIfAbsent(bukkit, mojang));
    for (Map.Entry<String, String> entry : fromMojMapping.methods.entrySet()) {
      String key = entry.getKey();
      int space = key.lastIndexOf(' ');
      if (space < 0) {
        continue;
      }
      String ownerAndName = key.substring(0, space);
      int parameterCount = parameterCount(key.substring(space + 1));
      if (parameterCount < 0) {
        continue;
      }
      mojangMethods
          .computeIfAbsent(ownerAndName, k -> new HashMap<>())
          .computeIfAbsent(parameterCount, k -> new HashSet<>())
          .add(entry.getValue());
    }
  }

  private static int parameterCount(String methodDescriptor) {
    int start = methodDescriptor.indexOf('(');
    if (start < 0) {
      return -1;
    }
    int count = 0;
    int i = start + 1;
    while (i < methodDescriptor.length() && methodDescriptor.charAt(i) != ')') {
      if (methodDescriptor.charAt(i) == 'L') {
        int end = methodDescriptor.indexOf(';', i);
        if (end < 0) {
          return -1;
        }
        i = end + 1;
      } else {
        i++;
      }
      count++;
    }
    return count;
  }

  public List<PluginTransformer> getTransformerList() {
    return transformerList;
  }

  public List<PluginPatcher> getPatchers() {
    return patchers;
  }

  private static long pkgOffset, clOffset, mdOffset, fdOffset, mapOffset;

  static {
    try {
      pkgOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("packages"));
      clOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("classes"));
      mdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("methods"));
      fdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("fields"));
      mapOffset = Unsafe.objectFieldOffset(JarMapping.class.getDeclaredField("inheritanceMap"));
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  private JarMapping copyOf(JarMapping mapping) {
    JarMapping jarMapping = new JarMapping();
    Unsafe.putObject(jarMapping, pkgOffset, Unsafe.getObject(mapping, pkgOffset));
    Unsafe.putObject(jarMapping, clOffset, Unsafe.getObject(mapping, clOffset));
    Unsafe.putObject(jarMapping, mdOffset, Unsafe.getObject(mapping, mdOffset));
    Unsafe.putObject(jarMapping, fdOffset, Unsafe.getObject(mapping, fdOffset));
    Unsafe.putObject(jarMapping, mapOffset, Unsafe.getObject(mapping, mapOffset));
    return jarMapping;
  }
}
