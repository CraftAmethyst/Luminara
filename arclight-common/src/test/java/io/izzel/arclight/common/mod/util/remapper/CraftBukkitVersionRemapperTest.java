package io.izzel.arclight.common.mod.util.remapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.izzel.arclight.api.ArclightVersion;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

class CraftBukkitVersionRemapperTest {

  private static String currentPackage() {
    try {
      ArclightVersion.setVersion(ArclightVersion.FEUDAL_KINGS);
    } catch (IllegalStateException ignored) {
      // Another test already initialized the runtime version.
    }
    return ArclightVersion.current().packageName();
  }

  @Test
  void mapsGenericPackageBackToTheCurrentVersion() {
    String current = currentPackage();
    assertEquals(
        "org/bukkit/craftbukkit/" + current + "/util/CraftLegacy",
        CraftBukkitVersionRemapper.toVersionedInternalName(
            "org/bukkit/craftbukkit/v/util/CraftLegacy"));
    assertEquals(
        "org.bukkit.craftbukkit." + current + ".util.CraftLegacy",
        CraftBukkitVersionRemapper.toVersionedBinaryName(
            "org.bukkit.craftbukkit.v.util.CraftLegacy"));
  }

  @Test
  void leavesOtherNamesUntouched() {
    assertEquals(
        "org.bukkit.craftbukkit.util.CraftSpawnCategory",
        CraftBukkitVersionRemapper.toVersionedBinaryName(
            "org.bukkit.craftbukkit.util.CraftSpawnCategory"));
    assertEquals(
        "net.minecraft.world.entity.MobCategory",
        CraftBukkitVersionRemapper.toVersionedBinaryName("net.minecraft.world.entity.MobCategory"));
  }

  @Test
  void rewritesLegacyVersionPackagesToTheCurrentVersion() {
    String current = currentPackage();
    assertEquals(
        "org/bukkit/craftbukkit/" + current + "/entity/CraftPlayer",
        CraftBukkitVersionRemapper.remapInternalName(
            "org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer"));
    assertEquals(
        "org.bukkit.craftbukkit." + current + ".entity.CraftPlayer",
        CraftBukkitVersionRemapper.remapBinaryName(
            "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer"));
    assertEquals(
        "org/bukkit/craftbukkit/" + current + "/util/CraftLegacy",
        CraftBukkitVersionRemapper.remapInternalName("org/bukkit/craftbukkit/v/util/CraftLegacy"));
    assertEquals(
        "org.bukkit.craftbukkit." + current + ".util.CraftLegacy",
        CraftBukkitVersionRemapper.remapBinaryName("org.bukkit.craftbukkit.v.util.CraftLegacy"));
    assertEquals(
        "java.lang.String", CraftBukkitVersionRemapper.remapBinaryName("java/lang/String"));
  }

  @Test
  void rewritesOwnersAndDescriptors() {
    String current = currentPackage();
    ClassNode pluginClass = new ClassNode();
    pluginClass.name = "example/Plugin";
    MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null);
    method.instructions.add(
        new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "org/bukkit/craftbukkit/v1_20_R1/util/CraftMagicNumbers",
            "getVersion",
            "()Lorg/bukkit/craftbukkit/v1_20_R1/CraftServer;",
            false));
    pluginClass.methods.add(method);

    CraftBukkitVersionRemapper.INSTANCE.handleClass(pluginClass, null, null);

    MethodInsnNode call = (MethodInsnNode) method.instructions.getFirst();
    assertEquals("org/bukkit/craftbukkit/" + current + "/util/CraftMagicNumbers", call.owner);
    assertEquals("()Lorg/bukkit/craftbukkit/" + current + "/CraftServer;", call.desc);
  }
}
