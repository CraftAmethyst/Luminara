package org.craftamethyst.luminara.common.mod.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.craftamethyst.luminara.common.mod.util.log.LuminaraLog;

/** Guarantees that the process exits after the server has completed its shutdown. */
public final class ServerShutdownWatchdog {

  private static final long TIMEOUT_MILLIS = 5000L;
  private static final AtomicBoolean STARTED = new AtomicBoolean();

  private ServerShutdownWatchdog() {}

  public static void start() {
    if (!STARTED.compareAndSet(false, true)) {
      return;
    }
    Thread watchdog =
        new Thread(ServerShutdownWatchdog::awaitTimeout, "Luminara Shutdown Watchdog");
    watchdog.setDaemon(true);
    watchdog.setPriority(Thread.MAX_PRIORITY);
    watchdog.start();
  }

  private static void awaitTimeout() {
    try {
      Thread.sleep(TIMEOUT_MILLIS);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
    try {
      reportBlockingThreads();
    } finally {
      // halt bypasses shutdown hooks, which may be the threads blocking System.exit.
      Runtime.getRuntime().halt(0);
    }
  }

  private static void reportBlockingThreads() {
    List<String> threads = new ArrayList<>();
    for (Thread thread : Thread.getAllStackTraces().keySet()) {
      if (!thread.isDaemon()
          && !thread.getName().equals("DestroyJavaVM")
          && thread != Thread.currentThread()) {
        threads.add(thread.getName());
      }
    }
    threads.sort(Comparator.naturalOrder());
    if (!threads.isEmpty()) {
      LuminaraLog.LOGGER.debug("Threads {} not shutting down", String.join(", ", threads));
      LuminaraLog.LOGGER.info("server.threads.force-exit", threads.size());
    }
  }
}
