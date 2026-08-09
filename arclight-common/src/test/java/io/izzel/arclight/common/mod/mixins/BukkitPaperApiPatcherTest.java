package io.izzel.arclight.common.mod.mixins;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPaperApiPatcherTest {

    @Test
    void injectsMinecraftVersionExactlyOnce() {
        ClassNode bukkit = new ClassNode();
        bukkit.name = "org/bukkit/Bukkit";

        BukkitPaperApiPatcher.patch("org.bukkit.Bukkit", bukkit);
        BukkitPaperApiPatcher.patch("org.bukkit.Bukkit", bukkit);

        var methods = bukkit.methods.stream()
            .filter(method -> method.name.equals("getMinecraftVersion") && method.desc.equals("()Ljava/lang/String;"))
            .toList();
        assertEquals(1, methods.size());
        assertTrue(methods.getFirst().instructions.iterator().next() instanceof MethodInsnNode call
            && call.getOpcode() == Opcodes.INVOKESTATIC
            && call.owner.equals("org/bukkit/Bukkit")
            && call.name.equals("getServer"));
    }

    @Test
    void injectsTeleportFlagsExactlyOnce() {
        ClassNode event = new ClassNode();
        event.name = "org/bukkit/event/player/PlayerTeleportEvent";
        event.methods.add(new MethodNode(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)V",
            null,
            null
        ));

        BukkitPaperApiPatcher.patch(event.name.replace('/', '.'), event);
        BukkitPaperApiPatcher.patch(event.name.replace('/', '.'), event);

        assertEquals(1, event.fields.stream().filter(field -> field.name.equals("arclight$relativeTeleportationFlags")).count());
        assertEquals(1, event.fields.stream().filter(field -> field.name.equals("arclight$dismounted")).count());
        assertEquals(1, event.methods.stream().filter(method -> method.name.equals("getRelativeTeleportationFlags")).count());
        assertEquals(1, event.methods.stream().filter(method -> method.name.equals("willDismountPlayer")).count());
    }
}
