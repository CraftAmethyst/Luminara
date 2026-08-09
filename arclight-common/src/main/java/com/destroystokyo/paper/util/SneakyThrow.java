package com.destroystokyo.paper.util;

public final class SneakyThrow {

    private SneakyThrow() {
    }

    public static RuntimeException sneaky(final Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        throw new RuntimeException(throwable);
    }
}
