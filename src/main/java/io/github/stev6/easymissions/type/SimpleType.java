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

package io.github.stev6.easymissions.type;

import org.jetbrains.annotations.NotNull;

/**
 * A simple record for simple mission types, if you need to specify targets, take a look at {@link TargetedMissionType}
 * @param id The name you wish to give to the type, will be used in placeholders and the type registry
 */
public record SimpleType(@NotNull String id) implements MissionType {
}
