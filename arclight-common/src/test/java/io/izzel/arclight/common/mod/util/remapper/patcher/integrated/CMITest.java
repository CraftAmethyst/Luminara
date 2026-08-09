package io.izzel.arclight.common.mod.util.remapper.patcher.integrated;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CMITest {

    @Test
    void replacesExecutorAccessorWithCompatibilityGuard() {
        ClassNode threadClass = new ClassNode();
        threadClass.name = "com/Zrips/CMI/utils/CMIThread";
        MethodNode accessor = new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getExecutor",
            "()Ljava/util/concurrent/ExecutorService;",
            null,
            null
        );
        accessor.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        accessor.instructions.add(new InsnNode(Opcodes.ARETURN));
        threadClass.methods.add(accessor);

        CMI.handleThreadExecutor(threadClass, null);

        MethodInsnNode compatCall = null;
        for (var instruction : accessor.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                compatCall = call;
            }
        }
        assertNotNull(compatCall);
        assertEquals("io/izzel/arclight/common/mod/compat/CMICompat", compatCall.owner);
        assertEquals("getExecutor", compatCall.name);
        assertEquals(Opcodes.ARETURN, accessor.instructions.getLast().getOpcode());
    }
}
