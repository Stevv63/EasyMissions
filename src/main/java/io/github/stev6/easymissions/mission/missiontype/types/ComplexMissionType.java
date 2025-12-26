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
import io.github.stev6.easymissions.mission.missiontype.validator.TargetValidator;
import io.github.stev6.easymissions.util.ComplexStringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

record ComplexMissionType(String id, List<TargetValidator> validators) implements TargetedMissionType {

    @Override
    public boolean isValidTarget(@NotNull String target) {
        String[] parts = ComplexStringUtil.REGEX.split(target.trim());

        if (parts.length != validators.size()) return false;

        for (int i = 0; i < validators.size(); i++) if (!validators.get(i).validate(parts[i])) return false;

        return true;
    }

    @Override
    public String normalize(@NotNull String target) {
        String[] parts = ComplexStringUtil.REGEX.split(target.trim());

        if (parts.length != validators.size()) return target;

        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < validators.size(); i++) {
            normalized.append(validators.get(i).normalize(parts[i]));
            if (i < validators.size() - 1) normalized.append(ComplexStringUtil.DELIMITER);
        }
        return normalized.toString();
    }
}
