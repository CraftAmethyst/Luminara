package io.izzel.arclight.common.mod.util.remapper.patcher.integrated;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuckPermsTest {

    @Test
    void initializesPermissibleFieldsThroughArclight() {
        ClassNode injector = new ClassNode();
        injector.name =
            "me/lucko/luckperms/bukkit/inject/permissible/PermissibleInjector";
        MethodNode initializer = new MethodNode(
            Opcodes.ACC_STATIC,
            "<clinit>",
            "()V",
            null,
            null
        );
        initializer.instructions.add(new InsnNode(Opcodes.RETURN));
        injector.methods.add(initializer);

        LuckPerms.handlePermissibleInjector(injector, null);

        MethodInsnNode humanField =
            (MethodInsnNode) initializer.instructions.getFirst();
        FieldInsnNode humanStore = (FieldInsnNode) humanField.getNext();
        MethodInsnNode attachmentsField =
            (MethodInsnNode) humanStore.getNext();
        assertEquals(
            "io/izzel/arclight/common/mod/compat/LuckPermsCompat",
            humanField.owner
        );
        assertEquals("humanEntityPermissibleField", humanField.name);
        assertEquals("HUMAN_ENTITY_PERMISSIBLE_FIELD", humanStore.name);
        assertEquals(
            "permissibleBaseAttachmentsField",
            attachmentsField.name
        );
        assertEquals(Opcodes.RETURN, initializer.instructions.getLast().getOpcode());
    }
}
