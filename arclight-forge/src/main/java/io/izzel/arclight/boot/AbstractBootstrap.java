package io.izzel.arclight.boot;

import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.i18n.ArclightLocale;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class AbstractBootstrap {

    private static boolean dirtyHacksApplied;
    private static boolean setupModApplied;

    static byte[] transformCommandNode(InputStream input) throws Exception {
        if (input == null) throw compatibilityFailure();
        var node = new ClassNode();
        new ClassReader(input).accept(node, 0);
        final String owner = "com/mojang/brigadier/tree/CommandNode";
        final String descriptor = "Lcom/mojang/brigadier/tree/CommandNode;";
        boolean hasCurrentCommand = node.fields
            .stream()
            .anyMatch(
                field ->
                    field.name.equals("CURRENT_COMMAND") &&
                        field.desc.equals(descriptor)
            );
        boolean invocationFound = false;
        for (var method : node.methods) {
            if (!method.name.equals("canUse")) continue;
            for (var instruction : method.instructions) {
                if (
                    instruction instanceof MethodInsnNode invocation &&
                        invocation.owner.equals("java/util/function/Predicate") &&
                        invocation.name.equals("test") &&
                        invocation.desc.equals("(Ljava/lang/Object;)Z")
                ) {
                    invocationFound = true;
                    if (!hasCurrentCommand) {
                        var fieldNode = new FieldNode(
                            Opcodes.ACC_PUBLIC |
                                Opcodes.ACC_STATIC |
                                Opcodes.ACC_VOLATILE,
                            "CURRENT_COMMAND",
                            descriptor,
                            null,
                            null
                        );
                        node.fields.add(fieldNode);
                        var assign = new InsnList();
                        assign.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        assign.add(
                            new FieldInsnNode(
                                Opcodes.PUTSTATIC,
                                owner,
                                fieldNode.name,
                                fieldNode.desc
                            )
                        );
                        method.instructions.insertBefore(instruction, assign);
                        var reset = new InsnList();
                        reset.add(new InsnNode(Opcodes.ACONST_NULL));
                        reset.add(
                            new FieldInsnNode(
                                Opcodes.PUTSTATIC,
                                owner,
                                fieldNode.name,
                                fieldNode.desc
                            )
                        );
                        method.instructions.insert(instruction, reset);
                    }
                    break;
                }
            }
        }
        if (!invocationFound) throw compatibilityFailure();
        if (
            node.methods
                .stream()
                .noneMatch(
                    method ->
                        method.name.equals("removeCommand") &&
                            method.desc.equals("(Ljava/lang/String;)V")
                )
        ) {
            var removeCommand = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "removeCommand",
                "(Ljava/lang/String;)V",
                null,
                null
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            removeCommand.instructions.add(
                new FieldInsnNode(
                    Opcodes.GETFIELD,
                    owner,
                    "children",
                    Type.getDescriptor(Map.class)
                )
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            removeCommand.instructions.add(
                new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Map.class),
                    "remove",
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    true
                )
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            removeCommand.instructions.add(
                new FieldInsnNode(
                    Opcodes.GETFIELD,
                    owner,
                    "literals",
                    Type.getDescriptor(Map.class)
                )
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            removeCommand.instructions.add(
                new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Map.class),
                    "remove",
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    true
                )
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            removeCommand.instructions.add(
                new FieldInsnNode(
                    Opcodes.GETFIELD,
                    owner,
                    "arguments",
                    Type.getDescriptor(Map.class)
                )
            );
            removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            removeCommand.instructions.add(
                new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(Map.class),
                    "remove",
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    true
                )
            );
            removeCommand.instructions.add(new InsnNode(Opcodes.RETURN));
            node.methods.add(removeCommand);
        }
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static IllegalStateException compatibilityFailure() {
        return new IllegalStateException(
            "Unsupported Brigadier CommandNode for Minecraft 1.20.1 / Forge 47.4.22"
        );
    }

    protected final void bootstrap() throws Exception {
        setupMod();
        dirtyHacks();
    }

    protected final void dirtyHacks() throws Exception {
        synchronized (AbstractBootstrap.class) {
            if (dirtyHacksApplied) return;
            TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(Object.class));
            Field field = TypeAdapters.class.getDeclaredField("ENUM_FACTORY");
            Object base = Unsafe.staticFieldBase(field);
            long offset = Unsafe.staticFieldOffset(field);
            Unsafe.putObjectVolatile(base, offset, new EnumTypeFactory());
            try (
                InputStream in = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                        "com/mojang/brigadier/tree/CommandNode.class"
                    )
            ) {
                byte[] bytes = transformCommandNode(in);
                Unsafe.defineClass(
                    "com.mojang.brigadier.tree.CommandNode",
                    bytes,
                    0,
                    bytes.length,
                    IModLocator.class.getClassLoader(),
                    getClass().getProtectionDomain()
                );
            }
            dirtyHacksApplied = true;
        }
    }

    protected final void setupMod() throws Exception {
        synchronized (AbstractBootstrap.class) {
            if (setupModApplied) return;
            ArclightVersion.setVersion(ArclightVersion.TRIALS);
            var logger = LogManager.getLogger("Luminara");
            try (
                InputStream stream = getClass()
                    .getModule()
                    .getResourceAsStream("/META-INF/MANIFEST.MF")
            ) {
                if (stream == null) throw new IllegalStateException(
                    "Missing Luminara manifest"
                );
                Manifest manifest = new Manifest(stream);
                Attributes attributes = manifest.getMainAttributes();
                String version = attributes.getValue(
                    Attributes.Name.IMPLEMENTATION_VERSION
                );
                extract(
                    getClass().getModule().getResourceAsStream("/common.jar"),
                    version
                );
                logger.info(
                    ArclightLocale.getInstance().get("logo"),
                    ArclightLocale.getInstance().get(
                        "release-name." +
                            ArclightVersion.current().getReleaseName()
                    ),
                    version,
                    version
                );
            }
            setupModApplied = true;
        }
    }

    private void extract(InputStream path, String version) throws Exception {
        System.setProperty("arclight.version", version);
        var dir = Paths.get(".arclight", "mod_file");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        var mod = dir.resolve(version + ".jar");
        if (
            !Files.exists(mod) || Boolean.getBoolean("arclight.alwaysExtract")
        ) {
            for (Path old : Files.list(dir).toList()) {
                Files.delete(old);
            }
            Files.copy(path, mod);
        }
    }
}
