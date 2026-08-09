package io.izzel.arclight.common.mod.mixins;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class BukkitPaperApiPatcher {

    private static final String BUKKIT_CLASS = "org.bukkit.Bukkit";

    private BukkitPaperApiPatcher() {
    }

    public static void patch(String targetClassName, ClassNode targetClass) {
        if (!BUKKIT_CLASS.equals(targetClassName) || hasMethod(targetClass, "getMinecraftVersion", "()Ljava/lang/String;")) {
            return;
        }

        MethodNode method = new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getMinecraftVersion",
            "()Ljava/lang/String;",
            null,
            null
        );
        method.instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "org/bukkit/Bukkit",
            "getServer",
            "()Lorg/bukkit/Server;",
            false
        ));
        method.instructions.add(new MethodInsnNode(
            Opcodes.INVOKEINTERFACE,
            "org/bukkit/Server",
            "getMinecraftVersion",
            "()Ljava/lang/String;",
            true
        ));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 1;
        method.maxLocals = 0;
        targetClass.methods.add(method);
    }

    private static boolean hasMethod(ClassNode classNode, String name, String descriptor) {
        for (MethodNode method : classNode.methods) {
            if (name.equals(method.name) && descriptor.equals(method.desc)) {
                return true;
            }
        }
        return false;
    }
}
