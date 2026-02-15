/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2026 Stev6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
