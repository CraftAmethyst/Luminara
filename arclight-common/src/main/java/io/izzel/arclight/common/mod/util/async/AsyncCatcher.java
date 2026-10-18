package io.izzel.arclight.common.mod.util.async;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.AsyncCatcherSpec;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Guards the world-state methods that must not be driven from a non-server thread. Strategy comes
 * from {@code async-catcher.defaultOperation} in {@code luminara.yml}.
 */
public final class AsyncCatcher {

  public static final Logger LOGGER = ArclightI18nLogger.getLogger("AsyncCatcher");

  private static final Marker MARKER = MarkerManager.getMarker("ASYNC_CATCHER");
  private static final long BLOCK_TIMEOUT_SECONDS = 5L;

  /**
   * Marks the thread running a replayed call. The replay invokes the same guarded method, so
   * without this the guard would replay itself forever.
   */
  private static final ThreadLocal<Boolean> REPLAYING = new ThreadLocal<>();

  @FunctionalInterface
  public interface Operation<T> {
    T run() throws Throwable;
  }

  private AsyncCatcher() {}

  /**
   * Returns empty when the caller is the server thread and the original body should run. Otherwise
   * the strategy has been applied and the value is what the caller must return.
   */
  public static <T> Optional<T> check(String reason, Operation<T> operation) {
    if (isReplaying() || ArclightServer.isPrimaryThread()) {
      return Optional.empty();
    }
    return Optional.ofNullable(apply(reason, operation));
  }

  /**
   * Returns false when the caller is the server thread. Otherwise the call was handled and the
   * caller must not run the original body.
   */
  public static <T> boolean checkVoid(String reason, Operation<T> operation) {
    if (isReplaying() || ArclightServer.isPrimaryThread()) {
      return false;
    }
    apply(reason, operation);
    return true;
  }

  public static <T> T replay(Operation<T> operation) {
    REPLAYING.set(Boolean.TRUE);
    try {
      return operation.run();
    } catch (RuntimeException | Error unchecked) {
      throw unchecked;
    } catch (Throwable checked) {
      throw new CompletionException(checked);
    } finally {
      REPLAYING.remove();
    }
  }

  private static boolean isReplaying() {
    return Boolean.TRUE.equals(REPLAYING.get());
  }

  private static <T> T apply(String reason, Operation<T> operation) {
    AsyncCatcherSpec specification = ArclightConfig.spec().getAsyncCatcher();
    AsyncCatcherSpec.Operation strategy =
        specification.getOverrides().getOrDefault(reason, specification.getDefaultOp());
    if (strategy == null) strategy = AsyncCatcherSpec.Operation.BLOCK;
    if (specification.isWarn()) {
      LOGGER.warn(
          MARKER, "Asynchronous {} from thread {}", reason, Thread.currentThread().getName());
    }
    if (specification.isDump()) {
      LOGGER.debug(
          MARKER,
          "Asynchronous {}",
          reason,
          new IllegalStateException("Asynchronous " + reason + "!"));
    }
    return execute(strategy, reason, operation, executor());
  }

  static <T> T execute(
      AsyncCatcherSpec.Operation strategy,
      String reason,
      Operation<T> operation,
      Executor executor) {
    switch (strategy) {
      case NONE:
        return null;
      case EXCEPTION:
        throw new IllegalStateException("Asynchronous " + reason + "!");
      case DISPATCH:
        executor.execute(
            () -> {
              try {
                replay(operation);
              } catch (Throwable throwable) {
                LOGGER.error(MARKER, "Dispatched {} failed", reason, throwable);
              }
            });
        return null;
      case BLOCK:
        return blocking(reason, operation, executor);
      default:
        throw new IllegalStateException("Unhandled async catcher strategy " + strategy);
    }
  }

  private static <T> T blocking(String reason, Operation<T> operation, Executor executor) {
    CompletableFuture<T> future =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                return replay(operation);
              } catch (Throwable throwable) {
                throw new CompletionException(throwable);
              }
            },
            executor);
    try {
      return future.get(BLOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (TimeoutException timeout) {
      var server = ArclightServer.getMinecraftServer();
      if (server == null) {
        LOGGER.error(
            MARKER, "Asynchronous {} timed out after {}s", reason, BLOCK_TIMEOUT_SECONDS, timeout);
      } else {
        // The stack of the blocked server thread is what makes this debuggable.
        Exception where = new Exception("Server thread");
        where.setStackTrace(server.getRunningThread().getStackTrace());
        LOGGER.error(
            MARKER, "Asynchronous {} timed out waiting for the server thread", reason, where);
      }
      throw new IllegalStateException(
          "Asynchronous " + reason + " timed out after " + BLOCK_TIMEOUT_SECONDS + "s", timeout);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while blocking on " + reason, interrupted);
    } catch (ExecutionException failure) {
      Throwable cause = failure.getCause();
      if (cause instanceof RuntimeException runtime) throw runtime;
      if (cause instanceof Error error) throw error;
      throw new IllegalStateException("Asynchronous " + reason + " failed", cause);
    }
  }

  private static Executor executor() {
    return ArclightServer.getMainThreadExecutor();
  }
}
