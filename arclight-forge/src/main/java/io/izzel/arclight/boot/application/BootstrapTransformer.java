package io.izzel.arclight.boot.application;

import cpw.mods.cl.ModuleClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.HexFormat;

/*
 * The implementation is affected by BootstrapLauncher and ModLauncher
 * Be sure to check for updates.
 */
public class BootstrapTransformer extends ClassLoader {


    private static final String cpwClass = "cpw.mods.bootstraplauncher.BootstrapLauncher";
    private static final String BOOTSTRAP_LAUNCHER_SHA256 = "04915b040fdc0044cb318a12b83cc461fa5a5e5bf0cb2ff8a5d605df69a1b3a7";
    private static final String UNSUPPORTED_BOOTSTRAP = "Unsupported BootstrapLauncher for Minecraft 1.20.1 / Forge 47.4.22";

    private final ProtectionDomain domain = getClass().getProtectionDomain();

    public BootstrapTransformer(ClassLoader appClassLoader) {
        super("arclight_bootstrap", appClassLoader);
    }

    @SuppressWarnings({"unused", "unchecked"})
    public static void onInvoke$BootstrapLauncher(String[] args, ModuleClassLoader moduleCl) {
        try {
            Class<ApplicationBootstrap> arclightBootClz = (Class<ApplicationBootstrap>) moduleCl.loadClass("io.izzel.arclight.boot.application.ApplicationBootstrap");
            Object instance = arclightBootClz.getConstructor().newInstance();
            arclightBootClz.getMethod("accept", String[].class).invoke(instance, (Object) args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * The class to transform can be resolved by AppClassLoader.
     * We have to break the delegation model to intercept class loading.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }

            // The class is not loaded. Are we going to intercept?
            // The inner classes and the outer class should be loaded
            // in the same ClassLoader to avoid inter-module access issues.
            if (!name.contains(cpwClass)) {
                // Delegate to parent.
                // parent.loadClass is inaccessible from here.
                // This ClassLoader will only load the launcher
                // and then a new ClassLoader, whose parent is
                // platform ClassLoader (null), will load the game.
                return super.loadClass(name, resolve);
            }

            Class<?> clz;
            try {
                clz = loadTransform(name);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ClassNotFoundException("Unexpected exception loading " + name);
            }

            if (resolve) {
                resolveClass(clz);
            }
            return clz;
        }
    }

