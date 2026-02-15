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

import io.github.stev6.easymissions.EasyMissionsAPI;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for a mission type, mission types can be registered using the API method {@link EasyMissionsAPI#registerType(MissionType...)}
 * @see TargetedMissionType
 * @see EasyMissionsAPI
 */
@FunctionalInterface
public interface MissionType {

    /**
     * Convenience method to create a simple, targetless mission type.
     * @param id The name of the type
     * @return a mission type
     */
    static @NotNull MissionType simple(@NotNull String id) {
        return new SimpleType(id);
    }

    /**
     * String identifier for the type, used for registering in the registry and displaying placeholders
     * @return The name of the type
     */
    @NotNull String id();
}
