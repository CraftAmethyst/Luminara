package io.izzel.arclight.boot;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

class AbstractBootstrapTest {

    private static final String OWNER = "com/mojang/brigadier/tree/CommandNode";

    @Test
    void resetsCurrentCommandAfterPredicateInvocation() throws Exception {
        byte[] transformed = AbstractBootstrap.transformCommandNode(
            new ByteArrayInputStream(commandNode(true))
        );
        ClassNode node = read(transformed);

        assertEquals(
            1,
            node.fields
                .stream()
                .filter(field -> field.name.equals("CURRENT_COMMAND"))
                .count()
        );
        assertEquals(
            1,
            node.methods
                .stream()
                .filter(method -> method.name.equals("removeCommand"))
                .count()
        );

        MethodNode canUse = node.methods
            .stream()
            .filter(method -> method.name.equals("canUse"))
            .findFirst()
            .orElseThrow();
        MethodInsnNode predicateCall = null;
        for (var instruction : canUse.instructions) {
            if (
                instruction instanceof MethodInsnNode call &&
                call.owner.equals("java/util/function/Predicate") &&
                call.name.equals("test")
            ) {
                predicateCall = call;
                break;
            }
        }
        assertNotNull(predicateCall);
        assertInstanceOf(FieldInsnNode.class, predicateCall.getPrevious());
        FieldInsnNode assignment = (FieldInsnNode) predicateCall.getPrevious();
        assertEquals(Opcodes.PUTSTATIC, assignment.getOpcode());
        assertEquals("CURRENT_COMMAND", assignment.name);
        assertEquals(Opcodes.ACONST_NULL, predicateCall.getNext().getOpcode());
        assertInstanceOf(
            FieldInsnNode.class,
            predicateCall.getNext().getNext()
        );
        FieldInsnNode reset = (FieldInsnNode) predicateCall.getNext().getNext();
        assertEquals(Opcodes.PUTSTATIC, reset.getOpcode());
        assertEquals("CURRENT_COMMAND", reset.name);
    }

    @Test
    void transformationIsStructurallyIdempotent() throws Exception {
        byte[] once = AbstractBootstrap.transformCommandNode(
            new ByteArrayInputStream(commandNode(true))
        );
        byte[] twice = AbstractBootstrap.transformCommandNode(
            new ByteArrayInputStream(once)
        );
        ClassNode node = read(twice);

        assertEquals(
            1,
            node.fields
                .stream()
                .filter(field -> field.name.equals("CURRENT_COMMAND"))
                .count()
        );
        assertEquals(
            1,
            node.methods
                .stream()
                .filter(method -> method.name.equals("removeCommand"))
                .count()
        );
    }

    @Test
    void rejectsUnknownBrigadierShape() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () ->
                AbstractBootstrap.transformCommandNode(
                    new ByteArrayInputStream(commandNode(false))
                )
        );

        assertEquals(
            "Unsupported Brigadier CommandNode for Minecraft 1.20.1 / Forge 47.4.22",
            exception.getMessage()
        );
    }

    private static byte[] commandNode(boolean includeCanUse) {
        ClassNode node = new ClassNode();
        node.version = Opcodes.V17;
        node.access = Opcodes.ACC_PUBLIC;
        node.name = OWNER;
        node.superName = "java/lang/Object";
        node.fields.add(
            new FieldNode(
                Opcodes.ACC_PRIVATE,
                "children",
                "Ljava/util/Map;",
                null,
                null
            )
        );
        node.fields.add(
            new FieldNode(
                Opcodes.ACC_PRIVATE,
                "literals",
                "Ljava/util/Map;",
                null,
                null
            )
        );
        node.fields.add(
            new FieldNode(
                Opcodes.ACC_PRIVATE,
                "arguments",
                "Ljava/util/Map;",
                null,
                null
            )
        );
        if (includeCanUse) {
            MethodNode canUse = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "canUse",
                "(Ljava/util/function/Predicate;)Z",
                null,
                null
            );
            canUse.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            canUse.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
            canUse.instructions.add(
                new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/function/Predicate",
                    "test",
                    "(Ljava/lang/Object;)Z",
                    true
                )
            );
            canUse.instructions.add(new InsnNode(Opcodes.IRETURN));
            canUse.maxStack = 2;
            canUse.maxLocals = 2;
            node.methods.add(canUse);
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
}
