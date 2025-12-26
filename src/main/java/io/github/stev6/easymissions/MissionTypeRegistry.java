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

package io.github.stev6.easymissions;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import io.github.stev6.easymissions.mission.missiontype.types.MissionType;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class  MissionTypeRegistry {
    private final Map<String, MissionType> types = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public void registerType(MissionType... missionTypes) {
        for (MissionType type : missionTypes) {
            String key = type.id().toLowerCase(Locale.ROOT);
            Preconditions.checkArgument(!types.containsKey(key), "ID %s already exists".formatted(key));
            types.put(key, type);
        }
    }

    @ApiStatus.Internal
    @Nullable
    public MissionType get(String id) {
        return types.get(id);
    }

    @ApiStatus.Internal
    public Map<String, MissionType> types() {
        return Collections.unmodifiableMap(types);
    }
}
