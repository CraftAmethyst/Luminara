package ca.spottedleaf.concurrentutil.lock;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;

import java.lang.invoke.VarHandle;

/**
 * Weak sequence lock implementation for Paper's concurrent utilities.
 * This lock provides a lightweight synchronization mechanism for read-heavy scenarios.
 * <p>
 * This implementation is from Paper patch 0007 (ConcurrentUtil.patch).
 */
public final class WeakSeqLock {

    private static final VarHandle SEQUENCE_HANDLE = ConcurrentUtil.getVarHandle(WeakSeqLock.class, "sequence", int.class);
    private volatile int sequence;

    public WeakSeqLock() {
        this.sequence = 0;
    }

    /**
     * Acquires a read lock and returns the sequence number.
     * Readers should use this sequence number to validate their read operation.
     *
     * @return The sequence number for this read operation
     */
    public int acquireRead() {
        int sequence;
        while (((sequence = this.getSequenceVolatile()) & 1) != 0) {
            // Sequence is odd, meaning a write is in progress
            ConcurrentUtil.backoff();
        }
        return sequence;
    }

    /**
     * Validates that the read operation is still valid.
     * This should be called after completing the read operation with the sequence
     * number returned by acquireRead().
     *
     * @param sequence The sequence number from acquireRead()
     * @return true if the read is valid, false if it should be retried
     */
    public boolean validateRead(final int sequence) {
        return this.getSequenceVolatile() == sequence;
    }

    /**
     * Acquires a write lock.
     * This increments the sequence number to an odd value, indicating a write is in progress.
     *
     * @return The sequence number for this write operation
     */
    public int acquireWrite() {
        int sequence;
        do {
            sequence = this.getSequenceVolatile();
            if ((sequence & 1) != 0) {
                // Another write is in progress, wait
                ConcurrentUtil.backoff();
                continue;
            }
        } while (!this.compareAndSetSequenceVolatile(sequence, sequence + 1));

        return sequence + 1;
    }

    /**
     * Releases a write lock.
     * This increments the sequence number to an even value, indicating the write is complete.
     *
     * @param sequence The sequence number from acquireWrite()
     */
    public void releaseWrite(final int sequence) {
        if ((sequence & 1) == 0) {
            throw new IllegalArgumentException("Invalid write sequence: " + sequence);
        }
        this.setSequenceVolatile(sequence + 1);
    }

    /**
     * Attempts to acquire a write lock without blocking.
     *
     * @return The sequence number if successful, or -1 if the lock could not be acquired
     */
    public int tryAcquireWrite() {
        final int sequence = this.getSequenceVolatile();
        if ((sequence & 1) != 0) {
            // Another write is in progress
            return -1;
        }

        if (this.compareAndSetSequenceVolatile(sequence, sequence + 1)) {
            return sequence + 1;
        }

        return -1;
    }

    /* sequence */

    private int getSequenceVolatile() {
        return (int) SEQUENCE_HANDLE.getVolatile(this);
    }

    private void setSequenceVolatile(final int sequence) {
        SEQUENCE_HANDLE.setVolatile(this, sequence);
    }

    private boolean compareAndSetSequenceVolatile(final int expect, final int update) {
        return SEQUENCE_HANDLE.compareAndSet(this, expect, update);
    }

    private int getAndAddSequenceVolatile(final int delta) {
        return (int) SEQUENCE_HANDLE.getAndAdd(this, delta);
    }

    private int addAndGetSequenceVolatile(final int delta) {
        return (int) SEQUENCE_HANDLE.getAndAdd(this, delta) + delta;
    }

    /**
     * Gets the current sequence number.
     * This is primarily for debugging purposes.
     *
     * @return The current sequence number
     */
    public int getSequence() {
        return this.getSequenceVolatile();
    }

    /**
     * Checks if a write operation is currently in progress.
     *
     * @return true if a write is in progress, false otherwise
     */
    public boolean isWriteLocked() {
        return (this.getSequenceVolatile() & 1) != 0;
    }

    @Override
    public String toString() {
        return "WeakSeqLock{sequence=" + this.getSequence() + ", writeLocked=" + this.isWriteLocked() + "}";
    }
}
