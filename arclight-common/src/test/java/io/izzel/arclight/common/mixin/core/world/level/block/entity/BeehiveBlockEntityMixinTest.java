package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BeehiveBlockEntityMixinTest {

    @Test
    void nightCheckUsesChainableInjection() throws Exception {
        var classNode = new ClassNode();
        new ClassReader(BeehiveBlockEntityMixin.class.getName()).accept(classNode, ClassReader.SKIP_CODE);

        var handler = classNode.methods.stream()
            .filter(method -> method.name.equals("arclight$bypassNightCheck"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, handler.visibleAnnotations.stream()
            .filter(annotation -> annotation.desc.equals(Type.getDescriptor(ModifyExpressionValue.class)))
            .count());
        assertFalse(handler.visibleAnnotations.stream()
            .anyMatch(annotation -> annotation.desc.equals(Type.getDescriptor(Redirect.class))));
    }
}
