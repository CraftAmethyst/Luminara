package org.craftamethyst.luminara.common.mod.util.async;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.craftamethyst.luminara.i18n.conf.AsyncCatcherSpec;
import org.junit.jupiter.api.Test;

class AsyncCatcherTest {

  private static final Executor DIRECT = Runnable::run;

  @Test
  void noneLeavesTheCallUntouched() {
    AtomicBoolean ran = new AtomicBoolean();

    Object result =
        AsyncCatcher.execute(
            AsyncCatcherSpec.Operation.NONE,
            "entity add",
            () -> {
              ran.set(true);
              return 1;
            },
            DIRECT);

    assertNull(result);
    assertFalse(ran.get());
  }

  @Test
  void exceptionRejectsTheCallWithoutRunningIt() {
    AtomicBoolean ran = new AtomicBoolean();

    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () ->
                AsyncCatcher.execute(
                    AsyncCatcherSpec.Operation.EXCEPTION,
                    "entity add",
                    () -> {
                      ran.set(true);
                      return 1;
                    },
                    DIRECT));

    assertEquals("Asynchronous entity add!", failure.getMessage());
    assertFalse(ran.get());
  }

  @Test
  void blockRunsOnTheGivenExecutorAndReturnsItsValue() {
    AtomicInteger ran = new AtomicInteger();

    Integer result =
        AsyncCatcher.execute(
            AsyncCatcherSpec.Operation.BLOCK, "effect add", ran::incrementAndGet, DIRECT);

    assertEquals(1, result);
    assertEquals(1, ran.get());
  }

  @Test
  void blockRethrowsTheUncaughtFailureOfTheReplayedCall() {
    IllegalArgumentException expected = new IllegalArgumentException("boom");

    IllegalArgumentException failure =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                AsyncCatcher.execute(
                    AsyncCatcherSpec.Operation.BLOCK,
                    "block place",
                    () -> {
                      throw expected;
                    },
                    DIRECT));

    assertSame(expected, failure);
  }

  @Test
  void dispatchRunsTheCallAndReportsNoResult() {
    AtomicBoolean ran = new AtomicBoolean();

    Object result =
        AsyncCatcher.execute(
            AsyncCatcherSpec.Operation.DISPATCH,
            "loot generate",
            () -> {
              ran.set(true);
              return 1;
            },
            DIRECT);

    assertNull(result);
    assertTrue(ran.get());
  }

  @Test
  void replayedCallDoesNotReEnterTheGuard() {
    AtomicBoolean bypassed = new AtomicBoolean();

    AsyncCatcher.execute(
        AsyncCatcherSpec.Operation.BLOCK,
        "entity add",
        () -> {
          bypassed.set(AsyncCatcher.check("entity add", () -> 1).isEmpty());
          return 1;
        },
        DIRECT);

    assertTrue(bypassed.get(), "the guard must let the replayed call through instead of recursing");
  }
}
