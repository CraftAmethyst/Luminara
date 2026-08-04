package io.izzel.arclight.common.mod.mixins;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ShouldApplyProcessor {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");
    private static final List<Predicate<ClassNode>> PREDICATES = List.of(
        LoadIfModProcessor::shouldApply
    );

    public static boolean shouldApply(String mixinClass) {
        try (
            var stream =
                LoadIfModProcessor.class.getClassLoader().getResourceAsStream(
                    mixinClass.replace('.', '/') + ".class"
                )
        ) {
            if (stream != null) {
                var bytes = stream.readAllBytes();
                var cr = new ClassReader(bytes);
                var node = new ClassNode();
                cr.accept(node, ClassReader.SKIP_CODE);
                for (var predicate : PREDICATES) {
                    if (!predicate.test(node)) {
                        return false;
                    }
                }
                return true;
            } else {
                LOGGER.debug(mixinClass);
            }
            return true;
        } catch (IOException e) {
            return true;
        }
    }
}
