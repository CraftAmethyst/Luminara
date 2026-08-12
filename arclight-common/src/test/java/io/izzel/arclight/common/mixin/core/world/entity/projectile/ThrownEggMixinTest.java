package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.animal.Chicken;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThrownEggMixinTest {

    @Test
    void chickenHatchRetainsVanillaMoveCallOwner() throws Exception {
        var classNode = new ClassNode();
        new ClassReader(ThrownEggMixin.class.getName()).accept(classNode, 0);

        var onHit = classNode.methods.stream()
            .filter(method -> method.name.equals("onHit"))
            .findFirst()
            .orElseThrow();
        var chickenOwner = Type.getInternalName(Chicken.class);

        var found = false;
        for (var instruction : onHit.instructions) {
            if (instruction instanceof MethodInsnNode method
                && method.owner.equals(chickenOwner)
                && method.name.equals("moveTo")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
