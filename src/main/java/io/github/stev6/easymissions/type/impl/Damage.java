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

import io.github.stev6.easymissions.context.impl.EntityDamageContext;
import io.github.stev6.easymissions.matcher.impl.EntityDataMatcher;
import io.github.stev6.easymissions.matcher.impl.ItemDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Damage implements TargetedMissionType<EntityDamageContext, Damage.DamageData> {
    public static final Damage INSTANCE = new Damage();
    private static final String ID = "damage";

    private Damage() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull DamageData parse(@NotNull ConfigurationSection section) {
        ConfigurationSection entitySection = section.getConfigurationSection("entity");
        var entityMatcher = entitySection == null ? null : EntityDataMatcher.parse(entitySection);
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        var itemMatcher = itemSection == null ? null : ItemDataMatcher.parse(itemSection);
        return new DamageData(entityMatcher, itemMatcher);
    }

    public record DamageData(@Nullable EntityDataMatcher entityMatcher,
                             @Nullable ItemDataMatcher itemMatcher) implements MissionTarget<EntityDamageContext> {
        @Override
        public boolean matches(EntityDamageContext context) {
            return (entityMatcher == null || entityMatcher.matches(context.entity())) && (itemMatcher == null || itemMatcher.matches(context.weapon()));
        }
    }
}