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

package io.github.stev6.easymissions.config;

import io.github.stev6.easymissions.EasyMissions;
import io.github.stev6.easymissions.config.records.DefaultMission;
import io.github.stev6.easymissions.config.records.MainConfig;
import io.github.stev6.easymissions.config.records.MissionConfig;
import io.github.stev6.easymissions.exceptions.ConfigException;
import io.github.stev6.easymissions.mission.missiontype.TargetedMissionType;
import io.github.stev6.easymissions.mission.missiontype.types.MissionType;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class ConfigManager {
    private final Map<String, MissionConfig> missions = new HashMap<>();
    private final EasyMissions plugin;
    private final File missionDir;
    private MainConfig mainConfig;
    private DefaultMission defaultMission;
    private boolean missionsLoaded = false;

    public ConfigManager(EasyMissions plugin) {
        this.plugin = plugin;
        this.missionDir = new File(plugin.getDataFolder(), "missions");
    }

    public Map<String, MissionConfig> getMissions() {
        return Collections.unmodifiableMap(missions);
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public boolean loadMain() {
        try {
            loadMainConfig();
        } catch (ConfigException e) {
            handleException("While loading main config", e);
            return false;
        }
        return true;
    }

    public boolean loadMissions() {
        try {
            Files.createDirectories(missionDir.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe(e.toString());
            return false;
        }

        try {
            loadMissions(missionDir);
        } catch (ConfigException e) {
            handleException("While loading mission configs", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("PatternValidation")
    private void loadMainConfig() {

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) throw new ConfigException("Couldn't find messages section");

        MainConfig.Messages messages =
                new MainConfig.Messages(
                        messagesSection.getString("reload", "<green>Reloaded successfully</green>"),
                        messagesSection.getString("reload_fail", "An error occurred while reloading, please check the console."),
                        messagesSection.getString("needs_player", "<red>Only players can use this command.</red>"),
                        messagesSection.getString("needs_mission", "<red>You must be holding a mission to use this command</red>"),
                        messagesSection.getString("give_mission", "<green>Successfully gave <mission> to <target></green>"),
                        messagesSection.getString("full_inventory", "<red>Target inventory is full.</red>"),
                        messagesSection.getString("rand_mission_not_found", "<red>Couldn't find any mission entry in the category, make sure all configs are assigned"),
                        messagesSection.getString("set_success", "<green>Success</green>"));

        ConfigurationSection missionSection = plugin.getConfig().getConfigurationSection("mission");
        if (missionSection == null) throw new ConfigException("Couldn't find mission section");

        String claimSound = missionSection.getString("claim_sound");

        MainConfig.Mission mission = new MainConfig.Mission(
                claimSound != null ? Key.key(claimSound) : null,
                (float) missionSection.getDouble("claim_sound_pitch", 1),
                (float) missionSection.getDouble("claim_sound_volume", 1),
                missionSection.getString("target_splitter", ", "),
                missionSection.getInt("update_walk", 5),
                missionSection.getLong("brew_cache_timeout", 300));

        ConfigurationSection antiAbuseSection = plugin.getConfig().getConfigurationSection("anti_abuse");

        if (antiAbuseSection == null) throw new ConfigException("Couldn't find anti abuse section");

        MainConfig.AntiAbuse antiAbuse = new MainConfig.AntiAbuse(
                antiAbuseSection.getBoolean("recent_placement_cache", true),
                antiAbuseSection.getInt("recent_placement_cache_size", 120),
                antiAbuseSection.getLong("recent_placement_cache_timeout", 60),
                antiAbuseSection.getBoolean("recent_block_step_cache", true),
                antiAbuseSection.getInt("recent_block_step_cache_size", 5));

        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");

        if (categoriesSection == null) throw new ConfigException("Couldn't find categories section");

        Map<String, Integer> categories = categoriesSection.getKeys(false).stream().collect(Collectors.toMap(key -> key, categoriesSection::getInt));

        if (categories.isEmpty()) throw new ConfigException("There cannot be 0 categories");

        ConfigurationSection menusSection = plugin.getConfig().getConfigurationSection("menus");

        if (menusSection == null) throw new ConfigException("Couldn't find menus section");

        MainConfig.Menus menus = new MainConfig.Menus(getConfigString(menusSection, "data_menu"));

        mainConfig = new MainConfig(messages, categories, mission, antiAbuse, menus);
    }

    private void loadDefaultMission(File defaultMission) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(defaultMission);
        ConfigurationSection defaultSection = cfg.getConfigurationSection("default");
        if (defaultSection == null) throw new ConfigException("No \"default\" section in default mission file");
        parseDefaultMissionConfig(defaultSection);
    }

    public boolean isMissionsLoaded() {
        return missionsLoaded;
    }

    private void loadMissions(File missionDir) {
        missions.clear();

        File[] files = missionDir.listFiles(f -> f.isFile() && f.getName().endsWith(".yml") && !f.getName().equals("default.yml"));
        if (files == null) return;

        File defaultConfig = new File(missionDir, "default.yml");
        if (!defaultConfig.isFile()) plugin.saveResource("missions/default.yml", false);
        loadDefaultMission(defaultConfig);


        if (files.length == 0) {
            plugin.saveResource("missions/example.yml", false);
            files = missionDir.listFiles(f -> f.isFile() && f.getName().endsWith(".yml") && !f.getName().equals("default.yml"));
        }

        for (File file : Objects.requireNonNull(files)) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            for (String missionEntry : cfg.getKeys(false)) {
                ConfigurationSection missionSection = cfg.getConfigurationSection(missionEntry);
                if (missionSection == null) continue;
                parseMissionConfig(missionSection);
            }
        }
        missionsLoaded = true;
    }

    private void parseDefaultMissionConfig(@NotNull ConfigurationSection missionSection) {
        String name = getConfigString(missionSection, "name");
        String completedName = getConfigString(missionSection, "completed_name");
        List<String> lore = missionSection.getStringList("lore");
        List<String> completedLore = missionSection.getStringList("completed_lore");
        String category = getConfigString(missionSection, "category");
        ItemRarity rarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, getConfigString(missionSection, "item_rarity"));
        if (rarity == null) throw new ConfigException("No item rarity set in default mission");

        Material material = Material.matchMaterial(getConfigString(missionSection, "item_material"));
        if (material == null) throw new ConfigException("No material set in default mission");
        defaultMission = new DefaultMission(
                name,
                completedName,
                lore,
                completedLore,
                category,
                rarity,
                material
        );
    }

    @SuppressWarnings("PatternValidation")
    private void parseMissionConfig(@NotNull ConfigurationSection missionSection) {
        if (defaultMission == null)
            throw new ConfigException("Default mission must exist before loading normal ones.");

        String name = missionSection.getString("name", defaultMission.name());
        String completedName = missionSection.getString("completed_name", defaultMission.completedName()).replace("[NAME]", name);

        List<String> lore = missionSection.contains("lore", true) ? missionSection.getStringList("lore") : defaultMission.lore();
        List<String> completedLore = missionSection.contains("completed_lore", true) ? missionSection.getStringList("completed_lore") : defaultMission.completedLore();

        if (!completedLore.isEmpty() && completedLore.getFirst().trim().equals("[LORE!]")) {
            completedLore = lore.stream().map(s -> "<st>" + s + "</st>").toList();
        }

        String category = missionSection.getString("category", defaultMission.category());
        MissionType type = plugin.getTypeRegistry().get(getConfigString(missionSection, "type").toLowerCase(Locale.ROOT));
        if (type == null) throw new ConfigException("Invalid type at " + missionSection);
        ItemRarity rarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, missionSection.getString("item_rarity"), defaultMission.itemRarity());
        int reqMin = missionSection.getInt("requirement_min", 1);
        if (reqMin <= 0) reqMin = 1;
        int reqMax = missionSection.getInt("requirement_max", reqMin);
        Set<String> targets = new HashSet<>(missionSection.getStringList("targets").stream().map(String::toLowerCase).toList());
        Set<UUID> blacklistedWorlds = missionSection.getStringList("blacklisted_worlds").stream()
                .map(s -> {
                    World world = plugin.getServer().getWorld(s);
                    if (world == null)
                        plugin.getLogger().warning("Invalid world name/World is unloaded? at " + missionSection.getName() + " " + s);
                    return world;
                })
                .filter(Objects::nonNull)
                .map(WorldInfo::getUID).collect(Collectors.toSet());

        String modelStr = missionSection.getString("item_model");
        String completedModelStr = missionSection.getString("completed_item_model");

        Optional<Key> itemModel = (modelStr == null || modelStr.isBlank())
                ? Optional.empty()
                : Optional.of(Key.key(modelStr));

        Optional<Key> completedItemModel = (completedModelStr == null || completedModelStr.isBlank())
                ? itemModel
                : Optional.of(Key.key(completedModelStr));

        String matString = missionSection.getString("item_material");
        Material material = matString != null ? Material.matchMaterial(matString) : defaultMission.itemMaterial();
        List<String> rewards = missionSection.getStringList("rewards");

        if (type instanceof TargetedMissionType targetedType) {
            targets = targets.stream().map(targetedType::normalize).collect(Collectors.toSet());
            for (String s : targets)
                if (!targetedType.validate(s))
                    throw new ConfigException("Mission " + missionSection + " has invalid target: " + s);
        }

        if (!mainConfig.categories().containsKey(category))
            throw new ConfigException("Invalid category " + category + " at " + missionSection);

        if (reqMax < reqMin)
            throw new ConfigException("Mission at " + missionSection + " has incorrect req min/max configuration");


        MissionConfig mission = new MissionConfig(
                missionSection.getName().toLowerCase(Locale.ROOT),
                name,
                completedName,
                lore,
                completedLore,
                category,
                type,
                rarity,
                reqMin,
                reqMax,
                targets,
                itemModel,
                completedItemModel,
                material,
                rewards,
                blacklistedWorlds
        );

        missions.put(mission.key(), mission);
    }

    private String getConfigString(ConfigurationSection section, String path) {
        String s = section.getString(path);
        if (s == null) throw new ConfigException("Missing String at " + section.getName() + "." + path);
        return s;
    }

    private void handleException(String when, ConfigException e) {
        Logger logger = plugin.getLogger();
        logger.severe("=======================================");
        logger.severe("         EasyMissions CONFIG ERROR      ");
        logger.severe("Plugin version: " + plugin.getPluginMeta().getVersion());
        logger.severe(when);
        logger.severe("Please check your missions.yml or configuration folder for errors.");
        logger.severe("---------------------------------------");
        logger.severe("Error message: " + e.getMessage());
        logger.severe("=======================================");
        if (plugin.isDebug()) logger.log(Level.SEVERE, "Stack trace:", e);
    }
}

