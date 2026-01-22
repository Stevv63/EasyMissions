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

import io.github.stev6.easymissions.context.impl.EntityContext;
import io.github.stev6.easymissions.matcher.impl.EntityDataMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Milk implements TargetedMissionType<EntityContext, Milk.MilkData> {
    public static final Milk INSTANCE = new Milk();
    private static final String ID = "milk";

    private Milk() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull MilkData parse(@NotNull ConfigurationSection section) {
        ConfigurationSection entitySection = section.getConfigurationSection("entity");
        var entityMatcher = entitySection == null ? null : EntityDataMatcher.parse(entitySection);
        return new MilkData(entityMatcher);
    }

    public record MilkData(@Nullable EntityDataMatcher entityMatcher) implements MissionTarget<EntityContext> {
        @Override
        public boolean matches(@NotNull EntityContext context) {
            return entityMatcher == null || entityMatcher.matches(context.entity());
        }
    }
}