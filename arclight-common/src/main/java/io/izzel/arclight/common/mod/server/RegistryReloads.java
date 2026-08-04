package io.izzel.arclight.common.mod.server;

import java.util.ArrayList;
import java.util.List;

final class RegistryReloads {

    private RegistryReloads() {}

    static void run(List<Operation> operations) {
        List<Throwable> failures = new ArrayList<>();
        for (Operation operation : operations) {
            try {
                operation.action().run();
            } catch (Throwable throwable) {
                failures.add(
                    new IllegalStateException(operation.name(), throwable)
                );
            }
        }
        if (!failures.isEmpty()) {
            IllegalStateException failure = new IllegalStateException(
                "Failed to reload " + failures.size() + " Bukkit registries"
            );
            failures.forEach(failure::addSuppressed);
            throw failure;
        }
    }

    record Operation(String name, ThrowingRunnable action) {}

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
