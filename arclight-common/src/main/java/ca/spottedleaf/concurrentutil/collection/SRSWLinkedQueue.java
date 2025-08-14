package ca.spottedleaf.concurrentutil.collection;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import ca.spottedleaf.concurrentutil.util.Validate;

import java.lang.invoke.VarHandle;

/**
 * Single-reader single-writer linked queue implementation.
 * This queue is optimized for scenarios where there is exactly one reader thread
 * and exactly one writer thread.
 * <p>
 * This implementation is from Paper patch 0007 (ConcurrentUtil.patch).
 */
public final class SRSWLinkedQueue<E> {

    protected static final VarHandle HEAD_HANDLE = ConcurrentUtil.getVarHandle(SRSWLinkedQueue.class, "head", LinkedNode.class);
    protected static final VarHandle TAIL_HANDLE = ConcurrentUtil.getVarHandle(SRSWLinkedQueue.class, "tail", LinkedNode.class);
    // Always non-null, high chance of being the actual head
    protected volatile LinkedNode<E> head;
    // Always non-null, high chance of being the actual tail
    protected volatile LinkedNode<E> tail;

    public SRSWLinkedQueue() {
        final LinkedNode<E> dummy = new LinkedNode<>(null, null);
        this.head = dummy;
        this.tail = dummy;
    }

    /**
     * Must be called by the reader thread.
     */
    public E poll() {
        final LinkedNode<E> head = this.head;
        final LinkedNode<E> next = head.getNextVolatile();

        if (next == null) {
            return null;
        }

        final E ret = next.getElementPlain();

        // Update head
        this.head = next;

        return ret;
    }

    /**
     * Must be called by the reader thread.
     */
    public E peek() {
        final LinkedNode<E> head = this.head;
        final LinkedNode<E> next = head.getNextVolatile();

        if (next == null) {
            return null;
        }

        return next.getElementPlain();
    }

    /**
     * Must be called by the writer thread.
     */
    public void offer(final E element) {
        Validate.notNull(element, "Null element");

        final LinkedNode<E> newTail = new LinkedNode<>(element, null);
        final LinkedNode<E> oldTail = this.tail;

        oldTail.setNextRelease(newTail);
        this.tail = newTail;
    }

    /**
     * Returns whether this queue is empty.
     * This method is safe to call from any thread.
     */
    public boolean isEmpty() {
        return this.head.getNextVolatile() == null;
    }

    /**
     * Returns the approximate size of this queue.
     * This method is safe to call from any thread, but the result may be stale.
     */
    public int size() {
        int size = 0;
        for (LinkedNode<E> curr = this.head.getNextVolatile(); curr != null; curr = curr.getNextVolatile()) {
            ++size;
        }
        return size;
    }

    protected static final class LinkedNode<E> {
        protected static final VarHandle ELEMENT_HANDLE = ConcurrentUtil.getVarHandle(LinkedNode.class, "element", Object.class);
        protected static final VarHandle NEXT_HANDLE = ConcurrentUtil.getVarHandle(LinkedNode.class, "next", LinkedNode.class);
        protected volatile Object element;
        protected volatile LinkedNode<E> next;

        protected LinkedNode(final Object element, final LinkedNode<E> next) {
            this.element = element;
            this.next = next;
        }

        /* element */

        @SuppressWarnings("unchecked")
        protected final E getElementPlain() {
            return (E) ELEMENT_HANDLE.get(this);
        }

        protected final void setElementPlain(final Object element) {
            ELEMENT_HANDLE.set(this, element);
        }

        @SuppressWarnings("unchecked")
        protected final E getElementVolatile() {
            return (E) ELEMENT_HANDLE.getVolatile(this);
        }

        protected final void setElementVolatile(final Object element) {
            ELEMENT_HANDLE.setVolatile(this, element);
        }

        /* next */

        @SuppressWarnings("unchecked")
        protected final LinkedNode<E> getNextPlain() {
            return (LinkedNode<E>) NEXT_HANDLE.get(this);
        }

        protected final void setNextPlain(final LinkedNode<E> next) {
            NEXT_HANDLE.set(this, next);
        }

        @SuppressWarnings("unchecked")
        protected final LinkedNode<E> getNextVolatile() {
            return (LinkedNode<E>) NEXT_HANDLE.getVolatile(this);
        }

        protected final void setNextVolatile(final LinkedNode<E> next) {
            NEXT_HANDLE.setVolatile(this, next);
        }

        protected final void setNextRelease(final LinkedNode<E> next) {
            NEXT_HANDLE.setRelease(this, next);
        }

        @SuppressWarnings("unchecked")
        protected final LinkedNode<E> getNextAcquire() {
            return (LinkedNode<E>) NEXT_HANDLE.getAcquire(this);
        }
    }
}
