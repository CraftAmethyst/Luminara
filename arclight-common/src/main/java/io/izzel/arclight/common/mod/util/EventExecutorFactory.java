package io.izzel.arclight.common.mod.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.izzel.arclight.api.Unsafe;
import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates {@link EventExecutor} classes for listeners with ASM. Kept out of the
 * mixin class so the mixin pre-processor never has to resolve ASM types from mixin
 * method signatures on the standalone mod path.
 */
public final class EventExecutorFactory {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Cache<Method, Class<? extends EventExecutor>> EXECUTOR_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();
    private static final String HIDDEN_FORM =
        Float.parseFloat(System.getProperty("java.class.version")) < 57
            ? "Ljava/lang/invoke/LambdaForm$Hidden;"
            : "Ljdk/internal/vm/annotation/Hidden;";

    private EventExecutorFactory() {
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends EventExecutor> createExecutor(Method method, Class<? extends Event> eventClass) throws ExecutionException {
        return EXECUTOR_CACHE.get(method, () -> {
            ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cv.visit(Opcodes.V1_8,
                Opcodes.ACC_SUPER + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_FINAL,
                Type.getInternalName(method.getDeclaringClass()) + "$$arclight$" + COUNTER.getAndIncrement(),
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(EventExecutor.class)}
            );
            cv.visitOuterClass(Type.getInternalName(method.getDeclaringClass()), null, null);
            createConstructor(cv);
            createImpl(method, eventClass, cv);
            cv.visitEnd();
            return (Class<? extends EventExecutor>) Unsafe.defineAnonymousClass(method.getDeclaringClass(), cv.toByteArray(), null);
        });
    }

    private static void createConstructor(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "()V",
            null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private static void createImpl(Method method, Class<? extends Event> eventClass, ClassVisitor cv) {
        String ownerType = Type.getInternalName(method.getDeclaringClass());
        MethodVisitor mv = cv.visitMethod(
            Opcodes.ACC_PUBLIC,
            "execute",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(org.bukkit.event.Listener.class), Type.getType(Event.class)),
            null, null
        );
        mv.visitAnnotation(HIDDEN_FORM, true);

        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
        Label label3 = new Label();
        Label label4 = new Label();
        // try {
        mv.visitTryCatchBlock(label3, label4, label2, "java/lang/Throwable");
        //   if (!(event instanceof TYPE))
        mv.visitLabel(label0);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(eventClass));
        mv.visitJumpInsn(Opcodes.IFNE, label3);
        //      return;
        mv.visitLabel(label1);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(label3);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        //   ((TYPE) listener).<method>(event);
        //   TYPE.<method>(event);
        int invokeCode;
        if (Modifier.isStatic(method.getModifiers())) {
            invokeCode = Opcodes.INVOKESTATIC;
        } else if (method.getDeclaringClass().isInterface()) {
            invokeCode = Opcodes.INVOKEINTERFACE;
        } else {
            invokeCode = Opcodes.INVOKEVIRTUAL;
        }
        if (invokeCode != Opcodes.INVOKESTATIC) {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ownerType);
        }
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(eventClass));
        mv.visitMethodInsn(invokeCode, ownerType, method.getName(), Type.getMethodDescriptor(method), invokeCode == Opcodes.INVOKEINTERFACE);
        int retSize = Type.getType(method.getReturnType()).getSize();
        if (retSize > 0) {
            mv.visitInsn(Opcodes.POP + retSize - 1);
        }
        mv.visitLabel(label4);
        // } catch (Throwable t) {
        Label label5 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, label5);
        mv.visitLabel(label2);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
        mv.visitVarInsn(Opcodes.ASTORE, 3);
        // throw new EventException(t);
        Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitTypeInsn(Opcodes.NEW, "org/bukkit/event/EventException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/bukkit/event/EventException", "<init>", "(Ljava/lang/Throwable;)V", false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(label5);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(Opcodes.RETURN);
        // }
        Label label7 = new Label();
        mv.visitLabel(label7);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }
}