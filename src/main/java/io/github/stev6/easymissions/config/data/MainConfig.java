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
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.stev6.easymissions.exception.ConfigException.withContext;

public record MainConfig(Messages messages, Map<String, Integer> categories, Mission mission, AntiAbuse antiAbuse,
                         Menus menus) {

    public static MainConfig parse(FileConfiguration config) {
        Messages messages = withContext("Section: messages", () -> parseMessages(config.getConfigurationSection("messages")));
        Map<String, Integer> categories = withContext("Section: categories", () -> parseCategories(config.getConfigurationSection("categories")));
        Mission mission = withContext("Section: mission", () -> parseMission(config.getConfigurationSection("mission")));
        AntiAbuse antiAbuse = withContext("Section: anti_abuse", () -> parseAntiAbuse(config.getConfigurationSection("anti_abuse")));
        Menus menus = withContext("Section: menus", () -> parseMenus(config.getConfigurationSection("menus")));

        return new MainConfig(messages, categories, mission, antiAbuse, menus);
    }

    private static Messages parseMessages(ConfigurationSection section) {
        if (section == null) throw new ConfigException("Section is missing");
        return new Messages(
                section.getString("reload", "<green>Reloaded successfully</green>"),
                section.getString("reload_fail", "An error occurred while reloading..."),
                section.getString("needs_player", "<red>Only players can use this command.</red>"),
                section.getString("needs_mission", "<red>You must be holding a mission...</red>"),
                section.getString("give_mission", "<green>Successfully gave <mission> to <target></green>"),
                section.getString("rand_mission_not_found", "<red>Couldn't find any mission...</red>"),
                section.getString("set_success", "<green>Success</green>")
        );
    }

    private static Map<String, Integer> parseCategories(ConfigurationSection section) {
        if (section == null) throw new ConfigException("Section is missing");
        Map<String, Integer> cats = section.getKeys(false).stream().collect(Collectors.toMap(key -> key, section::getInt));
        if (cats.isEmpty()) throw new ConfigException("You must have at least one category");
        if (cats.values().stream().anyMatch(v -> v < 0))
            throw new ConfigException("Categories cannot have negative weight");
        return Map.copyOf(cats);
    }

    private static Mission parseMission(ConfigurationSection section) {
        if (section == null) throw new ConfigException("Section is missing");
        String claimSoundStr = section.getString("claim_sound");
        NamespacedKey claimSound = (claimSoundStr != null) ? NamespacedKey.fromString(claimSoundStr) : null;

        return new Mission(
                section.getBoolean("cache_slots", true),
                claimSound,
                (float) section.getDouble("claim_sound_pitch", 1),
                (float) section.getDouble("claim_sound_volume", 1),
                section.getInt("update_walk", 5),
                section.getLong("brew_cache_timeout", 300)
        );
    }

    private static AntiAbuse parseAntiAbuse(ConfigurationSection section) {
        if (section == null) throw new ConfigException("Section is missing");
        return new AntiAbuse(
                section.getBoolean("recent_placement_cache", true),
                section.getInt("recent_placement_cache_size", 120),
                section.getLong("recent_placement_cache_timeout", 60),
                section.getBoolean("recent_block_step_cache", true),
                section.getInt("recent_block_step_cache_size", 5)
        );
    }

    private static Menus parseMenus(ConfigurationSection section) {
        if (section == null) throw new ConfigException("Section is missing");
        String menu = section.getString("data_menu");
        if (menu == null) throw new ConfigException("Missing field: data_menu");
        return new Menus(menu);
    }


    public record Messages(String reload, String reloadFail, String needsPlayer, String needsMission,
                           String giveMission, String randMissionNotFound, String setSuccess) {
    }

    public record Mission(boolean cacheSlots, NamespacedKey claimSound, float claimPitch, float claimVolume,
                          int updateWalk, long brewCacheTimeOut) {
    }

    public record AntiAbuse(boolean recentPlacementCache, int recentPlacementCacheSize,
                            long recentPlacementCacheTimeout, boolean recentBlockStepCache,
                            int recentBlockStepCacheSize) {
    }

    public record Menus(String dataMenu) {
    }
}