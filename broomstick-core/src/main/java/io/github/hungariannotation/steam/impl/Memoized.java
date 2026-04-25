package io.github.hungariannotation.steam.impl;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Memoizing supplier.
 * 
 * @param <T>
 */
public class Memoized<T> implements Supplier<T> {
    private final Object lock = new Object();

    private static final Supplier<Void> COMPLETED = () -> {
        throw new IllegalStateException();
    };

    private volatile Supplier<T> delegate;
    private T value;

    Memoized(Supplier<T> source) {
        this.delegate = Objects.requireNonNull(source);
    }

    public T get() {
        if (delegate != COMPLETED) {
            synchronized (lock) {
                if (delegate != COMPLETED) {
                    T memoizedValue = Objects.requireNonNull(delegate.get());
                    this.value = memoizedValue;
                    @SuppressWarnings("unchecked")
                    Supplier<T> completed = (Supplier<T>) COMPLETED;
                    delegate = completed;
                    return memoizedValue;
                }
            }
        }

        T result = value;
        return result;
    }

    public static <T> Memoized<T> of(Supplier<T> supplier) {
        return new Memoized<>(supplier);
    }
}
