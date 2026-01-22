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

import io.github.stev6.easymissions.context.impl.SmeltContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import io.github.stev6.easymissions.matcher.ValueMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Smelt implements TargetedMissionType<SmeltContext, Smelt.SmeltData> {
    public static final Smelt INSTANCE = new Smelt();
    private static final String ID = "smelt";

    private Smelt() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull SmeltData parse(@NotNull ConfigurationSection section) {
        var materialStrings = new HashSet<>(section.getStringList("materials"));
        return new SmeltData(EnumMatcher.parse(Material.class, materialStrings));
    }

    public record SmeltData(@NotNull ValueMatcher<Material> material) implements MissionTarget<SmeltContext> {
        @Override
        public boolean matches(SmeltContext context) {
            return material.matches(context.type());
        }
    }
}