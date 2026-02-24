package io.izzel.arclight.common.mod.mixins;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class BukkitPaperApiPatcher {

    private static final String BUKKIT_CLASS = "org.bukkit.Bukkit";
    private static final String TITLE_CLASS = "net.kyori.adventure.title.Title";
    private static final String PLAYER_TELEPORT_EVENT_CLASS = "org.bukkit.event.player.PlayerTeleportEvent";
    private static final String CREATE_INV_COMPONENT_DESC =
            "(Lorg/bukkit/inventory/InventoryHolder;ILnet/kyori/adventure/text/Component;)Lorg/bukkit/inventory/Inventory;";
    private static final String TITLE_COMPONENTS_TICKS_DESC =
            "(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/text/Component;III)Lnet/kyori/adventure/title/Title;";
    private static final String PLAYER_TELEPORT_EVENT_CTOR_DESC =
            "(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)V";
    private static final String PLAYER_TELEPORT_EVENT_CTOR_WITH_FLAGS_DESC =
            "(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;Ljava/util/Set;)V";
    private static final String PLAYER_TELEPORT_EVENT_GET_FLAGS_DESC = "()Ljava/util/Set;";
    private static final String PLAYER_TELEPORT_EVENT_WILL_DISMOUNT_DESC = "()Z";
    private static final String PLAYER_TELEPORT_EVENT_FLAGS_FIELD = "arclight$relativeTeleportationFlags";
    private static final String PLAYER_TELEPORT_EVENT_DISMOUNTED_FIELD = "arclight$dismounted";
    private static final String SET_DESC = "Ljava/util/Set;";
    private static final String BOOLEAN_DESC = "Z";

    private BukkitPaperApiPatcher() {
    }

    public static void patch(String targetClassName, ClassNode targetClass) {
        if (BUKKIT_CLASS.equals(targetClassName)) {
            patchGetCurrentTick(targetClass);
            patchCreateInventoryComponent(targetClass);
        }
        if (TITLE_CLASS.equals(targetClassName)) {
            patchTitleTicksFactory(targetClass);
        }
        if (PLAYER_TELEPORT_EVENT_CLASS.equals(targetClassName)) {
            patchPlayerTeleportEvent(targetClass);
        }
    }

    private static void patchGetCurrentTick(ClassNode targetClass) {
        if (hasMethod(targetClass, "getCurrentTick", "()I")) {
            return;
        }

        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getCurrentTick", "()I", null, null);
        method.instructions.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                "io/izzel/arclight/common/mod/ArclightConstants",
                "currentTick",
                "I"
        ));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.maxStack = 1;
        method.maxLocals = 0;
        targetClass.methods.add(method);
    }

    private static void patchCreateInventoryComponent(ClassNode targetClass) {
        if (hasMethod(targetClass, "createInventory", CREATE_INV_COMPONENT_DESC)) {
            return;
        }

        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "createInventory",
                CREATE_INV_COMPONENT_DESC,
                null,
                new String[]{"java/lang/IllegalArgumentException"}
        );

        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "io/izzel/arclight/common/adventure/PaperAdventure",
                "adventureToLegacy",
                "(Lnet/kyori/adventure/text/Component;)Ljava/lang/String;",
                false
        ));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "org/bukkit/Bukkit",
                "createInventory",
                "(Lorg/bukkit/inventory/InventoryHolder;ILjava/lang/String;)Lorg/bukkit/inventory/Inventory;",
                false
        ));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 3;
        method.maxLocals = 3;
        targetClass.methods.add(method);
    }

    private static void patchTitleTicksFactory(ClassNode targetClass) {
        if (hasMethod(targetClass, "title", TITLE_COMPONENTS_TICKS_DESC)) {
            return;
        }

        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "title",
                TITLE_COMPONENTS_TICKS_DESC,
                null,
                null
        );

        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));

        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        method.instructions.add(new InsnNode(Opcodes.I2L));
        method.instructions.add(new LdcInsnNode(50L));
        method.instructions.add(new InsnNode(Opcodes.LMUL));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/time/Duration",
                "ofMillis",
                "(J)Ljava/time/Duration;",
                false
        ));

        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        method.instructions.add(new InsnNode(Opcodes.I2L));
        method.instructions.add(new LdcInsnNode(50L));
        method.instructions.add(new InsnNode(Opcodes.LMUL));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/time/Duration",
                "ofMillis",
                "(J)Ljava/time/Duration;",
                false
        ));

        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
        method.instructions.add(new InsnNode(Opcodes.I2L));
        method.instructions.add(new LdcInsnNode(50L));
        method.instructions.add(new InsnNode(Opcodes.LMUL));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/time/Duration",
                "ofMillis",
                "(J)Ljava/time/Duration;",
                false
        ));

        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/kyori/adventure/title/Title$Times",
                "times",
                "(Ljava/time/Duration;Ljava/time/Duration;Ljava/time/Duration;)Lnet/kyori/adventure/title/Title$Times;",
                true
        ));

        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/kyori/adventure/title/Title",
                "title",
                "(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/title/Title$Times;)Lnet/kyori/adventure/title/Title;",
                true
        ));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 8;
        method.maxLocals = 5;
        targetClass.methods.add(method);
    }

    private static void patchPlayerTeleportEvent(ClassNode targetClass) {
        patchPlayerTeleportEventField(targetClass);
        patchPlayerTeleportEventConstructors(targetClass);
        patchPlayerTeleportEventCtorWithFlags(targetClass);
        patchPlayerTeleportEventWillDismount(targetClass);
        patchPlayerTeleportEventGetFlags(targetClass);
    }

    private static void patchPlayerTeleportEventField(ClassNode targetClass) {
        if (!hasField(targetClass, PLAYER_TELEPORT_EVENT_FLAGS_FIELD, SET_DESC)) {
            targetClass.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, PLAYER_TELEPORT_EVENT_FLAGS_FIELD, SET_DESC, null, null));
        }
        if (!hasField(targetClass, PLAYER_TELEPORT_EVENT_DISMOUNTED_FIELD, BOOLEAN_DESC)) {
            targetClass.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, PLAYER_TELEPORT_EVENT_DISMOUNTED_FIELD, BOOLEAN_DESC, null, null));
        }
    }

    private static void patchPlayerTeleportEventConstructors(ClassNode targetClass) {
        String ctor3 = "(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;)V";
        for (MethodNode method : targetClass.methods) {
            if ("<init>".equals(method.name) && (ctor3.equals(method.desc) || PLAYER_TELEPORT_EVENT_CTOR_DESC.equals(method.desc))) {
                initializeFlagsField(targetClass, method, false);
            }
        }
    }

    private static void patchPlayerTeleportEventCtorWithFlags(ClassNode targetClass) {
        if (hasMethod(targetClass, "<init>", PLAYER_TELEPORT_EVENT_CTOR_WITH_FLAGS_DESC)) {
            return;
        }

        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "<init>",
                PLAYER_TELEPORT_EVENT_CTOR_WITH_FLAGS_DESC,
                null,
                null
        );

        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                targetClass.name,
                "<init>",
                PLAYER_TELEPORT_EVENT_CTOR_DESC,
                false
        ));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 5));
        method.instructions.add(new FieldInsnNode(
                Opcodes.PUTFIELD,
                targetClass.name,
                PLAYER_TELEPORT_EVENT_FLAGS_FIELD,
                SET_DESC
        ));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 6;
        method.maxLocals = 6;
        targetClass.methods.add(method);
    }

    private static void patchPlayerTeleportEventWillDismount(ClassNode targetClass) {
        if (hasMethod(targetClass, "willDismountPlayer", PLAYER_TELEPORT_EVENT_WILL_DISMOUNT_DESC)) {
            return;
        }

        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "willDismountPlayer",
                PLAYER_TELEPORT_EVENT_WILL_DISMOUNT_DESC,
                null,
                null
        );
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                targetClass.name,
                PLAYER_TELEPORT_EVENT_DISMOUNTED_FIELD,
                BOOLEAN_DESC
        ));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.maxStack = 1;
        method.maxLocals = 1;
        targetClass.methods.add(method);
    }

    private static void patchPlayerTeleportEventGetFlags(ClassNode targetClass) {
        if (hasMethod(targetClass, "getRelativeTeleportationFlags", PLAYER_TELEPORT_EVENT_GET_FLAGS_DESC)) {
            return;
        }

        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "getRelativeTeleportationFlags",
                PLAYER_TELEPORT_EVENT_GET_FLAGS_DESC,
                null,
                null
        );
        LabelNode nonNull = new LabelNode();
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                targetClass.name,
                PLAYER_TELEPORT_EVENT_FLAGS_FIELD,
                SET_DESC
        ));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, nonNull));
        method.instructions.add(new InsnNode(Opcodes.POP));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/util/Collections",
                "emptySet",
                "()Ljava/util/Set;",
                false
        ));
        method.instructions.add(nonNull);
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 2;
        method.maxLocals = 1;
        targetClass.methods.add(method);
    }

    private static void initializeFlagsField(ClassNode targetClass, MethodNode constructor, boolean useCtorArgSet) {
        for (var insn = constructor.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == Opcodes.RETURN) {
                constructor.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                constructor.instructions.insertBefore(insn, new InsnNode(Opcodes.ICONST_1));
                constructor.instructions.insertBefore(insn, new FieldInsnNode(
                        Opcodes.PUTFIELD,
                        targetClass.name,
                        PLAYER_TELEPORT_EVENT_DISMOUNTED_FIELD,
                        BOOLEAN_DESC
                ));
                constructor.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                if (useCtorArgSet) {
                    constructor.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 5));
                } else {
                    constructor.instructions.insertBefore(insn, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/util/Collections",
                            "emptySet",
                            "()Ljava/util/Set;",
                            false
                    ));
                }
                constructor.instructions.insertBefore(insn, new FieldInsnNode(
                        Opcodes.PUTFIELD,
                        targetClass.name,
                        PLAYER_TELEPORT_EVENT_FLAGS_FIELD,
                        SET_DESC
                ));
                constructor.maxStack = Math.max(constructor.maxStack, 2);
                break;
            }
        }
    }

    private static boolean hasMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (name.equals(method.name) && desc.equals(method.desc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasField(ClassNode classNode, String name, String desc) {
        for (FieldNode field : classNode.fields) {
            if (name.equals(field.name) && desc.equals(field.desc)) {
                return true;
            }
        }
        return false;
    }
}
