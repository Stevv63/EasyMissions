package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.util.MatchWildCard;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public record RegistryMatcher<T extends Keyed>(Set<T> values, boolean any) implements ValueMatcher<T> {

    public static <T extends Keyed> RegistryMatcher<T> parse(Registry<T> registry, Set<String> targets) {
        if (targets.contains("*") || targets.isEmpty()) return new RegistryMatcher<>(Collections.emptySet(), true);

        HashSet<T> set = new HashSet<>();

        for (String target : targets) {
            target = target.toLowerCase(Locale.ROOT).trim();

            if (target.indexOf('*') != -1) {
                boolean foundAny = false;
                for (T constant : registry) {
                    if (MatchWildCard.matchesTarget(target, constant.getKey().asMinimalString())) {
                        set.add(constant);
                        foundAny = true;
                    }
                }
                if (!foundAny) {
                    throw new ConfigException("Wildcard '" + target + "' did not match any existing " + registry.getClass().getSimpleName());
                }
            } else {
                NamespacedKey key = NamespacedKey.fromString(target);
                if (key == null)
                    throw new ConfigException("Invalid NamespacedKey format: '" + target + "'. Must be 'namespace:key'.");


                T value = registry.get(key);
                if (value == null)
                    throw new ConfigException("Invalid registry key: '" + key + "'. Must be 'namespace:key' in " + registry.getClass().getSimpleName());

                set.add(value);
            }
        }
        return new RegistryMatcher<>(set, false);
    }

    public boolean matches(T value) {
        return any || values.contains(value);
    }
}
