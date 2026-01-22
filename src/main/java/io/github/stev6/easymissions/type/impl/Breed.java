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

import java.util.HashSet;

public class Breed implements TargetedMissionType<EntityContext, Breed.BreedData> {
    public static final Breed INSTANCE = new Breed();
    private static final String ID = "breed";

    private Breed() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull BreedData parse(@NotNull ConfigurationSection section) {
        ConfigurationSection entitySection = section.getConfigurationSection("entity");
        var entityMatcher = entitySection == null ? null : EntityDataMatcher.parse(entitySection);
        return new BreedData(entityMatcher);
    }

    public record BreedData(@Nullable EntityDataMatcher entityMatcher) implements MissionTarget<EntityContext> {
        @Override
        public boolean matches(@NotNull EntityContext context) {
            return entityMatcher == null || entityMatcher.matches(context.entity());
        }
    }
}