package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.api.Unsafe;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds the per-listener {@link EventExecutor} classes that Bukkit
 * normally compiles at event registration time.
 * <p>
 * The ASM calls live here rather than in the mixin that used to contain them. Mixin's
 * preprocessor resolves the owner of every method call in an injected or overwritten body,
 * and it cannot resolve {@code org.objectweb.asm} classes: Forge owns those packages on the
 * legacy class path, which the game layer's module class loader does not search. Keeping
 * the ASM calls in an ordinary helper keeps mixin application working, while this class is
 * loaded normally and reaches ASM the same way the plugin remapper does.
 */
public final class EventExecutorFactory {

    private static final String HIDDEN_FORM =
        Float.parseFloat(System.getProperty("java.class.version")) < 57
            ? "Ljava/lang/invoke/LambdaForm$Hidden;"
            : "Ljdk/internal/vm/annotation/Hidden;";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private EventExecutorFactory() {
    }

    /**
     * Returns the annotation value that decides whether a listener method is registered,
     * so callers do not have to reference Bukkit's annotation type directly.
     */
    public static EventHandler eventHandler(Method method) {
        return method.getAnnotation(EventHandler.class);
    }

    public static Class<? extends EventExecutor> createExecutor(
        Method method,
        Class<? extends Event> eventClass
    ) {
        ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cv.visit(
            Opcodes.V1_8,
            Opcodes.ACC_SUPER + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_FINAL,
            Type.getInternalName(method.getDeclaringClass()) +
                "$$arclight$" +
                COUNTER.getAndIncrement(),
            null,
            Type.getInternalName(Object.class),
            new String[]{Type.getInternalName(EventExecutor.class)}
        );
        cv.visitOuterClass(
            Type.getInternalName(method.getDeclaringClass()),
            null,
            null
        );
        createConstructor(cv);
        createImpl(method, eventClass, cv);
        cv.visitEnd();
        return (Class<? extends EventExecutor>) Unsafe.defineAnonymousClass(
            method.getDeclaringClass(),
            cv.toByteArray(),
            null
        );
    }

    private static void createConstructor(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "()V",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            Type.getInternalName(Object.class),
            "<init>",
            "()V",
            false
        );
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private static void createImpl(
        Method method,
        Class<? extends Event> eventClass,
        ClassVisitor cv
    ) {
        String ownerType = Type.getInternalName(method.getDeclaringClass());
        MethodVisitor mv = cv.visitMethod(
            Opcodes.ACC_PUBLIC,
            "execute",
            Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(Listener.class),
                Type.getType(Event.class)
            ),
            null,
            null
        );
        mv.visitAnnotation(HIDDEN_FORM, true);

        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
        Label label3 = new Label();
        Label label4 = new Label();
        mv.visitTryCatchBlock(label3, label4, label2, "java/lang/Throwable");

        mv.visitLabel(label0);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(eventClass));
        mv.visitJumpInsn(Opcodes.IFNE, label3);
        mv.visitLabel(label1);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(label3);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

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
        mv.visitMethodInsn(
            invokeCode,
            ownerType,
            method.getName(),
            Type.getMethodDescriptor(method),
            invokeCode == Opcodes.INVOKEINTERFACE
        );
        int retSize = Type.getType(method.getReturnType()).getSize();
        if (retSize > 0) {
            mv.visitInsn(Opcodes.POP + retSize - 1);
        }
        mv.visitLabel(label4);

        Label label5 = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, label5);
        mv.visitLabel(label2);
        mv.visitFrame(
            Opcodes.F_SAME1,
            0,
            null,
            1,
            new Object[]{"java/lang/Throwable"}
        );
        mv.visitVarInsn(Opcodes.ASTORE, 3);
        Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitTypeInsn(Opcodes.NEW, "org/bukkit/event/EventException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "org/bukkit/event/EventException",
            "<init>",
            "(Ljava/lang/Throwable;)V",
            false
        );
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(label5);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(Opcodes.RETURN);
        Label label7 = new Label();
        mv.visitLabel(label7);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

}
