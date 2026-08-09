package io.izzel.arclight.boot.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLoaderTransformerTest {

    @Test
    void redirectsSingleArgumentServiceLoads() {
        ClassNode serviceUser = new ClassNode();
        serviceUser.name = "example/ServiceUser";
        MethodNode load = new MethodNode(Opcodes.ACC_PUBLIC, "load", "()V", null, null);
        load.instructions.add(new LdcInsnNode(org.objectweb.asm.Type.getType(Runnable.class)));
        MethodInsnNode serviceLoad = new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "java/util/ServiceLoader",
            "load",
            "(Ljava/lang/Class;)Ljava/util/ServiceLoader;",
            false
        );
        load.instructions.add(serviceLoad);
        load.instructions.add(new InsnNode(Opcodes.POP));
        load.instructions.add(new InsnNode(Opcodes.RETURN));
        serviceUser.methods.add(load);

        assertTrue(new ServiceLoaderTransformer().processClass(serviceUser));
        assertEquals("io/izzel/arclight/boot/asm/ServiceLoaderTransformer", serviceLoad.owner);
    }
}
