package io.izzel.arclight.common.mod.util.remapper.patcher.integrated;

import io.izzel.arclight.api.PluginPatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class LuckPerms {

    private static final String COMPAT_OWNER =
        "io/izzel/arclight/common/mod/compat/LuckPermsCompat";

    public static void handlePermissibleInjector(
        ClassNode node,
        PluginPatcher.ClassRepo repo
    ) {
        for (MethodNode method : node.methods) {
            if (method.name.equals("<clinit>") && method.desc.equals("()V")) {
                method.instructions.clear();
                addFieldInitialization(
                    method,
                    node.name,
                    "HUMAN_ENTITY_PERMISSIBLE_FIELD",
                    "humanEntityPermissibleField"
                );
                addFieldInitialization(
                    method,
                    node.name,
                    "PERMISSIBLE_BASE_ATTACHMENTS_FIELD",
                    "permissibleBaseAttachmentsField"
                );
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.maxStack = 1;
                method.maxLocals = 0;
                return;
            }
        }
    }

    private static void addFieldInitialization(
        MethodNode method,
        String owner,
        String fieldName,
        String compatMethod
    ) {
        method.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                COMPAT_OWNER,
                compatMethod,
                "()Ljava/lang/reflect/Field;",
                false
            )
        );
        method.instructions.add(
            new FieldInsnNode(
                Opcodes.PUTSTATIC,
                owner,
                fieldName,
                "Ljava/lang/reflect/Field;"
            )
        );
    }
}
