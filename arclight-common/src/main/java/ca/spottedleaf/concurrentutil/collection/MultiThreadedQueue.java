package ca.spottedleaf.concurrentutil.collection;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import ca.spottedleaf.concurrentutil.util.Validate;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * MT-Safe linked first in first out ordered queue.
 * <p>
 * This queue should out-perform {@link java.util.concurrent.ConcurrentLinkedQueue} in high-contention reads/writes, and is
 * not any slower in lower contention reads/writes.
 * <p>
 * Note that this queue breaks the specification laid out by {@link Collection}, see {@link #preventAdds()} and {@link Collection#add(Object)}.
 * </p>
 * <p><b>
 * This queue will only unlink linked nodes through the {@link #peek()} and {@link #poll()} methods, and this is only if
 * they are at the head of the queue.
 * </b></p>
 *
 * @param <E> Type of element in this queue.
 */
public class MultiThreadedQueue<E> implements Queue<E> {

    protected static final VarHandle HEAD_HANDLE = ConcurrentUtil.getVarHandle(MultiThreadedQueue.class, "head", LinkedNode.class);
    protected static final VarHandle TAIL_HANDLE = ConcurrentUtil.getVarHandle(MultiThreadedQueue.class, "tail", LinkedNode.class);

    /* Note that it is possible to reach head from tail. */

    /* IMPL NOTE: Leave hashCode and equals to their defaults */
    protected volatile LinkedNode<E> head; /* Always non-null, high chance of being the actual head */
    protected volatile LinkedNode<E> tail; /* Always non-null, high chance of being the actual tail */

    /* head */

    /**
     * Constructs a {@code MultiThreadedQueue}, initially empty.
     * <p>
     * The returned object may not be published without synchronization.
     * </p>
     */
    public MultiThreadedQueue() {
        final LinkedNode<E> value = new LinkedNode<>(null, null);
        this.setHeadPlain(value);
        this.setTailPlain(value);
    }

    /**
     * Constructs a {@code MultiThreadedQueue}, initially containing all elements in the specified {@code collection}.
     * <p>
     * The returned object may not be published without synchronization.
     * </p>
     *
     * @param collection The specified collection.
     * @throws NullPointerException If {@code collection} is {@code null} or contains {@code null} elements.
     */
    public MultiThreadedQueue(final Iterable<? extends E> collection) {
        final Iterator<? extends E> elements = collection.iterator();

        if (!elements.hasNext()) {
            final LinkedNode<E> value = new LinkedNode<>(null, null);
            this.setHeadPlain(value);
            this.setTailPlain(value);
            return;
        }

        final LinkedNode<E> head = new LinkedNode<>(Validate.notNull(elements.next(), "Null element"), null);
        LinkedNode<E> tail = head;

        while (elements.hasNext()) {
            final LinkedNode<E> next = new LinkedNode<>(Validate.notNull(elements.next(), "Null element"), null);
            tail.setNextPlain(next);
            tail = next;
        }

        this.setHeadPlain(head);
        this.setTailPlain(tail);
    }

    @SuppressWarnings("unchecked")
    protected final LinkedNode<E> getHeadPlain() {
        return (LinkedNode<E>) HEAD_HANDLE.get(this);
    }

    protected final void setHeadPlain(final LinkedNode<E> newHead) {
        HEAD_HANDLE.set(this, newHead);
    }

    @SuppressWarnings("unchecked")
    protected final LinkedNode<E> getHeadOpaque() {
        return (LinkedNode<E>) HEAD_HANDLE.getOpaque(this);
    }

    /* tail */

    protected final void setHeadOpaque(final LinkedNode<E> newHead) {
        HEAD_HANDLE.setOpaque(this, newHead);
    }

    @SuppressWarnings("unchecked")
    protected final LinkedNode<E> getHeadAcquire() {
        return (LinkedNode<E>) HEAD_HANDLE.getAcquire(this);
    }

    @SuppressWarnings("unchecked")
    protected final LinkedNode<E> getTailPlain() {
        return (LinkedNode<E>) TAIL_HANDLE.get(this);
    }

    protected final void setTailPlain(final LinkedNode<E> newTail) {
        TAIL_HANDLE.set(this, newTail);
    }

    @SuppressWarnings("unchecked")
    protected final LinkedNode<E> getTailOpaque() {
        return (LinkedNode<E>) TAIL_HANDLE.getOpaque(this);
    }

    protected final void setTailOpaque(final LinkedNode<E> newTail) {
        TAIL_HANDLE.setOpaque(this, newTail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove() throws NoSuchElementException {
        final E ret = this.poll();

        if (ret == null) {
            throw new NoSuchElementException();
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Contrary to the specification of {@link Collection#add}, this method will fail to add the element to this queue
     * and return {@code false} if this queue is add-blocked.
     * </p>
     */
    @Override
    public boolean add(final E element) {
        return this.offer(element);
    }

    /**
     * Adds the specified element to the tail of this queue. If this queue is currently add-locked, then the queue is
     * released from that lock and this element is added. The unlock operation and addition of the specified
     * element is atomic.
     *
     * @param element The specified element.
     * @return {@code true} if this queue previously allowed additions
     */
    public boolean forceAdd(final E element) {
        final LinkedNode<E> node = new LinkedNode<>(element, null);

        return !this.forceAppendList(node, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E element() throws NoSuchElementException {
        final E ret = this.peek();

        if (ret == null) {
            throw new NoSuchElementException();
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method may also return {@code false} to indicate an element was not added if this queue is add-blocked.
     * </p>
     */
    @Override
    public boolean offer(final E element) {
        Validate.notNull(element, "Null element");

        final LinkedNode<E> node = new LinkedNode<>(element, null);

        return this.appendList(node, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E peek() {
        for (LinkedNode<E> head = this.getHeadOpaque(), curr = head; ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain(); /* Likely in sync */

            if (element != null) {
                if (this.getHeadOpaque() == head && curr != head) {
                    this.setHeadOpaque(curr);
                }
                return element;
            }

            if (next == null || curr == next) {
                return null;
            }
            curr = next;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E poll() {
        return this.removeHead();
    }

    /**
     * Retrieves and removes the head of this queue if it matches the specified predicate. If this queue is empty
     * or the head does not match the predicate, this function returns {@code null}.
     * <p>
     * The predicate may be invoked multiple or no times in this call.
     * </p>
     *
     * @param predicate The specified predicate.
     * @return The head if it matches the predicate, or {@code null} if it did not or this queue is empty.
     */
    public E pollIf(final Predicate<E> predicate) {
        return this.removeHead(Validate.notNull(predicate, "Null predicate"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        //noinspection StatementWithEmptyBody
        while (this.poll() != null) ;
    }

    /**
     * Prevents elements from being added to this queue. Once this is called, any attempt to add to this queue will fail.
     * <p>
     * This function is MT-Safe.
     * </p>
     *
     * @return {@code true} if the queue was modified to prevent additions, {@code false} if it already prevented additions.
     */
    public boolean preventAdds() {
        final LinkedNode<E> deadEnd = new LinkedNode<>(null, null);
        deadEnd.setNextPlain(deadEnd);

        if (!this.appendList(deadEnd, deadEnd)) {
            return false;
        }

        this.setTailPlain(deadEnd); /* (try to) Ensure tail is set for the following #allowAdds call */
        return true;
    }

    // Additional methods will be added via str-replace-editor due to length constraints

    @Override
    public int size() {
        int size = 0;
        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null) {
                ++size;
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return this.peek() == null;
    }

    @Override
    public boolean contains(final Object object) {
        Validate.notNull(object, "Null object");

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null && (element == object || element.equals(object))) {
                return true;
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedIterator<>(this.getHeadOpaque());
    }

    @Override
    public Object[] toArray() {
        final List<E> ret = new ArrayList<>();

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null) {
                ret.add(element);
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return ret.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        final List<T> ret = new ArrayList<>();

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null) {
                ret.add((T) element);
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return ret.toArray(array);
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return this.toArray(generator.apply(0));
    }

    @Override
    public boolean remove(final Object object) {
        Validate.notNull(object, "Null object");

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null && (element == object || element.equals(object))) {
                if (curr.getAndSetElementVolatile(null) != null) {
                    return true;
                }
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        Validate.notNull(collection, "Null collection");

        for (final Object element : collection) {
            if (!this.contains(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> collection) {
        Validate.notNull(collection, "Null collection");

        if (collection.isEmpty()) {
            return false;
        }

        final Iterator<? extends E> elements = collection.iterator();

        final LinkedNode<E> head = new LinkedNode<>(Validate.notNull(elements.next(), "Null element"), null);
        LinkedNode<E> tail = head;

        while (elements.hasNext()) {
            final LinkedNode<E> next = new LinkedNode<>(Validate.notNull(elements.next(), "Null element"), null);
            tail.setNextPlain(next);
            tail = next;
        }

        return this.appendList(head, tail);
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        Validate.notNull(collection, "Null collection");

        if (collection.isEmpty()) {
            return false;
        }

        boolean modified = false;

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null && collection.contains(element)) {
                if (curr.getAndSetElementVolatile(null) != null) {
                    modified = true;
                }
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return modified;
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        Validate.notNull(collection, "Null collection");

        boolean modified = false;

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null && !collection.contains(element)) {
                if (curr.getAndSetElementVolatile(null) != null) {
                    modified = true;
                }
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }

        return modified;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        Validate.notNull(action, "Null action");

        for (LinkedNode<E> curr = this.getHeadOpaque(); ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E element = curr.getElementPlain();

            if (element != null) {
                action.accept(element);
            }

            if (next == null || next == curr) {
                break;
            }
            curr = next;
        }
    }

    /**
     * Allows elements to be added to this queue again. Note that this function has undefined behaviour if this queue
     * currently allows additions.
     * <p>
     * This function is MT-Safe.
     * </p>
     *
     * @return {@code true} if the queue was modified to allow additions, {@code false} if it already allowed additions.
     */
    public boolean allowAdds() {
        for (LinkedNode<E> tail = this.getTailOpaque(); ; ) {
            final LinkedNode<E> next = tail.getNextVolatile();

            if (next == null) {
                return false; /* Additions already allowed */
            }

            if (next != tail) {
                /* Try to update tail */
                if (this.getTailOpaque() == tail) {
                    this.setTailOpaque(next);
                }
                tail = this.getTailOpaque();
                continue;
            }

            /* next == tail, so this is the node we need to replace */
            final LinkedNode<E> compared = tail.compareExchangeNextVolatile(next, null);

            if (compared != next) {
                /* Tail changed, try again */
                tail = this.getTailOpaque();
                continue;
            }

            /* Successfully allowed additions */
            return true;
        }
    }

    // return true if successful, false otherwise
    protected final boolean appendList(final LinkedNode<E> head, final LinkedNode<E> tail) {
        int failures = 0;

        for (LinkedNode<E> currTail = this.getTailOpaque(), curr = currTail; ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();

            if (next == curr) {
                /* Additions are stopped */
                return false;
            }

            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }

            if (next == null) {
                final LinkedNode<E> compared = curr.compareExchangeNextVolatile(null, head);

                if (compared == null) {
                    /* Added */
                    if (this.getTailOpaque() == currTail) {
                        this.setTailOpaque(tail);
                    }
                    return true;
                }

                ++failures;
                curr = compared;
                continue;
            }

            if (curr == currTail) {
                curr = next;
            } else {
                if (currTail == (currTail = this.getTailOpaque())) {
                    curr = next;
                } else {
                    curr = currTail;
                }
            }
        }
    }

    // return true if normal addition, false if the queue previously disallowed additions
    protected final boolean forceAppendList(final LinkedNode<E> head, final LinkedNode<E> tail) {
        int failures = 0;

        for (LinkedNode<E> currTail = this.getTailOpaque(), curr = currTail; ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();

            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }

            if (next == null || next == curr) {
                final LinkedNode<E> compared = curr.compareExchangeNextVolatile(next, head);

                if (compared == next) {
                    /* Added */
                    if (this.getTailOpaque() == currTail) {
                        this.setTailOpaque(tail);
                    }
                    return next != curr;
                }

                ++failures;
                curr = compared;
                continue;
            }

            if (curr == currTail) {
                curr = next;
            } else {
                if (currTail == (currTail = this.getTailOpaque())) {
                    curr = next;
                } else {
                    curr = currTail;
                }
            }
        }
    }

    protected final E removeHead() {
        int failures = 0;
        for (LinkedNode<E> head = this.getHeadOpaque(), curr = head; ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E currentVal = curr.getElementPlain();

            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }

            if (currentVal != null) {
                if (curr.getAndSetElementVolatile(null) == null) {
                    if (curr == (curr = next) || next == null) {
                        return null;
                    }
                    ++failures;
                    continue;
                }

                if (this.getHeadOpaque() == head) {
                    this.setHeadOpaque(next != null ? next : curr);
                }

                return currentVal;
            }

            if (curr == next || next == null) {
                if (curr != head && this.getHeadOpaque() == head) {
                    this.setHeadOpaque(curr);
                }
                return null;
            }

            if (head == curr) {
                curr = next;
            } else {
                if (head == (head = this.getHeadOpaque())) {
                    curr = next;
                } else {
                    curr = head;
                }
            }
        }
    }

    protected final E removeHead(final Predicate<E> predicate) {
        int failures = 0;
        for (LinkedNode<E> head = this.getHeadOpaque(), curr = head; ; ) {
            final LinkedNode<E> next = curr.getNextVolatile();
            final E currentVal = curr.getElementPlain();

            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }

            if (currentVal != null) {
                if (!predicate.test(currentVal)) {
                    if (curr != head && this.getHeadOpaque() == head) {
                        this.setHeadOpaque(curr);
                    }
                    return null;
                }
                if (curr.getAndSetElementVolatile(null) == null) {
                    if (curr == (curr = next) || next == null) {
                        return null;
                    }
                    ++failures;
                    continue;
                }

                if (this.getHeadOpaque() == head) {
                    this.setHeadOpaque(next != null ? next : curr);
                }

                return currentVal;
            }

            if (curr == next || next == null) {
                if (curr != head && this.getHeadOpaque() == head) {
                    this.setHeadOpaque(curr);
                }
                return null;
            }

            if (head == curr) {
                curr = next;
            } else {
                if (head == (head = this.getHeadOpaque())) {
                    curr = next;
                } else {
                    curr = head;
                }
            }
        }
    }

    // Inner classes
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
        protected E getElementPlain() {
            return (E) ELEMENT_HANDLE.get(this);
        }

        protected void setElementPlain(final Object element) {
            ELEMENT_HANDLE.set(this, element);
        }

        @SuppressWarnings("unchecked")
        protected E getElementVolatile() {
            return (E) ELEMENT_HANDLE.getVolatile(this);
        }

        protected void setElementVolatile(final Object element) {
            ELEMENT_HANDLE.setVolatile(this, element);
        }

        @SuppressWarnings("unchecked")
        protected E getAndSetElementVolatile(final Object element) {
            return (E) ELEMENT_HANDLE.getAndSet(this, element);
        }

        @SuppressWarnings("unchecked")
        protected E compareExchangeElementVolatile(final Object expect, final Object update) {
            return (E) ELEMENT_HANDLE.compareAndExchange(this, expect, update);
        }

        /* next */

        @SuppressWarnings("unchecked")
        protected LinkedNode<E> getNextPlain() {
            return (LinkedNode<E>) NEXT_HANDLE.get(this);
        }

        protected void setNextPlain(final LinkedNode<E> next) {
            NEXT_HANDLE.set(this, next);
        }

        @SuppressWarnings("unchecked")
        protected LinkedNode<E> getNextVolatile() {
            return (LinkedNode<E>) NEXT_HANDLE.getVolatile(this);
        }

        protected void setNextVolatile(final LinkedNode<E> next) {
            NEXT_HANDLE.setVolatile(this, next);
        }

        @SuppressWarnings("unchecked")
        protected LinkedNode<E> compareExchangeNextVolatile(final LinkedNode<E> expect, final LinkedNode<E> update) {
            return (LinkedNode<E>) NEXT_HANDLE.compareAndExchange(this, expect, update);
        }
    }

    protected static final class LinkedIterator<E> implements Iterator<E> {
        protected LinkedNode<E> curr;
        protected LinkedNode<E> next;
        protected E nextElement;

        protected LinkedIterator(final LinkedNode<E> start) {
            this.curr = null;
            this.next = start;
            this.advance();
        }

        protected void advance() {
            if (this.next == null) {
                this.nextElement = null;
                return;
            }

            while (this.next != null) {
                final E element = this.next.getElementVolatile();
                final LinkedNode<E> next = this.next.getNextVolatile();

                if (element != null) {
                    this.nextElement = element;
                    if (next == this.next) {
                        this.next = null;
                    } else {
                        this.next = next;
                    }
                    return;
                }

                if (this.next == next) {
                    this.next = null;
                    break;
                }

                this.next = next;
            }

            this.nextElement = null;
        }

        @Override
        public boolean hasNext() {
            return this.nextElement != null;
        }

        @Override
        public E next() {
            final E element = this.nextElement;
            if (element == null) {
                throw new NoSuchElementException();
            }

            this.curr = this.next;
            this.advance();

            return element;
        }

        @Override
        public void remove() {
            final LinkedNode<E> curr = this.curr;
            if (curr == null) {
                throw new IllegalStateException();
            }

            this.curr = null;
            curr.setElementVolatile(null);
        }
    }
}
