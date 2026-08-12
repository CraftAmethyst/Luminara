package io.izzel.arclight.common.mixin.core.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AnvilMenuMixinTest {

    @Test
    void repairCostModifiersUseChainableInjection() throws IOException {
        var classNode = new ClassNode();
        new ClassReader(AnvilMenuMixin.class.getName()).accept(classNode, ClassReader.SKIP_CODE);

        var handlers = classNode.methods.stream()
            .filter(method -> method.name.startsWith("arclight$maximumRepairCost"))
            .toList();

        assertEquals(2, handlers.size());
        for (var handler : handlers) {
            assertEquals(1, annotations(handler, Type.getDescriptor(ModifyExpressionValue.class)).size());
            assertFalse(hasAnnotation(handler, Type.getDescriptor(ModifyConstant.class)));
        }
    }

    private static boolean hasAnnotation(MethodNode method, String descriptor) {
        return !annotations(method, descriptor).isEmpty();
    }

    private static List<AnnotationNode> annotations(MethodNode method, String descriptor) {
        if (method.visibleAnnotations == null) {
            return List.of();
        }
        return method.visibleAnnotations.stream()
            .filter(annotation -> annotation.desc.equals(descriptor))
            .toList();
    }
}
