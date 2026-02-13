package io.github.stev6.easymissions.matcher;

import java.util.function.Predicate;

/**
 * Functional interface used to define matching logic for specific values or objects.
 * This extends {@link Predicate} to provide a more semantic {@link #matches(Object)} method.
 * <p>
 * This helps reuse matching code and keep it out of mission type declarations
 *
 * @param <T> The type of object being matched.
 * @see io.github.stev6.easymissions.matcher.impl.EnumMatcher
 * @see io.github.stev6.easymissions.matcher.impl.RegistryMatcher
 */
public interface ValueMatcher<T> extends Predicate<T> {

    /**
     * Checks if the value satisfies the matching criteria.
     * @param value The value to check.
     * @return true if it matches.
     */
    boolean matches(T value);

    @Override
    default boolean test(T value) {
        return matches(value);
    }
}
