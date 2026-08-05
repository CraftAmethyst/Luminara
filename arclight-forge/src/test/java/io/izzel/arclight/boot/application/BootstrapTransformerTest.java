package io.izzel.arclight.boot.application;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class BootstrapTransformerTest {

    @Test
    void transformsPinnedBootstrapLauncher() throws Exception {
        try (
            var input = getClass()
                .getClassLoader()
                .getResourceAsStream(
                    "cpw/mods/bootstraplauncher/BootstrapLauncher.class"
                )
        ) {
            assertNotNull(
                input,
                "BootstrapLauncher 1.1.2 fixture must be on the test runtime classpath"
            );
            byte[] transformed = new BootstrapTransformer(
                getClass().getClassLoader()
            ).transformBootstrapLauncher(input);
            ClassNode node = new ClassNode();
            new ClassReader(transformed).accept(node, 0);

            var main = node.methods
                .stream()
                .filter(
                    method ->
                        method.name.equals("main") &&
                            method.desc.equals("([Ljava/lang/String;)V")
                )
                .findFirst()
                .orElseThrow();
            int arclightInvocations = 0;
            int consumerInvocations = 0;
            for (var instruction : main.instructions) {
                if (!(instruction instanceof MethodInsnNode invoke)) continue;
                if (
                    invoke.getOpcode() == Opcodes.INVOKESTATIC &&
                        invoke.owner.equals(
                            "io/izzel/arclight/boot/application/BootstrapTransformer"
                        ) &&
                        invoke.name.equals("onInvoke$BootstrapLauncher") &&
                        invoke.desc.equals(
                            "([Ljava/lang/String;Lcpw/mods/cl/ModuleClassLoader;)V"
                        )
                ) {
                    arclightInvocations++;
                }
                if (
                    invoke.owner.equals("java/util/function/Consumer") &&
                        invoke.name.equals("accept") &&
                        invoke.desc.equals("(Ljava/lang/Object;)V")
                ) {
                    consumerInvocations++;
                }
            }
            assertEquals(1, arclightInvocations);
            assertEquals(0, consumerInvocations);
        }
    }

    @Test
    void rejectsUnknownBootstrapLauncherFingerprint() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () ->
                new BootstrapTransformer(
                    getClass().getClassLoader()
                ).transformBootstrapLauncher(
                    new ByteArrayInputStream(new byte[]{0})
                )
        );

        assertEquals(
            "Unsupported BootstrapLauncher for Minecraft 1.20.1 / Forge 47.4.22",
            exception.getMessage()
        );
    }
}
