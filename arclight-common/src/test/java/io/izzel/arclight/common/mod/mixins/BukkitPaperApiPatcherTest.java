package io.izzel.arclight.common.mod.mixins;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPaperApiPatcherTest {

    @Test
    void injectsMinecraftVersionExactlyOnce() {
        ClassNode bukkit = new ClassNode();
        bukkit.name = "org/bukkit/Bukkit";

        BukkitPaperApiPatcher.patch("org.bukkit.Bukkit", bukkit);
        BukkitPaperApiPatcher.patch("org.bukkit.Bukkit", bukkit);

        var methods = bukkit.methods.stream()
            .filter(method -> method.name.equals("getMinecraftVersion") && method.desc.equals("()Ljava/lang/String;"))
            .toList();
        assertEquals(1, methods.size());
        assertTrue(methods.getFirst().instructions.iterator().next() instanceof MethodInsnNode call
            && call.getOpcode() == Opcodes.INVOKESTATIC
            && call.owner.equals("org/bukkit/Bukkit")
            && call.name.equals("getServer"));
    }
}
