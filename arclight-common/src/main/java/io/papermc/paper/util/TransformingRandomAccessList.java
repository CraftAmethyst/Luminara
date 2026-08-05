package io.papermc.paper.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A mutable transforming view backed by another list.
 *
 * @param <F> backing element type
 * @param <T> transformed element type
 */
public final class TransformingRandomAccessList<F, T>
    extends AbstractList<T>
    implements RandomAccess {

    private final List<F> fromList;
    private final Function<? super F, ? extends T> toFunction;
    private final Function<? super T, ? extends F> fromFunction;

    public TransformingRandomAccessList(
        final @NotNull List<F> fromList,
        final @NotNull Function<? super F, ? extends T> toFunction,
        final @NotNull Function<? super T, ? extends F> fromFunction
    ) {
        this.fromList = Objects.requireNonNull(fromList, "fromList");
        this.toFunction = Objects.requireNonNull(toFunction, "toFunction");
        this.fromFunction = Objects.requireNonNull(
            fromFunction,
            "fromFunction"
        );
    }

    @Override
    public void clear() {
        this.fromList.clear();
    }

    @Override
    public T get(int index) {
        return this.toFunction.apply(this.fromList.get(index));
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.listIterator();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        return new TransformedListIterator<F, T>(
            this.fromList.listIterator(index)
        ) {
            @Override
            T transform(F from) {
                return TransformingRandomAccessList.this.toFunction.apply(from);
            }

            @Override
            F transformBack(T to) {
                return TransformingRandomAccessList.this.fromFunction.apply(to);
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return this.fromList.isEmpty();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        Objects.requireNonNull(filter, "filter");
        return this.fromList.removeIf(element ->
            filter.test(this.toFunction.apply(element))
        );
    }

    @Override
    public T remove(int index) {
        return this.toFunction.apply(this.fromList.remove(index));
    }

    @Override
    public int size() {
        return this.fromList.size();
    }

    @Override
    public T set(int index, T element) {
        return this.toFunction.apply(
            this.fromList.set(index, this.fromFunction.apply(element))
        );
    }

    @Override
    public void add(int index, T element) {
        this.fromList.add(index, this.fromFunction.apply(element));
    }

    abstract static class TransformedListIterator<
        F,
        T
        > implements ListIterator<T>, Iterator<T> {

        final Iterator<F> backingIterator;

        TransformedListIterator(ListIterator<F> backingIterator) {
            this.backingIterator = Objects.requireNonNull(
                backingIterator,
                "backingIterator"
            );
        }

        @SuppressWarnings("unchecked")
        static <A> ListIterator<A> cast(Iterator<A> iterator) {
            return (ListIterator<A>) iterator;
        }

        private ListIterator<F> backingIterator() {
            return cast(this.backingIterator);
        }

        @Override
        public final boolean hasPrevious() {
            return this.backingIterator().hasPrevious();
        }

        @Override
        public final T previous() {
            return this.transform(this.backingIterator().previous());
        }

        @Override
        public final int nextIndex() {
            return this.backingIterator().nextIndex();
        }

        @Override
        public final int previousIndex() {
            return this.backingIterator().previousIndex();
        }

        @Override
        public void set(T element) {
            this.backingIterator().set(this.transformBack(element));
        }

        @Override
        public void add(T element) {
            this.backingIterator().add(this.transformBack(element));
        }

        abstract T transform(F from);

        abstract F transformBack(T to);

        @Override
        public final boolean hasNext() {
            return this.backingIterator.hasNext();
        }

        @Override
        public final T next() {
            return this.transform(this.backingIterator.next());
        }

        @Override
        public final void remove() {
            this.backingIterator.remove();
        }
    }
}
