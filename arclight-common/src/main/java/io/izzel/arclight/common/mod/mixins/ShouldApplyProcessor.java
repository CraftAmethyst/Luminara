package io.izzel.arclight.common.mod.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ShouldApplyProcessor {

    private static final Logger LOGGER = LogManager.getLogger("Luminara");
    private static final boolean FABRIC = isClassPresent("net.fabricmc.loader.api.FabricLoader");
    private static final String MIXIN_ROOT = "io/izzel/arclight/common/mixin/";
    private static final String MIXIN_ROOT_DOT = "io.izzel.arclight.common.mixin.";
    private static final List<String> FABRIC_BLOCKED_INTERNAL_PREFIX = List.of(
            "net/minecraftforge/",
            "cpw/mods/"
    );
    private static final Set<String> FABRIC_EXCLUDES = Set.of(
            "io.izzel.arclight.common.mixin.core.network.chat.StyleMixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.WitherSkullBlockMixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.BlockMixin",
            "io.izzel.arclight.common.mixin.core.world.entity.EntityMixin_FabricBridge",
            "io.izzel.arclight.common.mixin.core.world.entity.LivingEntityMixin_FabricBridge",
            "io.izzel.arclight.common.mixin.core.world.entity.MobMixin_FabricBridge",
            "io.izzel.arclight.common.mixin.core.world.entity.player.ServerPlayerMixin_FabricBridge",
            "io.izzel.arclight.common.mixin.core.world.entity.projectile.ThrowableProjectileMixin",
            "io.izzel.arclight.common.mixin.core.world.entity.projectile.ThrowableItemProjectileMixin",
            "io.izzel.arclight.common.mixin.core.server.level.ServerChunkCache_MainThreadExecutorMixin",
            "io.izzel.arclight.common.mixin.core.commands.CommandSource1Mixin",
            "io.izzel.arclight.common.mixin.core.world.entity.animal.Sheep1Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.CartographyContainer1Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.CartographyContainer2Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.EnchantmentContainer1Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.GrindstoneContainer1Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.LoomContainer1Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.LoomContainer2Mixin",
            "io.izzel.arclight.common.mixin.core.world.inventory.StonecutterContainer1Mixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.ChestBlock2_1Mixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.ComposterBlock_InputContainerMixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.entity.CommandBlockTileEntity1Mixin",
            "io.izzel.arclight.common.mixin.core.world.level.block.entity.LecternTileEntity1Mixin",
            "io.izzel.arclight.common.mixin.core.world.level.chunk.LevelChunk_BoundTickingBlockEntityMixin"
    );
    private static final List<Predicate<ClassNode>> PREDICATES = List.of(
            LoadIfModProcessor::shouldApply
    );
    private static final Map<String, Boolean> DECISION_CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Set<String>> EVALUATING = ThreadLocal.withInitial(HashSet::new);

    public static boolean shouldApply(String mixinClass) {
        var cached = DECISION_CACHE.get(mixinClass);
        if (cached != null) {
            return cached;
        }
        var evaluating = EVALUATING.get();
        if (!evaluating.add(mixinClass)) {
            LOGGER.warn("Detected recursive shouldApply evaluation for {}, allowing by default", mixinClass);
            return true;
        }
        try {
            boolean decision = computeShouldApply(mixinClass);
            DECISION_CACHE.put(mixinClass, decision);
            return decision;
        } finally {
            evaluating.remove(mixinClass);
            if (evaluating.isEmpty()) {
                EVALUATING.remove();
            }
        }
    }

    private static boolean computeShouldApply(String mixinClass) {
        try (var stream = LoadIfModProcessor.class.getClassLoader().getResourceAsStream(mixinClass.replace('.', '/') + ".class")) {
            if (stream != null) {
                var cr = new ClassReader(stream);
                var node = new ClassNode();
                cr.accept(node, ClassReader.SKIP_FRAMES);
                if (FABRIC) {
                    if (mixinClass.endsWith("ServerChunkCache_MainThreadExecutorMixin")) {
                        LOGGER.info("Skipping Fabric-incompatible mixin {}", mixinClass);
                        return false;
                    }
                    if (FABRIC_EXCLUDES.contains(mixinClass)) {
                        LOGGER.info("Skipping Fabric-incompatible mixin {}", mixinClass);
                        return false;
                    }
                    var parent = node.superName;
                    if (parent != null && parent.startsWith(MIXIN_ROOT)) {
                        var parentMixin = parent.replace('/', '.');
                        if (!parentMixin.equals(mixinClass) && parentMixin.startsWith(MIXIN_ROOT_DOT) && !shouldApply(parentMixin)) {
                            LOGGER.info("Skipping mixin {} because parent mixin {} is disabled on Fabric", mixinClass, parentMixin);
                            return false;
                        }
                    }
                    if (node.superName != null && node.superName.startsWith("net/minecraftforge/")) {
                        LOGGER.info("Skipping Forge-super mixin {} -> {}", mixinClass, node.superName);
                        return false;
                    }
                    var reason = findFabricBlockedReference(node);
                    if (reason != null) {
                        LOGGER.info("Skipping Forge-linked mixin {} ({})", mixinClass, reason);
                        return false;
                    }
                }
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

    private static String findFabricBlockedReference(ClassNode node) {
        var classRef = firstBlockedRef(node.name);
        if (classRef != null) {
            return "class " + classRef;
        }
        var superRef = firstBlockedRef(node.superName);
        if (superRef != null) {
            return "super " + superRef;
        }
        if (node.interfaces != null) {
            for (String iface : node.interfaces) {
                var ifaceRef = firstBlockedRef(iface);
                if (ifaceRef != null) {
                    return "interface " + ifaceRef;
                }
            }
        }
        var sigRef = firstBlockedRef(node.signature);
        if (sigRef != null) {
            return "signature " + sigRef;
        }
        var outerClassRef = firstBlockedRef(node.outerClass);
        if (outerClassRef != null) {
            return "outer class " + outerClassRef;
        }
        var outerDescRef = firstBlockedRef(node.outerMethodDesc);
        if (outerDescRef != null) {
            return "outer method desc " + outerDescRef;
        }
        var annRef = firstBlockedInAnnotations(node.visibleAnnotations);
        if (annRef == null) {
            annRef = firstBlockedInAnnotations(node.invisibleAnnotations);
        }
        if (annRef != null) {
            return "annotation " + annRef;
        }
        if (node.innerClasses != null) {
            for (InnerClassNode inner : node.innerClasses) {
                var nameRef = firstBlockedRef(inner.name);
                if (nameRef != null) {
                    return "inner class " + nameRef;
                }
                var outerRef = firstBlockedRef(inner.outerName);
                if (outerRef != null) {
                    return "inner outer " + outerRef;
                }
            }
        }
        if (node.fields != null) {
            for (FieldNode field : node.fields) {
                var fieldDescRef = firstBlockedRef(field.desc);
                if (fieldDescRef != null) {
                    return "field desc " + fieldDescRef;
                }
                var fieldSigRef = firstBlockedRef(field.signature);
                if (fieldSigRef != null) {
                    return "field signature " + fieldSigRef;
                }
                var fieldAnnRef = firstBlockedInAnnotations(field.visibleAnnotations);
                if (fieldAnnRef == null) {
                    fieldAnnRef = firstBlockedInAnnotations(field.invisibleAnnotations);
                }
                if (fieldAnnRef != null) {
                    return "field annotation " + fieldAnnRef;
                }
                var valueRef = firstBlockedInAnnotationValue(field.value);
                if (valueRef != null) {
                    return "field value " + valueRef;
                }
            }
        }
        if (node.methods != null) {
            for (MethodNode method : node.methods) {
                var methodDescRef = firstBlockedRef(method.desc);
                if (methodDescRef != null) {
                    return "method desc " + methodDescRef;
                }
                var methodSigRef = firstBlockedRef(method.signature);
                if (methodSigRef != null) {
                    return "method signature " + methodSigRef;
                }
                if (method.exceptions != null) {
                    for (String ex : method.exceptions) {
                        var exRef = firstBlockedRef(ex);
                        if (exRef != null) {
                            return "throws " + exRef;
                        }
                    }
                }
                var methodAnnRef = firstBlockedInAnnotations(method.visibleAnnotations);
                if (methodAnnRef == null) {
                    methodAnnRef = firstBlockedInAnnotations(method.invisibleAnnotations);
                }
                if (methodAnnRef == null) {
                    methodAnnRef = firstBlockedInAnnotations(method.visibleTypeAnnotations);
                }
                if (methodAnnRef == null) {
                    methodAnnRef = firstBlockedInAnnotations(method.invisibleTypeAnnotations);
                }
                if (methodAnnRef == null) {
                    methodAnnRef = firstBlockedInAnnotations(method.visibleLocalVariableAnnotations);
                }
                if (methodAnnRef == null) {
                    methodAnnRef = firstBlockedInAnnotations(method.invisibleLocalVariableAnnotations);
                }
                if (methodAnnRef != null) {
                    return "method annotation " + methodAnnRef;
                }
                if (method.localVariables != null) {
                    for (var local : method.localVariables) {
                        var localDescRef = firstBlockedRef(local.desc);
                        if (localDescRef != null) {
                            return "local desc " + localDescRef;
                        }
                        var localSigRef = firstBlockedRef(local.signature);
                        if (localSigRef != null) {
                            return "local signature " + localSigRef;
                        }
                    }
                }
                if (method.tryCatchBlocks != null) {
                    for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
                        var typeRef = firstBlockedRef(tcb.type);
                        if (typeRef != null) {
                            return "catch type " + typeRef;
                        }
                        var tcbAnnRef = firstBlockedInAnnotations(tcb.visibleTypeAnnotations);
                        if (tcbAnnRef == null) {
                            tcbAnnRef = firstBlockedInAnnotations(tcb.invisibleTypeAnnotations);
                        }
                        if (tcbAnnRef != null) {
                            return "try/catch annotation " + tcbAnnRef;
                        }
                    }
                }
                if (method.instructions != null) {
                    for (var insn : method.instructions) {
                        String insnRef = null;
                        if (insn instanceof MethodInsnNode methodInsn) {
                            insnRef = firstBlockedRef(methodInsn.owner);
                            if (insnRef == null) {
                                insnRef = firstBlockedRef(methodInsn.desc);
                            }
                        } else if (insn instanceof FieldInsnNode fieldInsn) {
                            insnRef = firstBlockedRef(fieldInsn.owner);
                            if (insnRef == null) {
                                insnRef = firstBlockedRef(fieldInsn.desc);
                            }
                        } else if (insn instanceof TypeInsnNode typeInsn) {
                            insnRef = firstBlockedRef(typeInsn.desc);
                        } else if (insn instanceof InvokeDynamicInsnNode indy) {
                            insnRef = firstBlockedRef(indy.desc);
                            if (insnRef == null) {
                                insnRef = firstBlockedInAnnotationValue(indy.bsm);
                            }
                            if (insnRef == null && indy.bsmArgs != null) {
                                for (Object arg : indy.bsmArgs) {
                                    insnRef = firstBlockedInAnnotationValue(arg);
                                    if (insnRef != null) {
                                        break;
                                    }
                                }
                            }
                        } else if (insn instanceof MultiANewArrayInsnNode multiArrayInsn) {
                            insnRef = firstBlockedRef(multiArrayInsn.desc);
                        } else if (insn instanceof LdcInsnNode ldcInsn) {
                            insnRef = firstBlockedInAnnotationValue(ldcInsn.cst);
                        } else if (insn instanceof FrameNode
                                || insn instanceof LabelNode
                                || insn instanceof LineNumberNode
                                || insn instanceof JumpInsnNode
                                || insn instanceof LookupSwitchInsnNode
                                || insn instanceof TableSwitchInsnNode
                                || insn instanceof IincInsnNode) {
                            // no-op
                        }
                        if (insnRef != null) {
                            return "instruction " + insnRef;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String firstBlockedInAnnotations(Collection<? extends AnnotationNode> annotations) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (annotation == null) {
                continue;
            }
            var descRef = firstBlockedRef(annotation.desc);
            if (descRef != null) {
                return descRef;
            }
            var valueRef = firstBlockedInAnnotationValues(annotation.values);
            if (valueRef != null) {
                return valueRef;
            }
        }
        return null;
    }

    private static String firstBlockedInAnnotationValues(List<Object> values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            var ref = firstBlockedInAnnotationValue(value);
            if (ref != null) {
                return ref;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String firstBlockedInAnnotationValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return firstBlockedRef(text);
        }
        if (value instanceof Type type) {
            var descRef = firstBlockedRef(type.getDescriptor());
            if (descRef != null) {
                return descRef;
            }
            return firstBlockedRef(type.getInternalName());
        }
        if (value instanceof Handle handle) {
            var ownerRef = firstBlockedRef(handle.getOwner());
            if (ownerRef != null) {
                return ownerRef;
            }
            return firstBlockedRef(handle.getDesc());
        }
        if (value instanceof ConstantDynamic constantDynamic) {
            var nameRef = firstBlockedRef(constantDynamic.getName());
            if (nameRef != null) {
                return nameRef;
            }
            var descRef = firstBlockedRef(constantDynamic.getDescriptor());
            if (descRef != null) {
                return descRef;
            }
            var bsmRef = firstBlockedInAnnotationValue(constantDynamic.getBootstrapMethod());
            if (bsmRef != null) {
                return bsmRef;
            }
            for (int i = 0; i < constantDynamic.getBootstrapMethodArgumentCount(); i++) {
                var argRef = firstBlockedInAnnotationValue(constantDynamic.getBootstrapMethodArgument(i));
                if (argRef != null) {
                    return argRef;
                }
            }
            return null;
        }
        if (value instanceof AnnotationNode annotationNode) {
            var descRef = firstBlockedRef(annotationNode.desc);
            if (descRef != null) {
                return descRef;
            }
            return firstBlockedInAnnotationValues(annotationNode.values);
        }
        if (value instanceof List<?> list) {
            for (Object o : list) {
                var listRef = firstBlockedInAnnotationValue(o);
                if (listRef != null) {
                    return listRef;
                }
            }
            return null;
        }
        if (value instanceof String[] arrayPair && arrayPair.length > 0) {
            for (String entry : arrayPair) {
                var pairRef = firstBlockedRef(entry);
                if (pairRef != null) {
                    return pairRef;
                }
            }
            return null;
        }
        return null;
    }

    private static String firstBlockedRef(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        var normalized = text.replace('.', '/');
        for (String prefix : FABRIC_BLOCKED_INTERNAL_PREFIX) {
            if (normalized.contains(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, ShouldApplyProcessor.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
