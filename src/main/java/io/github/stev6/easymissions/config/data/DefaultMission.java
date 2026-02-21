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

import io.github.stev6.easymissions.exception.ConfigException;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DefaultMission(
        @Nullable String name,
        @Nullable String completedName,
        @Nullable List<String> lore,
        @Nullable List<String> completedLore,
        @Nullable String requirementRange,
        @Nullable String category,
        @Nullable ItemRarity itemRarity,
        @Nullable Material itemMaterial,
        @Nullable NamespacedKey itemModel,
        @Nullable NamespacedKey completedItemModel,
        @Nullable ConfigurationSection optionsSection,
        @Nullable List<String> rewards,
        @Nullable List<String> blacklistedWorlds
) {

    public static DefaultMission parse(@NotNull ConfigurationSection section) {
        String name = section.getString("name");
        String completedName = section.getString("completed_name");
        String requirement = section.getString("requirement_range");
        String category = section.getString("category");

        String rarityStr = section.getString("item_rarity");
        ItemRarity itemRarity = rarityStr != null ? EnumUtils.getEnumIgnoreCase(ItemRarity.class, rarityStr) : null;

        String matStr = section.getString("item_material");
        Material itemMaterial = matStr != null ? Material.matchMaterial(matStr) : null;

        List<String> lore = getListOrNull(section, "lore");
        List<String> completedLore = getListOrNull(section, "completed_lore");
        List<String> rewards = getListOrNull(section, "rewards");
        List<String> worlds = getListOrNull(section, "blacklisted_worlds");

        String modelStr = section.getString("item_model");
        NamespacedKey itemModel = (modelStr != null && !modelStr.isBlank())
                ? NamespacedKey.fromString(modelStr) : null;

        String completedModelStr = section.getString("completed_item_model");
        NamespacedKey completedItemModel = (completedModelStr != null && !completedModelStr.isBlank())
                ? NamespacedKey.fromString(completedModelStr) : null;

        ConfigurationSection options = section.getConfigurationSection("custom_options");

        boolean hasAnyField = name != null || completedName != null || requirement != null || category != null ||
                itemRarity != null || itemMaterial != null || lore != null || completedLore != null ||
                rewards != null || worlds != null || itemModel != null || completedItemModel != null ||
                options != null;

        if (!hasAnyField) {
            throw new ConfigException("Default definition cannot be completely empty.");
        }

        return new DefaultMission(
                name, completedName, lore, completedLore, requirement, category,
                itemRarity, itemMaterial, itemModel, completedItemModel, options, rewards, worlds
        );
    }

    public static DefaultMission parseBase(@NotNull ConfigurationSection section) {
        String name = section.getString("name");
        if (name == null) throw new ConfigException("Base default missing required field: 'name'");

        String completedName = section.getString("completed_name");
        if (completedName == null) completedName = name;

        String requirement = section.getString("requirement_range");
        if (requirement == null) throw new ConfigException("Base default missing required field: 'requirement_range'");

        String category = section.getString("category");
        if (category == null) throw new ConfigException("Base default missing required field: 'category'");

        String matStr = section.getString("item_material");
        if (matStr == null) throw new ConfigException("Base default missing required field: 'item_material'");

        Material itemMaterial = Material.matchMaterial(matStr);
        if (itemMaterial == null) throw new ConfigException("Invalid item_material in base default: " + matStr);

        String rarityStr = section.getString("item_rarity", "COMMON");
        ItemRarity itemRarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, rarityStr);
        if (itemRarity == null) throw new ConfigException("Invalid item_rarity in base default: " + rarityStr);

        List<String> lore = section.getStringList("lore");
        List<String> completedLore = section.getStringList("completed_lore");
        List<String> rewards = section.getStringList("rewards");
        List<String> worlds = section.getStringList("blacklisted_worlds");

        String modelStr = section.getString("item_model");
        NamespacedKey itemModel = (modelStr != null && !modelStr.isBlank())
                ? NamespacedKey.fromString(modelStr) : null;

        String completedModelStr = section.getString("completed_item_model");
        NamespacedKey completedItemModel = (completedModelStr != null && !completedModelStr.isBlank())
                ? NamespacedKey.fromString(completedModelStr) : null;

        ConfigurationSection options = section.getConfigurationSection("custom_options");

        return new DefaultMission(
                name, completedName, lore, completedLore, requirement, category,
                itemRarity, itemMaterial, itemModel, completedItemModel, options, rewards, worlds
        );
    }

    private static List<String> getListOrNull(ConfigurationSection section, String key) {
        return section.contains(key) ? section.getStringList(key) : null;
    }

    public DefaultMission inheritFrom(DefaultMission parent) {
        return new DefaultMission(
                name != null ? name : parent.name,
                completedName != null ? completedName : parent.completedName,
                lore != null ? lore : parent.lore,
                completedLore != null ? completedLore : parent.completedLore,
                requirementRange != null ? requirementRange : parent.requirementRange,
                category != null ? category : parent.category,
                itemRarity != null ? itemRarity : parent.itemRarity,
                itemMaterial != null ? itemMaterial : parent.itemMaterial,
                itemModel != null ? itemModel : parent.itemModel,
                completedItemModel != null ? completedItemModel : parent.completedItemModel,
                optionsSection != null ? optionsSection : parent.optionsSection,
                rewards != null ? rewards : parent.rewards,
                blacklistedWorlds != null ? blacklistedWorlds : parent.blacklistedWorlds
        );
    }
}