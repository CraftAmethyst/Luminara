package io.izzel.arclight.common.mod.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegistryReloadsTest {

    @Test
    void reportsEveryFailureAfterAttemptingEveryRegistry() {
        List<String> attempted = new ArrayList<>();
        List<RegistryReloads.Operation> operations = List.of(
            new RegistryReloads.Operation("first", () -> {
                attempted.add("first");
                throw new IllegalStateException("one");
            }),
            new RegistryReloads.Operation("second", () ->
                attempted.add("second")
            ),
            new RegistryReloads.Operation("third", () -> {
                attempted.add("third");
                throw new IllegalArgumentException("three");
            })
        );

        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> RegistryReloads.run(operations)
        );

        assertEquals(List.of("first", "second", "third"), attempted);
        assertEquals(
            "Failed to reload 2 Bukkit registries",
            failure.getMessage()
        );
        assertEquals(2, failure.getSuppressed().length);
        assertTrue(failure.getSuppressed()[0].getMessage().contains("first"));
        assertTrue(failure.getSuppressed()[1].getMessage().contains("third"));
    }
}
