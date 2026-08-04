package io.izzel.arclight.boot.asm;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class BukkitApiImplementer implements Implementer {

    private static final Marker MARKER = MarkerManager.getMarker("BUKKIT_API");
    private static final String BUKKIT = "org/bukkit/Bukkit";
    private static final String SUPPORT =
        "io/izzel/arclight/boot/asm/BukkitApiSupport";

    private static boolean hasMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    private static void addGetMinecraftVersion(ClassNode node) {
        MethodNode method = new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getMinecraftVersion",
            "()Ljava/lang/String;",
            null,
            null
        );
        method.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                BUKKIT,
                "getServer",
                "()Lorg/bukkit/Server;",
                false
            )
        );
        method.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SUPPORT,
                "getMinecraftVersion",
                "(Ljava/lang/Object;)Ljava/lang/String;",
                false
            )
        );
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        node.methods.add(method);
    }

    private static void addGetCommandMap(ClassNode node) {
        MethodNode method = new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getCommandMap",
            "()Lorg/bukkit/command/CommandMap;",
            null,
            null
        );
        method.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                BUKKIT,
                "getServer",
                "()Lorg/bukkit/Server;",
                false
            )
        );
        method.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SUPPORT,
                "getCommandMap",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false
            )
        );
        method.instructions.add(
            new TypeInsnNode(Opcodes.CHECKCAST, "org/bukkit/command/CommandMap")
        );
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        node.methods.add(method);
    }

    @Override
    public boolean processClass(
        ClassNode node,
        ILaunchPluginService.ITransformerLoader transformerLoader
    ) {
        if (!BUKKIT.equals(node.name)) {
            return false;
        }
        boolean changed = false;
        if (!hasMethod(node, "getMinecraftVersion", "()Ljava/lang/String;")) {
            addGetMinecraftVersion(node);
            changed = true;
        }
        if (
            !hasMethod(
                node,
                "getCommandMap",
                "()Lorg/bukkit/command/CommandMap;"
            )
        ) {
            addGetCommandMap(node);
            changed = true;
        }
        if (changed) {
            ArclightImplementer.LOGGER.debug(
                MARKER,
                "Injected missing Bukkit API methods into {}",
                node.name
            );
        }
        return changed;
    }
}
