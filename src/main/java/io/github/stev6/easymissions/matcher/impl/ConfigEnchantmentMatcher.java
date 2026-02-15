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

/**
 * {@link ValueMatcher} for a map of Enchantments and levels against configuration requirements.
 * <p>
 * This matcher supports:
 * <ul>
 *     <li>Specific levels for specific enchantments (e.g., sharpness: 5)</li>
 *     <li>Level ranges (e.g., unbreaking: "1-3")</li>
 *     <li>"Match Any" logic (require all listed enchants vs require at least one)</li>
 *     <li>Wildcard matches via {@link RegistryMatcher}</li>
 * </ul>
 *
 * <h3>Config Example</h3>
 * <pre>
 * enchants:
 *   sharpness: "5"      # Must be exactly level 5
 *   unbreaking: "3+"    # Must be level 3 or higher
 *   mending: "1"
 *   protection: # anything
 *   match_any_enchant: false # If false, item must have ALL defined enchants
 * </pre>
 */
public record ConfigEnchantmentMatcher(Map<Enchantment, IntRange> targetEnchants, @Nullable IntRange globalRange,
                                       boolean matchAny) implements ValueMatcher<Map<Enchantment, Integer>> {

    /**
     * Parses a configuration section into an enchantment matcher.
     *
     * @param section The 'enchants' configuration section.
     * @param matchAny Whether to require <b>all</b> listed enchants (false) or <b>at least one</b> (true).
     *                 This is usually passed from the parent section (e.g. {@code match_any_enchant: true}).
     * @return A matcher instance, or null if the section is empty/invalid.
     */
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