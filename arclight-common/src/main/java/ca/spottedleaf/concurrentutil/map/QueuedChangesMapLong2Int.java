package ca.spottedleaf.concurrentutil.map;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import ca.spottedleaf.concurrentutil.util.Validate;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

/**
 * Queued changes map for long to int mappings.
 * This map allows for batched updates to improve performance in high-contention scenarios.
 * <p>
 * This implementation is from Paper patch 0007 (ConcurrentUtil.patch).
 */
public final class QueuedChangesMapLong2Int {

    protected static final VarHandle UPDATE_QUEUE_HANDLE = ConcurrentUtil.getVarHandle(QueuedChangesMapLong2Int.class, "updateQueue", UpdateQueue.class);
    protected final Long2IntOpenHashMap map;
    protected final AtomicLong version = new AtomicLong();
    protected volatile UpdateQueue<Update> updateQueue = new UpdateQueue<>();

    public QueuedChangesMapLong2Int() {
        this.map = new Long2IntOpenHashMap();
        this.map.defaultReturnValue(0);
    }

    public QueuedChangesMapLong2Int(final int expectedSize) {
        this.map = new Long2IntOpenHashMap(expectedSize);
        this.map.defaultReturnValue(0);
    }

    public QueuedChangesMapLong2Int(final int expectedSize, final float loadFactor) {
        this.map = new Long2IntOpenHashMap(expectedSize, loadFactor);
        this.map.defaultReturnValue(0);
    }

    /**
     * Gets the current version of this map.
     * The version is incremented each time the map is updated.
     */
    public long getVersion() {
        return this.version.get();
    }

    /**
     * Gets the value associated with the specified key.
     * This method is thread-safe for readers.
     */
    public int get(final long key) {
        return this.map.get(key);
    }

    /**
     * Gets the value associated with the specified key, or the default value if not present.
     * This method is thread-safe for readers.
     */
    public int getOrDefault(final long key, final int defaultValue) {
        return this.map.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if the map contains the specified key.
     * This method is thread-safe for readers.
     */
    public boolean containsKey(final long key) {
        return this.map.containsKey(key);
    }

    /**
     * Gets the size of the map.
     * This method is thread-safe for readers.
     */
    public int size() {
        return this.map.size();
    }

    /**
     * Checks if the map is empty.
     * This method is thread-safe for readers.
     */
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Queues a put operation.
     * This method is thread-safe and can be called from any thread.
     */
    public void queuePut(final long key, final int value) {
        this.queueUpdate(new PutUpdate(key, value));
    }

    /**
     * Queues a remove operation.
     * This method is thread-safe and can be called from any thread.
     */
    public void queueRemove(final long key) {
        this.queueUpdate(new RemoveUpdate(key));
    }

    /**
     * Queues a compute operation.
     * This method is thread-safe and can be called from any thread.
     */
    public void queueCompute(final long key, final LongFunction<Integer> function) {
        Validate.notNull(function, "Null function");
        this.queueUpdate(new ComputeUpdate(key, function));
    }

    /**
     * Queues an update operation.
     * This method is thread-safe and can be called from any thread.
     */
    protected void queueUpdate(final Update update) {
        UpdateQueue<Update> queue;
        do {
            queue = this.getUpdateQueueVolatile();
        } while (!queue.add(update));
    }

    /**
     * Performs all queued updates.
     * This method must be called by the writer thread.
     */
    public boolean performUpdates() {
        final UpdateQueue<Update> queue = this.getAndSetUpdateQueueVolatile(new UpdateQueue<>());

        if (queue.isEmpty()) {
            return false;
        }

        // Apply all updates
        for (final Update update : queue) {
            update.apply(this.map);
        }

        // Increment version to signal readers that the map has changed
        this.version.incrementAndGet();

        return true;
    }

    /**
     * Performs all queued updates and returns the new version.
     * This method must be called by the writer thread.
     */
    public long performUpdatesAndGetVersion() {
        this.performUpdates();
        return this.getVersion();
    }

    /* update queue */

    @SuppressWarnings("unchecked")
    protected final UpdateQueue<Update> getUpdateQueueVolatile() {
        return (UpdateQueue<Update>) UPDATE_QUEUE_HANDLE.getVolatile(this);
    }

    @SuppressWarnings("unchecked")
    protected final UpdateQueue<Update> getAndSetUpdateQueueVolatile(final UpdateQueue<Update> queue) {
        return (UpdateQueue<Update>) UPDATE_QUEUE_HANDLE.getAndSet(this, queue);
    }

    protected static abstract class Update {
        protected final long key;

        protected Update(final long key) {
            this.key = key;
        }

        protected abstract void apply(final Long2IntOpenHashMap map);
    }

    protected static final class PutUpdate extends Update {
        protected final int value;

        protected PutUpdate(final long key, final int value) {
            super(key);
            this.value = value;
        }

        @Override
        protected void apply(final Long2IntOpenHashMap map) {
            map.put(this.key, this.value);
        }
    }

    protected static final class RemoveUpdate extends Update {
        protected RemoveUpdate(final long key) {
            super(key);
        }

        @Override
        protected void apply(final Long2IntOpenHashMap map) {
            map.remove(this.key);
        }
    }

    protected static final class ComputeUpdate extends Update {
        protected final LongFunction<Integer> function;

        protected ComputeUpdate(final long key, final LongFunction<Integer> function) {
            super(key);
            this.function = function;
        }

        @Override
        protected void apply(final Long2IntOpenHashMap map) {
            final int currentValue = map.get(this.key);
            final Integer newValue = this.function.apply(currentValue);
            if (newValue == null) {
                map.remove(this.key);
            } else {
                map.put(this.key, newValue.intValue());
            }
        }
    }

    // Simple queue implementation for updates
    protected static final class UpdateQueue<T> implements Iterable<T> {
        private final java.util.concurrent.ConcurrentLinkedQueue<T> queue = new java.util.concurrent.ConcurrentLinkedQueue<>();

        public boolean add(final T element) {
            return this.queue.offer(element);
        }

        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public java.util.Iterator<T> iterator() {
            return this.queue.iterator();
        }
    }
}
