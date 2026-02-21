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

package io.github.stev6.easymissions.config.data;

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.option.MissionOption;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.util.IntRange;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.stev6.easymissions.exception.ConfigException.withContext;

public record MissionConfig(
        @NotNull String key,
        @NotNull String name,
        @NotNull String completedName,
        @NotNull List<String> lore,
        @NotNull List<String> completedLore,
        @NotNull String category,
        @NotNull MissionType type,
        @NotNull String taskDescription,
        @Nullable MissionTarget<?> data,
        @NotNull List<MissionOption> options,
        @NotNull ItemRarity itemRarity,
        @NotNull IntRange requirementRange,
        @Nullable NamespacedKey itemModel,
        @Nullable NamespacedKey completedItemModel,
        @NotNull Material itemMaterial,
        @NotNull List<String> rewards,
        @NotNull Set<UUID> blacklistedWorlds
) {

    public static MissionConfig parse(@NotNull ConfigurationSection section,
                                      @NotNull DefaultMission def,
                                      @NotNull String defName,
                                      @NotNull EasyMissions plugin) {

        String key = section.getName().toLowerCase(Locale.ROOT);

        String name = resolve(section.getString("name"), def.name(), "name", defName);

        String rawCompleted = resolve(section.getString("completed_name"), def.completedName(), "completed_name", defName);
        String completedName = rawCompleted.replace("[NAME]", name);

        String category = resolve(section.getString("category"), def.category(), "category", defName);
        String task = section.getString("task", "");

        List<String> lore = new ArrayList<>(resolve(
                getListOrNull(section, "lore"), def.lore(), "lore", defName
        ));

        List<String> completedLore = new ArrayList<>(resolve(
                getListOrNull(section, "completed_lore"), def.completedLore(), "completed_lore", defName
        ));

        int loreIdx = completedLore.indexOf("[LORE!]");
        if (loreIdx != -1) {
            var strLore = lore.stream().map(s -> "<st>" + s + "</st>").toList();
            completedLore.remove(loreIdx);
            completedLore.addAll(loreIdx, strLore);
        }

        List<String> rewards = new ArrayList<>(resolve(
                getListOrNull(section, "rewards"), def.rewards(), "rewards", defName
        ));

        int rewIdx = rewards.indexOf("[REWARDS!]");
        if (rewIdx != -1) {
            List<String> rewardsList = def.rewards() == null ? Collections.emptyList() : def.rewards();
            if (rewardsList.isEmpty())
                plugin.getLogger().warning("[REWARDS!] is used but default '" + defName + "' has no rewards.");

            rewards.remove(rewIdx);
            rewards.addAll(rewIdx, rewardsList);
        }

        String typeId = section.getString("type");
        if (typeId == null) throw new ConfigException("Missing field: type");

        MissionType type = plugin.getTypeRegistry().get(typeId.toLowerCase(Locale.ROOT));
        if (type == null) throw new ConfigException("Unknown type id: " + typeId);

        ConfigurationSection targetSec = section.getConfigurationSection("targets");
        if (targetSec == null) targetSec = section;

        MissionTarget<?> data = null;
        if (type instanceof TargetedMissionType<?, ?> targetedType) {
            ConfigurationSection finalTargetSec = targetSec;
            data = withContext("while parsing targets", () -> targetedType.parse(finalTargetSec));
        }

        ConfigurationSection optSec = section.getConfigurationSection("custom_options");
        if (optSec == null) optSec = def.optionsSection();

        List<MissionOption> options = new ArrayList<>();
        if (optSec != null) {
            for (String k : optSec.getKeys(false)) {
                ConfigurationSection finalOptSec = optSec;
                var opt = withContext("Option: " + k, () ->
                        plugin.getOptionRegistry().parse(k, finalOptSec.getConfigurationSection(k))
                );
                if (opt != null) options.add(opt);
                else plugin.getLogger().warning("Unknown option '" + k + "' in " + key);
            }
        }

        String rarityStr = section.getString("item_rarity");
        ItemRarity rarity;
        if (rarityStr != null) {
            rarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, rarityStr);
            if (rarity == null) throw new ConfigException("Invalid item_rarity: '" + rarityStr + "'");
        } else {
            rarity = resolve(null, def.itemRarity(), "item_rarity", defName);
        }

        String matStr = section.getString("item_material");
        Material material;
        if (matStr != null) {
            material = Material.matchMaterial(matStr);
            if (material == null) throw new ConfigException("Invalid item_material: '" + matStr + "'");
        } else {
            material = resolve(null, def.itemMaterial(), "item_material", defName);
        }

        String modelStr = section.getString("item_model");
        NamespacedKey itemModel = (modelStr != null && !modelStr.isBlank())
                ? NamespacedKey.fromString(modelStr)
                : def.itemModel();

        String compModelStr = section.getString("completed_item_model");
        NamespacedKey completedItemModel;
        if (compModelStr != null && !compModelStr.isBlank()) {
            completedItemModel = NamespacedKey.fromString(compModelStr);
        } else {
            completedItemModel = (def.completedItemModel() != null) ? def.completedItemModel() : itemModel;
        }

        String rangeStr = resolve(section.getString("requirement_range"), def.requirementRange(), "requirement_range", defName);

        IntRange range = withContext("Field: requirement_range", () -> IntRange.fromString(rangeStr));

        List<String> worldStrs = resolve(
                getListOrNull(section, "blacklisted_worlds"),
                def.blacklistedWorlds(),
                Collections.emptyList()
        );

        Set<UUID> blacklisted = worldStrs.stream()
                .map(s -> {
                    var w = plugin.getServer().getWorld(s);
                    if (w == null) plugin.getLogger().warning("Invalid world '" + s + "' in mission '" + key + "'");
                    return w;
                })
                .filter(Objects::nonNull)
                .map(WorldInfo::getUID)
                .collect(Collectors.toSet());

        return new MissionConfig(
                key, name, completedName, List.copyOf(lore), List.copyOf(completedLore),
                category, type, task, data, List.copyOf(options), rarity, range,
                itemModel, completedItemModel, material, List.copyOf(rewards), Set.copyOf(blacklisted)
        );
    }


    private static List<String> getListOrNull(ConfigurationSection s, String k) {
        return s.contains(k) ? s.getStringList(k) : null;
    }

    private static <T> T resolve(@Nullable T specific, @Nullable T def, @NotNull String field, @NotNull String defName) {
        if (specific != null) return specific;
        if (def != null) return def;
        throw new ConfigException("Field: '" + field + "' is missing in both mission and default '" + defName + "'");
    }

    private static <T> T resolve(@Nullable T specific, @Nullable T def, @NotNull T fallback) {
        if (specific != null) return specific;
        if (def != null) return def;
        return fallback;
    }
}