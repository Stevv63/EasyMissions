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

package io.github.stev6.easymissions.type.impl;

import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.context.impl.BlockContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Place implements TargetedMissionType<BlockContext, Place.PlaceData> {

    public static final Place INSTANCE = new Place();

    private Place() {
    }

    @Override
    public @NotNull String id() {
        return "place";
    }

    @Override
    public @NotNull PlaceData parse(@NotNull ConfigurationSection section) {
        var matStrings = new HashSet<>(section.getStringList("materials"));
        return new PlaceData(EnumMatcher.parse(Material.class, matStrings));
    }

    public record PlaceData(@NotNull EnumMatcher<Material> materials) implements MissionTarget<BlockContext> {
        @Override
        public boolean matches(@NotNull BlockContext context) {
            return materials.matches(context.block().getType());
        }
    }
}
