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

import io.github.stev6.easymissions.context.impl.BlockContext;
import io.github.stev6.easymissions.matcher.impl.EnumMatcher;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.TargetedMissionType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Harvest implements TargetedMissionType<BlockContext, Harvest.HarvestData> {
    public static final Harvest INSTANCE = new Harvest();
    private static final String ID = "harvest";

    private Harvest() {
    }

    @Override
    public @NotNull String id() {
        return ID;
    }

    @Override
    public @NotNull HarvestData parse(@NotNull ConfigurationSection section) {
        var blockStrings = new HashSet<>(section.getStringList("materials"));
        return new HarvestData(EnumMatcher.parse(Material.class, blockStrings));
    }

    public record HarvestData(EnumMatcher<Material> blocks) implements MissionTarget<BlockContext> {
        @Override
        public boolean matches(BlockContext context) {
            return blocks.matches(context.block().getType());
        }
    }
}
