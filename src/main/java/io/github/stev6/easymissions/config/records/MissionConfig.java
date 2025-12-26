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

import io.github.stev6.easymissions.mission.missiontype.types.MissionType;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record MissionConfig(
        String key,
        String name,
        String completedName,
        List<String> lore,
        List<String> completedLore,
        String category,
        MissionType type,
        ItemRarity itemRarity,
        int reqMin,
        int reqMax,
        Set<String> targets,
        Optional<Key> itemModel,
        Optional<Key> completedItemModel,
        Material itemMaterial,
        List<String> rewards,
        Set<UUID> blacklistedWorlds) {
}
