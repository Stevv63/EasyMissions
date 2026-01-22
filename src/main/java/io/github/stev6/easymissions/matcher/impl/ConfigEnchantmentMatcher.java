package io.github.stev6.easymissions.matcher.impl;

import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.util.IntRange;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record ConfigEnchantmentMatcher(Map<Enchantment, IntRange> targetEnchants, @Nullable IntRange globalRange,
                                       boolean matchAny) implements ValueMatcher<Map<Enchantment, Integer>> {

    @Nullable
    public static ConfigEnchantmentMatcher parse(@NotNull ConfigurationSection section, boolean matchAny) {
        Map<Enchantment, IntRange> targets = new HashMap<>();
        IntRange global = null;
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        for (String key : section.getKeys(false)) {
            String rangeStr = section.getString(key);
            IntRange range;

            try {
                range = rangeStr == null ? IntRange.ANY_RANGE : IntRange.fromString(rangeStr);
            } catch (Exception e) {
                throw new ConfigException("Invalid range configuration for enchant '" + key + "': " + rangeStr);
            }

            RegistryMatcher<Enchantment> matcher = RegistryMatcher.parse(registry, Set.of(key));
            if (matcher.any()) {
                global = range;
            } else {
                matcher.values().forEach(e -> targets.put(e, range));
            }

        }
        if (targets.isEmpty() && global == null) return null;

        return new ConfigEnchantmentMatcher(targets, global, matchAny);
    }

    public boolean matches(@NotNull Map<Enchantment, Integer> enchants) {
        if (globalRange != null) {
            boolean globalMet = false;
            for (int i : enchants.values()) {
                if (globalRange.isWithin(i)) {
                    globalMet = true;
                    break;
                }
            }

            if (matchAny && globalMet) return true;
            if (!matchAny && !globalMet) return false;
        }

        if (targetEnchants.isEmpty()) return !matchAny;

        if (matchAny) {
            for (var entry : targetEnchants.entrySet()) {
                int level = enchants.getOrDefault(entry.getKey(), 0);
                if (entry.getValue().isWithin(level)) return true;
            }
            return false;
        } else {
            for (var entry : targetEnchants.entrySet()) {
                int level = enchants.getOrDefault(entry.getKey(), 0);
                if (!entry.getValue().isWithin(level)) return false;
            }
            return true;
        }
    }
}