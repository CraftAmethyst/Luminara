package io.izzel.arclight.common.mod.util.remapper;

import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.mod.ArclightMod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.tree.*;

import java.util.regex.Pattern;

public class CraftBukkitVersionRemapper implements PluginTransformer {

    public static final CraftBukkitVersionRemapper INSTANCE =
        new CraftBukkitVersionRemapper();
    private static final Marker MARKER = MarkerManager.getMarker("CBREMAPPER");

    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "v\\d+_\\d+_R\\d+"
    );
    private static final String CRAFTBUKKIT_PREFIX = "org/bukkit/craftbukkit/";
    private static final String CRAFTBUKKIT_DOT_PREFIX =
        "org.bukkit.craftbukkit.";
    private static final String GENERIC_VERSION = "v";

    public static String remapInternalName(String internalName) {
        if (
            internalName == null || !internalName.startsWith(CRAFTBUKKIT_PREFIX)
        ) {
            return internalName;
        }

        String afterPrefix = internalName.substring(
            CRAFTBUKKIT_PREFIX.length()
        );
        int slashIndex = afterPrefix.indexOf('/');

        if (slashIndex == -1) {
            // No slash after prefix, entire string is version
            if (VERSION_PATTERN.matcher(afterPrefix).matches()) {
                return CRAFTBUKKIT_PREFIX + GENERIC_VERSION;
            }
            return internalName;
        }

        String versionPart = afterPrefix.substring(0, slashIndex);
        if (VERSION_PATTERN.matcher(versionPart).matches()) {
            return (
                CRAFTBUKKIT_PREFIX +
                    GENERIC_VERSION +
                    afterPrefix.substring(slashIndex)
            );
        }

        return internalName;
    }

    public static String remapBinaryName(String binaryName) {
        if (binaryName == null) {
            return null;
        }

        // Some plugins pass internal-style names (with '/')
        // into reflection APIs that expect binary names.
        binaryName = binaryName.replace('/', '.');

        if (!binaryName.startsWith(CRAFTBUKKIT_DOT_PREFIX)) {
            return binaryName;
        }

        String afterPrefix = binaryName.substring(
            CRAFTBUKKIT_DOT_PREFIX.length()
        );
        int dotIndex = afterPrefix.indexOf('.');

        if (dotIndex == -1) {
            if (VERSION_PATTERN.matcher(afterPrefix).matches()) {
                return CRAFTBUKKIT_DOT_PREFIX + GENERIC_VERSION;
            }
            return binaryName;
        }

        String versionPart = afterPrefix.substring(0, dotIndex);
        if (VERSION_PATTERN.matcher(versionPart).matches()) {
            return (
                CRAFTBUKKIT_DOT_PREFIX +
                    GENERIC_VERSION +
                    afterPrefix.substring(dotIndex)
            );
        }

        return binaryName;
    }

    public static String toVersionedInternalName(String internalName) {
        if (
            internalName == null || !internalName.startsWith(CRAFTBUKKIT_PREFIX)
        ) {
            return internalName;
        }

        String afterPrefix = internalName.substring(
            CRAFTBUKKIT_PREFIX.length()
        );
        int slashIndex = afterPrefix.indexOf('/');
        String versionPart =
            slashIndex == -1
                ? afterPrefix
                : afterPrefix.substring(0, slashIndex);
        if (!GENERIC_VERSION.equals(versionPart)) {
            return internalName;
        }

        String version = currentVersion();
        if (slashIndex == -1) {
            return CRAFTBUKKIT_PREFIX + version;
        }
        return CRAFTBUKKIT_PREFIX + version + afterPrefix.substring(slashIndex);
    }

    public static String toVersionedBinaryName(String binaryName) {
        if (binaryName == null) {
            return null;
        }

        binaryName = binaryName.replace('/', '.');
        if (!binaryName.startsWith(CRAFTBUKKIT_DOT_PREFIX)) {
            return binaryName;
        }

        String afterPrefix = binaryName.substring(
            CRAFTBUKKIT_DOT_PREFIX.length()
        );
        int dotIndex = afterPrefix.indexOf('.');
        String versionPart =
            dotIndex == -1 ? afterPrefix : afterPrefix.substring(0, dotIndex);
        if (!GENERIC_VERSION.equals(versionPart)) {
            return binaryName;
        }

        String version = currentVersion();
        if (dotIndex == -1) {
            return CRAFTBUKKIT_DOT_PREFIX + version;
        }
        return (
            CRAFTBUKKIT_DOT_PREFIX + version + afterPrefix.substring(dotIndex)
        );
    }

    private static String currentVersion() {
        try {
            String current = ArclightVersion.current().packageName();
            if (current != null && !current.isBlank()) {
                return current;
            }
        } catch (Throwable ignored) {
        }
        return GENERIC_VERSION;
    }

    private void remapAnnotations(java.util.List<AnnotationNode> annotations) {
        if (annotations != null) {
            for (AnnotationNode annotation : annotations) {
                annotation.desc = remapDescriptor(annotation.desc);
            }
        }
    }

    private void remapFrameTypes(java.util.List<Object> frameTypes) {
        if (frameTypes == null) {
            return;
        }
        for (int i = 0; i < frameTypes.size(); i++) {
            Object entry = frameTypes.get(i);
            if (entry instanceof String internalName) {
                if (internalName.startsWith("[")) {
                    frameTypes.set(i, remapDescriptor(internalName));
                } else {
                    frameTypes.set(i, remapInternalName(internalName));
                }
            }
        }
    }

    @Override
    public void handleClass(
        ClassNode node,
        ClassLoaderRemapper remapper,
        ArclightRemapConfig config
    ) {
        if (node.name.startsWith(CRAFTBUKKIT_PREFIX)) {
            String remapped = remapInternalName(node.name);
            if (!remapped.equals(node.name)) {
                ArclightMod.LOGGER.debug(
                    MARKER,
                    "Remapping class {} to {}",
                    node.name,
                    remapped
                );
                node.name = remapped;
            }
        }

        if (node.superName != null) {
            node.superName = remapInternalName(node.superName);
        }

        if (node.interfaces != null) {
            for (int i = 0; i < node.interfaces.size(); i++) {
                node.interfaces.set(
                    i,
                    remapInternalName(node.interfaces.get(i))
                );
            }
        }

        for (FieldNode field : node.fields) {
            field.desc = remapDescriptor(field.desc);
            if (field.signature != null) {
                field.signature = remapSignature(field.signature);
            }
        }

        for (MethodNode method : node.methods) {
            method.desc = remapDescriptor(method.desc);
            if (method.signature != null) {
                method.signature = remapSignature(method.signature);
            }

            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof TypeInsnNode typeInsn) {
                    typeInsn.desc = remapInternalName(typeInsn.desc);
                } else if (insn instanceof FieldInsnNode fieldInsn) {
                    fieldInsn.owner = remapInternalName(fieldInsn.owner);
                    fieldInsn.desc = remapDescriptor(fieldInsn.desc);
                } else if (insn instanceof MethodInsnNode methodInsn) {
                    methodInsn.owner = remapInternalName(methodInsn.owner);
                    methodInsn.desc = remapDescriptor(methodInsn.desc);
                } else if (
                    insn instanceof InvokeDynamicInsnNode invokeDynamicInsn
                ) {
                    invokeDynamicInsn.bsm = new org.objectweb.asm.Handle(
                        invokeDynamicInsn.bsm.getTag(),
                        remapInternalName(invokeDynamicInsn.bsm.getOwner()),
                        invokeDynamicInsn.bsm.getName(),
                        remapDescriptor(invokeDynamicInsn.bsm.getDesc()),
                        invokeDynamicInsn.bsm.isInterface()
                    );
                    invokeDynamicInsn.desc = remapDescriptor(
                        invokeDynamicInsn.desc
                    );
                    Object[] bsmArgs = invokeDynamicInsn.bsmArgs;
                    for (int i = 0; i < bsmArgs.length; i++) {
                        if (bsmArgs[i] instanceof org.objectweb.asm.Type) {
                            bsmArgs[i] = org.objectweb.asm.Type.getType(
                                remapDescriptor(
                                    (
                                        (org.objectweb.asm.Type) bsmArgs[i]
                                    ).getDescriptor()
                                )
                            );
                        } else if (
                            bsmArgs[i] instanceof
                                org.objectweb.asm.Handle handle
                        ) {
                            bsmArgs[i] = new org.objectweb.asm.Handle(
                                handle.getTag(),
                                remapInternalName(handle.getOwner()),
                                handle.getName(),
                                remapDescriptor(handle.getDesc()),
                                handle.isInterface()
                            );
                        }
                    }
                } else if (insn instanceof LdcInsnNode ldcInsn) {
                    if (ldcInsn.cst instanceof String stringConstant) {
                        ldcInsn.cst = remapStringConstant(stringConstant);
                    } else if (
                        ldcInsn.cst instanceof org.objectweb.asm.Type type
                    ) {
                        ldcInsn.cst = org.objectweb.asm.Type.getType(
                            remapDescriptor(type.getDescriptor())
                        );
                    }
                } else if (
                    insn instanceof MultiANewArrayInsnNode multiArrayInsn
                ) {
                    multiArrayInsn.desc = remapDescriptor(multiArrayInsn.desc);
                } else if (insn instanceof FrameNode frameNode) {
                    remapFrameTypes(frameNode.local);
                    remapFrameTypes(frameNode.stack);
                }
            }

            if (method.localVariables != null) {
                for (LocalVariableNode localVar : method.localVariables) {
                    localVar.desc = remapDescriptor(localVar.desc);
                    if (localVar.signature != null) {
                        localVar.signature = remapSignature(localVar.signature);
                    }
                }
            }

            if (method.tryCatchBlocks != null) {
                for (TryCatchBlockNode tryCatch : method.tryCatchBlocks) {
                    if (tryCatch.type != null) {
                        tryCatch.type = remapInternalName(tryCatch.type);
                    }
                }
            }
        }

        remapAnnotations(node.visibleAnnotations);
        remapAnnotations(node.invisibleAnnotations);

        for (FieldNode field : node.fields) {
            remapAnnotations(field.visibleAnnotations);
            remapAnnotations(field.invisibleAnnotations);
        }

        for (MethodNode method : node.methods) {
            remapAnnotations(method.visibleAnnotations);
            remapAnnotations(method.invisibleAnnotations);
        }
    }

    private String remapDescriptor(String descriptor) {
        if (descriptor == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < descriptor.length()) {
            char c = descriptor.charAt(i);
            if (c == 'L') {
                // Object type descriptor
                int semicolon = descriptor.indexOf(';', i);
                if (semicolon == -1) {
                    result.append(descriptor.substring(i));
                    break;
                }
                String className = descriptor.substring(i + 1, semicolon);
                result
                    .append('L')
                    .append(remapInternalName(className))
                    .append(';');
                i = semicolon + 1;
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    private String remapStringConstant(String constant) {
        if (constant == null || constant.isEmpty()) {
            return constant;
        }
        if (constant.startsWith(CRAFTBUKKIT_PREFIX)) {
            return remapInternalName(constant);
        }
        if (constant.startsWith(CRAFTBUKKIT_DOT_PREFIX)) {
            return remapBinaryName(constant);
        }
        if (
            (constant.startsWith("L") || constant.startsWith("[")) &&
                constant.contains(CRAFTBUKKIT_PREFIX)
        ) {
            return remapDescriptor(constant);
        }
        return constant;
    }

    private String remapSignature(String signature) {
        if (signature == null) {
            return null;
        }
        // Simple implementation - just remap class references in signatures
        return remapDescriptor(signature);
    }
}
