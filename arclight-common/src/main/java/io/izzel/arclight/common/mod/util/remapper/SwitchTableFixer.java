package io.izzel.arclight.common.mod.util.remapper;

import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Function;

/**
 * Repairs the compiler generated {@code $SwitchMap$} tables that switch over the Bukkit
 * enums Luminara extends.
 * <p>
 * javac builds those tables from the constants that existed at compile time, so a switch
 * over a value Luminara adds later would fall through to {@code default}. The launcher ran
 * this as a launch plugin transformer; plugin classes still need it, so it now lives in
 * the module and is applied by the plugin remapper instead of being loaded reflectively
 * from the deleted launcher package.
 */
public final class SwitchTableFixer
    implements Function<byte[], byte[]> {

    public static final SwitchTableFixer INSTANCE = new SwitchTableFixer();

    private static final Set<String> ENUMS = EnumValues.ENUMS;

    private SwitchTableFixer() {
    }

    /** Variant where the table is refilled in place. */
    public static int[] fillSwitchTable1(
        int[] arr,
        Class<? extends Enum<?>> cl
    ) {
        Enum<?>[] enums = cl.getEnumConstants();
        if (arr.length < enums.length) {
            int[] ints = new int[enums.length];
            System.arraycopy(arr, 0, ints, 0, arr.length);
            arr = ints;
        }
        int highest = -1;
        for (int value : arr) {
            if (value > highest) highest = value;
        }
        if (highest != -1) {
            for (int index = highest; index < enums.length; index++) {
                arr[index] = enums[index].ordinal();
            }
        }
        return arr;
    }

    /** Variant where the table was already sized by the caller. */
    public static int[] fillSwitchTable2(
        int[] arr,
        Class<? extends Enum<?>> cl
    ) {
        Enum<?>[] enums = cl.getEnumConstants();
        if (arr.length < enums.length) {
            int[] ints = new int[enums.length];
            System.arraycopy(arr, 0, ints, 0, arr.length);
            arr = ints;
        }
        return arr;
    }

    @Override
    public byte[] apply(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        processClass(node);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public boolean processClass(ClassNode node) {
        boolean success = false;
        for (MethodNode method : node.methods) {
            if (inject1(node, method)) {
                success = true;
            } else if (inject2(node, method)) {
                success = true;
            }
        }
        return success;
    }

    private boolean inject1(ClassNode node, MethodNode method) {
        if (
            !Modifier.isStatic(method.access) ||
                (method.access & Opcodes.ACC_SYNTHETIC) == 0 ||
                !method.desc.equals("()[I")
        ) {
            return false;
        }
        boolean foundTryCatch = false;
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            if ("java/lang/NoSuchFieldError".equals(tryCatchBlock.type)) {
                foundTryCatch = true;
            } else {
                return false;
            }
        }
        if (!foundTryCatch) return false;

        FieldInsnNode fieldInsnNode = null;
        String enumType = null;
        for (AbstractInsnNode insnNode : method.instructions) {
            if (enumType != null) break;
            if (
                insnNode.getOpcode() == Opcodes.GETSTATIC &&
                    ((FieldInsnNode) insnNode).desc.equals("[I")
            ) {
                fieldInsnNode = (FieldInsnNode) insnNode;
            }
            if (
                insnNode.getOpcode() == Opcodes.INVOKESTATIC &&
                    ((MethodInsnNode) insnNode).name.equals("values")
            ) {
                enumType = enumValuesType((MethodInsnNode) insnNode);
            }
        }
        if (fieldInsnNode == null || enumType == null) return false;

        AbstractInsnNode last = method.instructions.getLast();
        while (last != null && last.getOpcode() != Opcodes.ARETURN) {
            last = last.getPrevious();
        }
        if (last == null) return false;

        InsnList list = new InsnList();
        list.add(new LdcInsnNode(Type.getObjectType(enumType)));
        list.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(SwitchTableFixer.class),
                "fillSwitchTable1",
                "([ILjava/lang/Class;)[I",
                false
            )
        );
        list.add(new InsnNode(Opcodes.DUP));
        list.add(
            new FieldInsnNode(
                Opcodes.PUTSTATIC,
                fieldInsnNode.owner,
                fieldInsnNode.name,
                fieldInsnNode.desc
            )
        );
        method.instructions.insertBefore(last, list);
        return true;
    }

    private boolean inject2(ClassNode node, MethodNode method) {
        if ((node.access & Opcodes.ACC_SYNTHETIC) == 0) return false;
        if (
            node.methods.size() != 1 ||
                !Modifier.isStatic(method.access) ||
                !method.name.equals("<clinit>")
        ) {
            return false;
        }
        boolean foundTryCatch = false;
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            if ("java/lang/NoSuchFieldError".equals(tryCatchBlock.type)) {
                foundTryCatch = true;
            } else {
                return false;
            }
        }
        if (!foundTryCatch) return false;

        FieldInsnNode fieldInsnNode = null;
        String enumType = null;
        for (AbstractInsnNode insnNode : method.instructions) {
            if (
                insnNode.getOpcode() != Opcodes.INVOKESTATIC ||
                    !((MethodInsnNode) insnNode).name.equals("values")
            ) {
                continue;
            }
            enumType = enumValuesType((MethodInsnNode) insnNode);
            if (enumType == null) continue;
            AbstractInsnNode next = insnNode.getNext();
            if (next == null || next.getOpcode() != Opcodes.ARRAYLENGTH) continue;
            AbstractInsnNode newArray = next.getNext();
            if (
                newArray == null ||
                    newArray.getOpcode() != Opcodes.NEWARRAY ||
                    ((IntInsnNode) newArray).operand != Opcodes.T_INT
            ) {
                continue;
            }
            AbstractInsnNode putStatic = newArray.getNext();
            if (
                putStatic != null &&
                    putStatic.getOpcode() == Opcodes.PUTSTATIC &&
                    ((FieldInsnNode) putStatic).desc.equals("[I")
            ) {
                fieldInsnNode = (FieldInsnNode) putStatic;
                break;
            }
        }
        if (fieldInsnNode == null) return false;

        AbstractInsnNode last = method.instructions.getLast();
        while (last != null && last.getOpcode() != Opcodes.RETURN) {
            last = last.getPrevious();
        }
        if (last == null) return false;

        InsnList list = new InsnList();
        list.add(
            new FieldInsnNode(
                Opcodes.GETSTATIC,
                fieldInsnNode.owner,
                fieldInsnNode.name,
                fieldInsnNode.desc
            )
        );
        list.add(new LdcInsnNode(Type.getObjectType(enumType)));
        list.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(SwitchTableFixer.class),
                "fillSwitchTable2",
                "([ILjava/lang/Class;)[I",
                false
            )
        );
        list.add(
            new FieldInsnNode(
                Opcodes.PUTSTATIC,
                fieldInsnNode.owner,
                fieldInsnNode.name,
                fieldInsnNode.desc
            )
        );
        method.instructions.insertBefore(last, list);
        return true;
    }

    private static String enumValuesType(MethodInsnNode invoke) {
        Type returnType = Type.getMethodType(invoke.desc).getReturnType();
        if (
            returnType.getSort() != Type.ARRAY ||
                returnType.getDimensions() != 1
        ) {
            return null;
        }
        String element = returnType.getElementType().getInternalName();
        return ENUMS.contains(element) ? element : null;
    }

    /** Holder so the enum set is initialized once per class loader. */
    private static final class EnumValues {

        static final Set<String> ENUMS = Set.of(
            "org/bukkit/Material",
            "org/bukkit/potion/PotionType",
            "org/bukkit/entity/EntityType",
            "org/bukkit/entity/Villager$Profession",
            "org/bukkit/block/Biome",
            "org/bukkit/Art",
            "org/bukkit/Statistic",
            "org/bukkit/inventory/CreativeCategory",
            "org/bukkit/entity/SpawnCategory",
            "org/bukkit/entity/EnderDragon$Phase",
            "org/bukkit/inventory/recipe/CookingBookCategory",
            "org/bukkit/Fluid",
            "org/bukkit/entity/Spellcaster$Spell",
            "org/bukkit/entity/Pose"
        );
    }
}
