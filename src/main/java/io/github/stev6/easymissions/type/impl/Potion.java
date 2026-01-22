/*
 * EasyMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
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

package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.context.impl.ItemContext;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.matcher.impl.RegistryMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.util.ListenerUtils;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Potion implements TargetedMissionType<ItemContext, Potion.PotionData> {
    public static final Potion INSTANCE = new Potion();
    private static final String ID = "potion";

    private Potion() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull PotionData parse(@NotNull ConfigurationSection section) {
        var itemSection = section.getConfigurationSection("item");
        ItemDataMatcher itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);

        HashSet<String> effectStrings = new HashSet<>(section.getStringList("effects"));
        RegistryMatcher<PotionEffectType> effectMatcher = RegistryMatcher.parse(Registry.POTION_EFFECT_TYPE, effectStrings);
        return new PotionData(itemMatcher, effectMatcher);
    }

    public record PotionData(@Nullable ItemDataMatcher itemMatcher,
                             @NotNull RegistryMatcher<PotionEffectType> effectMatcher) implements MissionTarget<ItemContext> {
        @Override
        public boolean matches(ItemContext context) {
            if (itemMatcher != null && !itemMatcher.matches(context.item())) return false;
            if (effectMatcher.any()) return true;
            var effects = ListenerUtils.getAllPotionEffects(context.item());
            if (effects != null) {
                for (var effect : effects) if (effectMatcher.matches(effect)) return true;
            }
            return false;
        }
    }
}