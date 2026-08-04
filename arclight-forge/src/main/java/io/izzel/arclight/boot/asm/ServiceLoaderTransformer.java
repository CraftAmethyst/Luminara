package io.izzel.arclight.boot.asm;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import java.util.ServiceLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ServiceLoaderTransformer implements Implementer {

    private static final String SERVICE_LOADER = "java/util/ServiceLoader";
    private static final String LOAD_DESC =
        "(Ljava/lang/Class;)Ljava/util/ServiceLoader;";

    public static <S> ServiceLoader<S> load(Class<S> service) {
        if (service == null) {
            throw new NullPointerException("service");
        }
        ClassLoader loader = service.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return ServiceLoader.load(service, loader);
    }

    @Override
    public boolean processClass(
        ClassNode node,
        ILaunchPluginService.ITransformerLoader transformerLoader
    ) {
        if (
            node.name.equals(
                Type.getInternalName(ServiceLoaderTransformer.class)
            )
        ) {
            return false;
        }
        var transform = false;
        for (var mn : node.methods) {
            for (var insn : mn.instructions) {
                if (
                    insn.getOpcode() == Opcodes.INVOKESTATIC &&
                    insn instanceof MethodInsnNode method &&
                    SERVICE_LOADER.equals(method.owner) &&
                    "load".equals(method.name) &&
                    LOAD_DESC.equals(method.desc)
                ) {
                    method.owner = Type.getInternalName(
                        ServiceLoaderTransformer.class
                    );
                    transform = true;
                }
            }
        }
        return transform;
    }
}
