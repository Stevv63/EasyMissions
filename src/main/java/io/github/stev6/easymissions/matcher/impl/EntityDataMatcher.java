package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class EntityDataMatcher implements ValueMatcher<Entity> {
    private final Predicate<Entity> predicate;

    public EntityDataMatcher(Predicate<Entity> predicate) {
        this.predicate = predicate;
    }

    public static EntityDataMatcher parse(ConfigurationSection section) {
        Predicate<Entity> predicate = Objects::nonNull;

        HashSet<String> types = new HashSet<>(section.getStringList("types"));
        if (!types.isEmpty()) {
            EnumMatcher<EntityType> matcher = EnumMatcher.parse(EntityType.class, types);
            predicate = predicate.and(e -> matcher.matches(e.getType()));
        }

        boolean adultOnly = section.getBoolean("adult", false);
        boolean babyOnly = section.getBoolean("baby", false);

        if (babyOnly && adultOnly) throw new ConfigException("Cannot have both 'adult' and 'baby' set to true.");

        if (adultOnly) predicate = predicate.and(e -> e instanceof Ageable ageable && ageable.isAdult());
        if (babyOnly) predicate = predicate.and(e -> e instanceof Ageable ageable && !ageable.isAdult());


        boolean monsterOnly = section.getBoolean("monster", false);
        if (monsterOnly) predicate = predicate.and(e -> e instanceof Monster);

        HashSet<String> pdcKeys = new HashSet<>(section.getStringList("pdc"));
        if (!pdcKeys.isEmpty()) {
            for (String key : pdcKeys) {
                NamespacedKey namespaceKey = NamespacedKey.fromString(key);
                if (namespaceKey == null)
                    throw new ConfigException("Invalid NamespacedKey format: '" + key + "'. Must be 'namespace:key'.");

                predicate = predicate.and(e -> e.getPersistentDataContainer().has(namespaceKey));
            }
        }
        return new EntityDataMatcher(predicate);
    }

    @Override
    public boolean matches(Entity value) {
        return predicate.test(value);
    }
}