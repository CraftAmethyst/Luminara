package io.izzel.arclight.common.mod.util.remapper.patcher.integrated;

import io.izzel.arclight.api.PluginPatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class CMI {

    private static final String COMPAT_OWNER =
        "io/izzel/arclight/common/mod/compat/CMICompat";
    private static final String COMPAT_NAME = "getExecutor";
    private static final String COMPAT_DESC =
        "(Ljava/lang/Class;Ljava/util/concurrent/ExecutorService;)Ljava/util/concurrent/ExecutorService;";

    public static void handleThreadExecutor(
        ClassNode node,
        PluginPatcher.ClassRepo repo
    ) {
        for (MethodNode method : node.methods) {
            if (
                method.name.equals("getExecutor") &&
                method.desc.equals("()Ljava/util/concurrent/ExecutorService;")
            ) {
                InsnList list = new InsnList();
                list.add(new LdcInsnNode(Type.getObjectType(node.name)));
                list.add(
                    new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        node.name,
                        "EXECUTOR",
                        "Ljava/util/concurrent/ExecutorService;"
                    )
                );
                list.add(
                    new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        COMPAT_OWNER,
                        COMPAT_NAME,
                        COMPAT_DESC,
                        false
                    )
                );
                list.add(new InsnNode(Opcodes.ARETURN));

                method.instructions.clear();
                method.instructions.add(list);
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.maxStack = 2;
                method.maxLocals = 0;
                return;
            }
        }
    }
}
