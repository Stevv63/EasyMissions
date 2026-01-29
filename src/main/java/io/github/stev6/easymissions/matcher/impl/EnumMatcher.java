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

public record EnumMatcher<E extends Enum<E>>(@NotNull Set<E> values, boolean any) implements ValueMatcher<E> {

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
