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

package io.github.stev6.easymissions.mission.missiontype.types;

import io.github.stev6.easymissions.mission.missiontype.TargetedMissionType;
import io.github.stev6.easymissions.mission.missiontype.validator.Validators;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

record KeyedMissionType<T extends Keyed>(String id,
                                         Registry<@NotNull T> registry) implements TargetedMissionType {

    @Override
    public boolean isValidTarget(@NotNull String target) {
        return Validators.matchRegistry(registry).validate(target);
    }

    @Override
    public @NotNull String normalize(@NotNull String target) {
        return Validators.matchRegistry(registry).normalize(target);
    }
}
