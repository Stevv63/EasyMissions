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

package io.github.stev6.easymissions.registry;

import com.google.common.base.Preconditions;
import io.github.stev6.easymissions.option.MissionOption;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ApiStatus.Internal
public class MissionOptionRegistry {
    private final Map<String, Function<ConfigurationSection, MissionOption>> missionOptions = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public void register(String id, Function<ConfigurationSection, MissionOption> factory) {
        Preconditions.checkArgument(!missionOptions.containsKey(id), "ID %s already exists".formatted(id));
        missionOptions.put(id, factory);
    }

    @ApiStatus.Internal
    public MissionOption parse(String id, ConfigurationSection section) {
        var fun = missionOptions.get(id);
        return (fun != null) ? fun.apply(section) : null;
    }

    @ApiStatus.Internal
    public Map<String, Function<ConfigurationSection, MissionOption>> options() {
        return Collections.unmodifiableMap(missionOptions);
    }
}
