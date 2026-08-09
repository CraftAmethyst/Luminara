package io.izzel.arclight.boot.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ServiceLoader;

public class ServiceLoaderTransformer implements Implementer {

    private static final String SERVICE_LOADER = "java/util/ServiceLoader";
    private static final String LOAD_DESC = "(Ljava/lang/Class;)Ljava/util/ServiceLoader;";

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
    public boolean processClass(ClassNode node) {
        if (node.name.equals(Type.getInternalName(ServiceLoaderTransformer.class))) {
            return false;
        }
        boolean transformed = false;
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call
                    && call.getOpcode() == Opcodes.INVOKESTATIC
                    && SERVICE_LOADER.equals(call.owner)
                    && "load".equals(call.name)
                    && LOAD_DESC.equals(call.desc)) {
                    call.owner = Type.getInternalName(ServiceLoaderTransformer.class);
                    transformed = true;
                }
            }
        }
        return transformed;
    }
}
