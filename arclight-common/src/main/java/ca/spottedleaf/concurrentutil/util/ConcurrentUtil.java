package ca.spottedleaf.concurrentutil.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * Utility class for concurrent operations and VarHandle access.
 * This class provides thread-safe utilities for Paper's concurrent operations.
 */
public final class ConcurrentUtil {

    private ConcurrentUtil() {
    }

    /**
     * Gets a VarHandle for the specified field.
     *
     * @param clazz     The class containing the field
     * @param fieldName The name of the field
     * @param fieldType The type of the field
     * @return The VarHandle for the field
     */
    public static VarHandle getVarHandle(final Class<?> clazz, final String fieldName, final Class<?> fieldType) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findVarHandle(clazz, fieldName, fieldType);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to get VarHandle for field " + fieldName + " in class " + clazz.getName(), ex);
        }
    }

    /**
     * Performs a backoff operation for busy-waiting scenarios.
     * This method provides a hint to the processor that the current thread is in a spin-wait loop.
     */
    public static void backoff() {
        // Use Thread.onSpinWait() if available (Java 9+), otherwise fall back to Thread.yield()
        try {
            Thread.onSpinWait();
        } catch (final Throwable ignored) {
            Thread.yield();
        }
    }

    /**
     * Parks the current thread for a very short duration.
     * This is useful for implementing short waits in concurrent algorithms.
     */
    public static void shortPark() {
        LockSupport.parkNanos(1L);
    }

    /**
     * Parks the current thread for the specified number of nanoseconds.
     *
     * @param nanos The number of nanoseconds to park
     */
    public static void parkNanos(final long nanos) {
        if (nanos > 0L) {
            LockSupport.parkNanos(nanos);
        }
    }

    /**
     * Rethrows the specified throwable, wrapping it in a RuntimeException if necessary.
     *
     * @param throwable The throwable to rethrow
     */
    public static void rethrow(final Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        throw new RuntimeException(throwable);
    }

    /**
     * Returns the number of available processors.
     *
     * @return The number of available processors
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Creates a daemon thread with the specified name and runnable.
     *
     * @param name     The name of the thread
     * @param runnable The runnable to execute
     * @return The created thread
     */
    public static Thread createDaemonThread(final String name, final Runnable runnable) {
        final Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Creates a daemon thread with the specified name, runnable, and thread group.
     *
     * @param group    The thread group
     * @param name     The name of the thread
     * @param runnable The runnable to execute
     * @return The created thread
     */
    public static Thread createDaemonThread(final ThreadGroup group, final String name, final Runnable runnable) {
        final Thread thread = new Thread(group, runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Safely gets the current thread's name.
     *
     * @return The current thread's name
     */
    public static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * Checks if the current thread is the main server thread.
     *
     * @return true if the current thread is the main server thread
     */
    public static boolean isMainThread() {
        return Thread.currentThread().getName().equals("Server thread");
    }

    /**
     * Ensures that the current thread is the main server thread.
     *
     * @throws IllegalStateException if not on the main thread
     */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("Must be called on the main server thread, but was called on " + getCurrentThreadName());
        }
    }

    /**
     * Ensures that the current thread is NOT the main server thread.
     *
     * @throws IllegalStateException if on the main thread
     */
    public static void ensureNotMainThread() {
        if (isMainThread()) {
            throw new IllegalStateException("Must not be called on the main server thread");
        }
    }

    /**
     * Gets the system property as an integer, returning the default value if not found or invalid.
     *
     * @param property     The system property name
     * @param defaultValue The default value
     * @return The property value as an integer, or the default value
     */
    public static int getSystemPropertyInt(final String property, final int defaultValue) {
        try {
            final String value = System.getProperty(property);
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Gets the system property as a long, returning the default value if not found or invalid.
     *
     * @param property     The system property name
     * @param defaultValue The default value
     * @return The property value as a long, or the default value
     */
    public static long getSystemPropertyLong(final String property, final long defaultValue) {
        try {
            final String value = System.getProperty(property);
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Gets the system property as a boolean, returning the default value if not found.
     *
     * @param property     The system property name
     * @param defaultValue The default value
     * @return The property value as a boolean, or the default value
     */
    public static boolean getSystemPropertyBoolean(final String property, final boolean defaultValue) {
        final String value = System.getProperty(property);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }
}
