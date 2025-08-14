package co.aikar.timings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides an ability to time sections of code within the Minecraft Server
 */
public interface Timing extends AutoCloseable {

    /**
     * Gets the timer that is currently timing your code
     *
     * @return Current Timing or null if none
     */
    @Nullable
    static Timing getCurrentTiming() {
        // Timings v2 is deprecated, return null
        return null;
    }

    /**
     * Used as a super convenient way to time code
     *
     * @param timing   The timing to time
     * @param runnable The code to time
     */
    static void time(@NotNull Timing timing, @NotNull Runnable runnable) {
        timing.startTiming();
        try {
            runnable.run();
        } finally {
            timing.stopTiming();
        }
    }

    /**
     * Starts timing the execution until {@link #stopTiming()} is called.
     *
     * @return this
     */
    @NotNull
    Timing startTiming();

    /**
     * <p>Stops timing and records the data. Propagates the data up to group handlers.</p>
     *
     * <p>Will automatically be called when this Timing is used with try-with-resources</p>
     */
    void stopTiming();

    /**
     * Starts timing the execution until {@link #stopTiming()} is called.
     * <p>
     * But only if we are on the primary thread.
     *
     * @return this
     */
    @NotNull
    Timing startTimingIfSync();

    /**
     * Stops timing and records the data. Propagates the data up to group handlers.
     *
     * <p>Will automatically be called when this Timing is used with try-with-resources</p>
     * <p>
     * But only if we are on the primary thread.
     */
    void stopTimingIfSync();

    /**
     * Stops timing and disregards current timing data.
     */
    void abort();

    @Override
    void close();
}
