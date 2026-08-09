package io.izzel.arclight.common.mod.util.remapper;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CraftBukkitVersionRemapperTest {

    @Test
    void normalizesLegacyVersionPackages() {
        assertEquals(
            "org/bukkit/craftbukkit/v/entity/CraftPlayer",
            CraftBukkitVersionRemapper.remapInternalName("org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer")
        );
        assertEquals(
            "org.bukkit.craftbukkit.v.entity.CraftPlayer",
            CraftBukkitVersionRemapper.remapBinaryName("org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer")
        );
        assertEquals("java.lang.String", CraftBukkitVersionRemapper.remapBinaryName("java/lang/String"));
    }

    @Test
    void rewritesOwnersAndDescriptors() {
        ClassNode pluginClass = new ClassNode();
        pluginClass.name = "example/Plugin";
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null);
        method.instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "org/bukkit/craftbukkit/v1_20_R1/util/CraftMagicNumbers",
            "getVersion",
            "()Lorg/bukkit/craftbukkit/v1_20_R1/CraftServer;",
            false
        ));
        pluginClass.methods.add(method);

        CraftBukkitVersionRemapper.INSTANCE.handleClass(pluginClass, null, null);

        MethodInsnNode call = (MethodInsnNode) method.instructions.getFirst();
        assertEquals("org/bukkit/craftbukkit/v/util/CraftMagicNumbers", call.owner);
        assertEquals("()Lorg/bukkit/craftbukkit/v/CraftServer;", call.desc);
    }
}
