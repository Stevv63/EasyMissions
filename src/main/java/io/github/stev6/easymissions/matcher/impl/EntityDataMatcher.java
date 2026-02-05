package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class EntityDataMatcher implements ValueMatcher<Entity> {
    private final Predicate<Entity> predicate;

    public EntityDataMatcher(Predicate<Entity> predicate) {
        this.predicate = predicate;
    }

    public static EntityDataMatcher parse(@NotNull ConfigurationSection section) {
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

        ConfigurationSection equipment = section.getConfigurationSection("equipment");
        if (equipment != null) {
            ConfigurationSection mainhand = equipment.getConfigurationSection("mainhand");
            ConfigurationSection offhand = equipment.getConfigurationSection("offhand");
            ConfigurationSection helmet = equipment.getConfigurationSection("helmet");
            ConfigurationSection chestplate = equipment.getConfigurationSection("chestplate");
            ConfigurationSection leggings = equipment.getConfigurationSection("leggings");
            ConfigurationSection boots = equipment.getConfigurationSection("boots");

            ItemDataMatcher mainhandMatcher = mainhand != null ? ItemDataMatcher.parse(mainhand) : null;
            ItemDataMatcher offhandMatcher = offhand != null ? ItemDataMatcher.parse(offhand) : null;
            ItemDataMatcher helmetMatcher = helmet != null ? ItemDataMatcher.parse(helmet) : null;
            ItemDataMatcher chestMatcher = chestplate != null ? ItemDataMatcher.parse(chestplate) : null;
            ItemDataMatcher leggingsMatcher = leggings != null ? ItemDataMatcher.parse(leggings) : null;
            ItemDataMatcher bootsMatcher = boots != null ? ItemDataMatcher.parse(boots) : null;

            predicate = predicate.and(e -> {
                if (!(e instanceof LivingEntity lE)) return false;

                var eq = lE.getEquipment();
                if (eq == null) return false;

                if (mainhandMatcher != null && !mainhandMatcher.matches(eq.getItemInMainHand())) return false;
                if (offhandMatcher != null && !offhandMatcher.matches(eq.getItemInOffHand())) return false;
                if (helmetMatcher != null && !helmetMatcher.matches(eq.getHelmet())) return false;
                if (chestMatcher != null && !chestMatcher.matches(eq.getChestplate())) return false;
                if (leggingsMatcher != null && !leggingsMatcher.matches(eq.getLeggings())) return false;
                return bootsMatcher == null || bootsMatcher.matches(eq.getBoots());
            });
        }

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