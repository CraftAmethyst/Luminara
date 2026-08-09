package io.izzel.arclight.common.mod.util.remapper.patcher.integrated;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEditTest {

    @Test
    void forcesFaweCommandRegistrationOntoBukkitPath() {
        ClassNode registration = new ClassNode();
        registration.name = "com/sk89q/bukkit/util/CommandRegistration";
        MethodNode getCommandMap = new MethodNode(
            Opcodes.ACC_PRIVATE,
            "getCommandMap",
            "()Lorg/bukkit/command/CommandMap;",
            null,
            null
        );
        getCommandMap.instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "io/papermc/lib/PaperLib",
            "isPaper",
            "()Z",
            false
        ));
        getCommandMap.instructions.add(new InsnNode(Opcodes.IRETURN));
        registration.methods.add(getCommandMap);

        WorldEdit.handleFaweCommandRegistration(registration, null);

        assertEquals(Opcodes.ICONST_0, getCommandMap.instructions.getFirst().getOpcode());
    }
}
