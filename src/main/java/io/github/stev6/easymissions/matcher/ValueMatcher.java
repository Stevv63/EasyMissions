package io.github.stev6.easymissions.matcher;

import java.util.function.Predicate;

public interface ValueMatcher<T> extends Predicate<T> {

    boolean matches(T value);

    @Override
    default boolean test(T value) {
        return matches(value);
    }
}
