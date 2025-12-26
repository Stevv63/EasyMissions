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

package io.github.stev6.easymissions.config.records;

import net.kyori.adventure.key.Key;

import java.util.Map;

public record MainConfig(Messages messages, Map<String, Integer> categories, Mission mission, AntiAbuse antiAbuse,
                         Menus menus) {

    public record Messages(
            String reload,
            String reloadFail,
            String needsPlayer,
            String needsMission,
            String giveMission,
            String fullInventory,
            String randMissionNotFound,
            String setSuccess) {
    }

    public record Mission(
            Key claimSound,
            float claimPitch,
            float claimVolume,
            String splitter,
            int updateWalk,
            long brewCacheTimeOut
    ) {
    }

    public record AntiAbuse(
            boolean recentPlacementCache,
            int recentPlacementCacheSize,
            long recentPlacementCacheTimeout,
            boolean recentBlockStepCache,
            int recentBlockStepCacheSize) {
    }

    public record Menus(
            String dataMenu
    ) {
    }
}