    /*
     * findClass() is invoked when parent (in this case AppClassLoader)
     * cannot find the corresponding class. In this case we can't find either.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    public Class<?> loadTransform(String className) throws IOException {
        if (className.equals(cpwClass)) {
            var file = cpwClass.replace('.', '/').concat(".class");
            try (var inputStream = getResourceAsStream(file)) {
                if (inputStream == null) {
                    throw new RuntimeException("getResourceAsStream can't read BootstrapLauncher.class");
                }
                var transformed = transformBootstrapLauncher(inputStream);
                return defineClass(cpwClass, transformed, 0, transformed.length, domain);
            }
        } else if (className.contains(cpwClass)) {
            var file = className.replace('.', '/').concat(".class");
            try (var inputStream = getResourceAsStream(file)) {
                if (inputStream == null) {
                    throw new RuntimeException("getResourceAsStream can't read " + file.substring(file.lastIndexOf('/')));
                }
                var bytes = inputStream.readAllBytes();
                return defineClass(className, bytes, 0, bytes.length, domain);
            }
        }
        throw new UnsupportedOperationException("Transformation for " + className + " is not supported");
    }

    /*
     * Previous implementation of BootstrapLauncher relies on the order of ServiceLoader.load().stream()
     * where the ApplicationBootstrap will be ahead of modlauncher, which is an UB related to module name.
     * Modify BootstrapLauncher to use ApplicationBootstrap directly so a change in module name won't
     * affect launch process.
     */
    public byte[] transformBootstrapLauncher(InputStream inputStream) throws IOException {
        byte[] original = inputStream.readAllBytes();
        if (!BOOTSTRAP_LAUNCHER_SHA256.equals(sha256(original))) {
            throw new IllegalStateException(UNSUPPORTED_BOOTSTRAP);
        }
        System.out.println("Transforming cpw.mods.bootstraplauncher.BootstrapLauncher");
        var asmClass = new ClassNode();
        new ClassReader(original).accept(asmClass, 0);

        MethodNode asmMain = asmClass.methods.stream()
                .filter(method -> method.name.equals("main")
                        && method.desc.equals("([Ljava/lang/String;)V")
                        && (method.access & Opcodes.ACC_STATIC) != 0)
                .findFirst()
                .orElseThrow(BootstrapTransformer::unsupportedBootstrap);

        MethodInsnNode injectionPoint = null;
        int acceptCount = 0;
        for (var instruction : asmMain.instructions) {
            if (instruction instanceof MethodInsnNode invoke
                    && invoke.getOpcode() == Opcodes.INVOKEINTERFACE
                    && invoke.owner.equals("java/util/function/Consumer")
                    && invoke.name.equals("accept")
                    && invoke.desc.equals("(Ljava/lang/Object;)V")) {
                acceptCount++;
                injectionPoint = invoke;
            }
        }
        if (acceptCount != 1 || injectionPoint == null || !isLauncherConsumerCall(injectionPoint)) {
            throw unsupportedBootstrap();
        }

        int moduleClassLoaderVariable = findModuleClassLoaderVariable(asmMain);
        var replacement = new InsnList();
        replacement.add(new InsnNode(Opcodes.POP2));
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
        replacement.add(new VarInsnNode(Opcodes.ALOAD, moduleClassLoaderVariable));
        replacement.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "io/izzel/arclight/boot/application/BootstrapTransformer",
                "onInvoke$BootstrapLauncher",
                "([Ljava/lang/String;Lcpw/mods/cl/ModuleClassLoader;)V",
                false
        ));
        asmMain.instructions.insert(injectionPoint, replacement);
        asmMain.instructions.remove(injectionPoint);

        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        asmClass.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isLauncherConsumerCall(MethodInsnNode accept) {
        AbstractInsnNode args = previousInstruction(accept);
        AbstractInsnNode consumerCast = previousInstruction(args);
        AbstractInsnNode providerGet = previousInstruction(consumerCast);
        return args instanceof VarInsnNode loadArgs
                && loadArgs.getOpcode() == Opcodes.ALOAD
                && loadArgs.var == 0
                && consumerCast instanceof TypeInsnNode cast
                && cast.getOpcode() == Opcodes.CHECKCAST
                && cast.desc.equals("java/util/function/Consumer")
                && providerGet instanceof MethodInsnNode get
                && get.getOpcode() == Opcodes.INVOKEINTERFACE
                && get.owner.equals("java/util/ServiceLoader$Provider")
                && get.name.equals("get")
                && get.desc.equals("()Ljava/lang/Object;");
    }

    private static int findModuleClassLoaderVariable(MethodNode method) {
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode invoke
                    && invoke.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && invoke.owner.equals("java/lang/Thread")
                    && invoke.name.equals("setContextClassLoader")
                    && invoke.desc.equals("(Ljava/lang/ClassLoader;)V")) {
                AbstractInsnNode load = previousInstruction(invoke);
                if (!(load instanceof VarInsnNode variable) || variable.getOpcode() != Opcodes.ALOAD) continue;
                if (isStoredModuleClassLoader(method, variable.var, instruction)) return variable.var;
            }
        }
        throw unsupportedBootstrap();
    }

    private static boolean isStoredModuleClassLoader(MethodNode method, int variable, AbstractInsnNode before) {
        for (AbstractInsnNode instruction = previousInstruction(before); instruction != null; instruction = previousInstruction(instruction)) {
            if (instruction instanceof VarInsnNode store
                    && store.getOpcode() == Opcodes.ASTORE
                    && store.var == variable) {
                AbstractInsnNode constructor = previousInstruction(store);
                return constructor instanceof MethodInsnNode invoke
                        && invoke.getOpcode() == Opcodes.INVOKESPECIAL
                        && invoke.owner.equals("cpw/mods/cl/ModuleClassLoader")
                        && invoke.name.equals("<init>")
                        && invoke.desc.equals("(Ljava/lang/String;Ljava/lang/module/Configuration;Ljava/util/List;)V");
            }
        }
        return false;
    }

    private static AbstractInsnNode previousInstruction(AbstractInsnNode instruction) {
        if (instruction == null) return null;
        AbstractInsnNode previous = instruction.getPrevious();
        while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
        return previous;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static IllegalStateException unsupportedBootstrap() {
        return new IllegalStateException(UNSUPPORTED_BOOTSTRAP);
    }
}
