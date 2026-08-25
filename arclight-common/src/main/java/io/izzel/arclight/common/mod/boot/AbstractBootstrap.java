package io.izzel.arclight.common.mod.boot;

import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.i18n.ArclightLocale;
import io.izzel.arclight.i18n.LuminaraVersion;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Runtime bootstrap utilities shared by the standalone mod entry points.
 * <p>
 * These used to live in the custom launcher ({@code bootstrap}) module and were
 * applied by the launch chain. As a standard mod both loaders now invoke the same
 * logic from their own entry points. Bytecode-level transformation parity
 * (CommandNode / Gson enum / ASM implementers) is a later-phase verification item.
 */
public interface AbstractBootstrap {

    /**
     * This runs the CommandNode hook hack and Gson enum hack.
     * A friendly version for CommandNode hook hack can be found in Sinytra Connector support,
     * which uses Mixin to do the same thing.
     *
     * @see io.izzel.arclight.neoforge.mixin.compat.connector.CommandNodeMixin
     * @throws Exception the injection fails due to various reasons and cannot proceed to boot.
     */
    @SuppressWarnings("JavadocReference")
    default void dirtyHacks() throws Exception {
        installGsonEnumFactory();
        installCommandNodeHack();
    }

    default void installGsonEnumFactory() {
        TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(Object.class));
        Field field;
        try {
            field = TypeAdapters.class.getDeclaredField("ENUM_FACTORY");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        Object base = Unsafe.staticFieldBase(field);
        long offset = Unsafe.staticFieldOffset(field);
        Unsafe.putObjectVolatile(base, offset, new EnumTypeFactory());
    }

    default void installCommandNodeHack() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("com/mojang/brigadier/tree/CommandNode.class")) {
            var node = new ClassNode();
            new ClassReader(in).accept(node, 0);
            {
                FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE, "CURRENT_COMMAND", "Lcom/mojang/brigadier/tree/CommandNode;", null, null);
                node.fields.add(fieldNode);
                for (var method : node.methods) {
                    if (method.name.equals("canUse")) {
                        for (var instruction : method.instructions) {
                            if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE || instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                var assign = new InsnList();
                                assign.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                assign.add(new FieldInsnNode(Opcodes.PUTSTATIC, "com/mojang/brigadier/tree/CommandNode", fieldNode.name, fieldNode.desc));
                                method.instructions.insertBefore(instruction, assign);
                                var reset = new InsnList();
                                reset.add(new InsnNode(Opcodes.ACONST_NULL));
                                reset.add(new FieldInsnNode(Opcodes.PUTSTATIC, "com/mojang/brigadier/tree/CommandNode", fieldNode.name, fieldNode.desc));
                                method.instructions.insert(instruction, reset);
                                break;
                            }
                        }
                    }
                }
            }
            {
                var removeCommand = new MethodNode();
                removeCommand.access = Opcodes.ACC_PUBLIC;
                removeCommand.name = "removeCommand";
                removeCommand.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "children", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "literals", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                removeCommand.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/brigadier/tree/CommandNode", "arguments", Type.getDescriptor(Map.class)));
                removeCommand.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                removeCommand.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
                removeCommand.instructions.add(new InsnNode(Opcodes.RETURN));
                node.methods.add(removeCommand);
            }
            var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(cw);
            byte[] bytes = cw.toByteArray();
            Unsafe.defineClass("com.mojang.brigadier.tree.CommandNode", bytes, 0, bytes.length, getClass().getClassLoader(), getClass().getProtectionDomain());
        }
    }

    default void setupMod(ArclightPlatform platform) throws Exception {
        setVersionIfAbsent(ArclightVersion.FEUDAL_KINGS);
        setPlatformIfAbsent(platform);
        setVersionPropertyIfAbsent();
        try (InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (stream == null) {
                return;
            }
            Manifest manifest = new Manifest(stream);
            Attributes attributes = manifest.getMainAttributes();
            String version = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            String buildTime = attributes.getValue("Implementation-Timestamp");
            LogManager.getLogger("Luminara").info(ArclightLocale.getInstance().get("logo"),
                ArclightLocale.getInstance().get("release-name." + ArclightVersion.current().getReleaseName()), version, buildTime);
        }
    }

    static void setVersionIfAbsent(ArclightVersion version) {
        try {
            ArclightVersion.current();
        } catch (IllegalStateException ignored) {
            ArclightVersion.setVersion(version);
        }
    }

    /**
     * {@code CraftServer#getVersion} reports this property, and the legacy launcher published it
     * from its own manifest during boot. A standalone mod has to publish it itself, otherwise
     * Bukkit reports {@code Arclight version null}.
     */
    static void setVersionPropertyIfAbsent() {
        if (System.getProperty("arclight.version") == null) {
            System.setProperty("arclight.version", LuminaraVersion.version());
        }
    }

    static void setPlatformIfAbsent(ArclightPlatform platform) {
        try {
            ArclightPlatform.current();
        } catch (IllegalStateException ignored) {
            ArclightPlatform.setPlatform(platform);
        }
    }

    static ArclightPlatform detectPlatform() {
        try {
            Class.forName("net.neoforged.fml.loading.FMLLoader");
            return ArclightPlatform.NEOFORGE;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return ArclightPlatform.FABRIC;
        } catch (ClassNotFoundException ignored) {
        }
        return ArclightPlatform.VANILLA;
    }
}