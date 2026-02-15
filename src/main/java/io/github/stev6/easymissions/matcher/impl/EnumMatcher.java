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

package io.github.stev6.easymissions.matcher.impl;

import com.google.common.base.Enums;
import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.util.MatchWildCard;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * {@link ValueMatcher} implementation that matches an Enum against a configured set of enum values.
 * <p>
 * Supports:
 * <ul>
 *     <li>Exact matches (e.g., "DIAMOND_ORE")</li>
 *     <li>Wildcards (e.g., "*_ORE", "STRIPPED_*", "*")</li>
 * </ul>
 * @param <E> The enum type
 */
public record EnumMatcher<E extends Enum<E>>(@NotNull Set<E> values, boolean any) implements ValueMatcher<E> {

    /**
     * Parses a set of strings from the config into an EnumMatcher.
     *
     * @param enumClass The class of the Enum (e.g., Material.class).
     * @param targets The set of strings from the config, or anywhere you want.
     * @param <T> The Enum type.
     * @return A new matcher containing the resolved enums.
     * @throws ConfigException If a value cannot be found or a wildcard matches nothing.
     */
    public static <T extends Enum<T>> EnumMatcher<T> parse(Class<T> enumClass, Set<String> targets) {
        if (targets.isEmpty() || targets.contains("*"))
            return new EnumMatcher<>(Collections.<T>emptySet(), true); // no intellij, they cannot be inferred

        EnumSet<T> set = EnumSet.noneOf(enumClass);
        T[] allConstants = enumClass.getEnumConstants();

        for (String target : targets) {
            target = target.toUpperCase(Locale.ROOT).trim();

            var exact = Enums.getIfPresent(enumClass, target);

            if (exact.isPresent()) {
                set.add(exact.get());
                continue;
            }

            if (target.indexOf('*') != -1) {
                boolean found = false;
                for (T constant : allConstants) {
                    if (MatchWildCard.matchesTarget(target, constant.name())) {
                        set.add(constant);
                        found = true;
                    }
                }
                if (!found)
                    throw new ConfigException("Wildcard '" + target + "' did not match any existing " + enumClass.getSimpleName());

            } else throw new ConfigException("Invalid " + enumClass.getSimpleName() + ": " + target);

        }

        return new EnumMatcher<>(set, false);
    }

    public boolean matches(E value) {
        return any || values.contains(value);
    }
}
