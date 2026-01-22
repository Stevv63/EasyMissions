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
import io.github.stev6.easymissions.config.data.DefaultMission;
import io.github.stev6.easymissions.config.data.MainConfig;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.exception.ConfigException;
import io.github.stev6.easymissions.option.MissionOption;
import io.github.stev6.easymissions.type.MissionTarget;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.github.stev6.easymissions.util.IntRange;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemRarity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
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
            withContext("Loading main config (config.yml)", this::loadMainConfig);
        } catch (ConfigException e) {
            handleException(e);
            return false;
        }
        return true;
    }

    public boolean isMissionsLoaded() {
        return missionsLoaded;
    }

    public boolean loadMissions() {
        try {
            Files.createDirectories(missionDir.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create missions directory: " + e.getMessage());
            return false;
        }
        Map<String, MissionConfig> snapShot = new HashMap<>(missions);
        missions.clear();
        missionsLoaded = false;

        File defaultConfig = new File(missionDir, "default.yml");
        if (!defaultConfig.isFile()) plugin.saveResource("missions/default.yml", false);


        try {
            withContext("Loading default mission", () -> loadDefaultMission(defaultConfig));
        } catch (ConfigException e) {
            handleException(e);
            return false;
        }

        boolean loaded = loadMissionFiles(missionDir, snapShot);
        missionsLoaded = loaded;
        return loaded;
    }

    private void loadMainConfig() {

        MainConfig.Messages messages = withContext("Section: messages", () -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages");
            if (section == null) throw new ConfigException("Section is missing");

            return new MainConfig.Messages(
                    section.getString("reload", "<green>Reloaded successfully</green>"),
                    section.getString("reload_fail", "An error occurred while reloading, check the console for more info, broken configs will be roll backed..."),
                    section.getString("needs_player", "<red>Only players can use this command.</red>"),
                    section.getString("needs_mission", "<red>You must be holding a mission...</red>"),
                    section.getString("give_mission", "<green>Successfully gave <mission> to <target></green>"),
                    section.getString("rand_mission_not_found", "<red>Couldn't find any mission...</red>"),
                    section.getString("set_success", "<green>Success</green>")
            );
        });

        MainConfig.Mission mission = withContext("Section: mission", () -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("mission");
            if (section == null) throw new ConfigException("Section is missing");

            String claimSoundStr = section.getString("claim_sound");
            @Nullable NamespacedKey claimSound = null;
            if (claimSoundStr != null) claimSound = NamespacedKey.fromString(claimSoundStr);

            return new MainConfig.Mission(
                    claimSound,
                    (float) section.getDouble("claim_sound_pitch", 1),
                    (float) section.getDouble("claim_sound_volume", 1),
                    section.getString("target_splitter", ", "),
                    section.getInt("update_walk", 5),
                    section.getLong("brew_cache_timeout", 300)
            );
        });

        MainConfig.AntiAbuse antiAbuse = withContext("Section: anti_abuse", () -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("anti_abuse");
            if (section == null) throw new ConfigException("Section is missing");

            return new MainConfig.AntiAbuse(
                    section.getBoolean("recent_placement_cache", true),
                    section.getInt("recent_placement_cache_size", 120),
                    section.getLong("recent_placement_cache_timeout", 60),
                    section.getBoolean("recent_block_step_cache", true),
                    section.getInt("recent_block_step_cache_size", 5)
            );
        });

        Map<String, Integer> categories = withContext("Section: categories", () -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("categories");
            if (section == null) throw new ConfigException("Section is missing");

            Map<String, Integer> cats = section.getKeys(false).stream().collect(Collectors.toMap(key -> key, section::getInt));

            if (cats.isEmpty()) throw new ConfigException("You must have at least one category");

            for (var entry : cats.entrySet())
                if (entry.getValue() < 0)
                    throw new ConfigException("Category '" + entry.getKey() + "' cannot have negative weight");

            return cats; // meow
        });

        MainConfig.Menus menus = withContext("Section: menus", () -> {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("menus");
            if (section == null) throw new ConfigException("Section is missing");

            return new MainConfig.Menus(getConfigString(section, "data_menu"));
        });

        mainConfig = new MainConfig(messages, categories, mission, antiAbuse, menus);
    }

    private void loadDefaultMission(File defaultMission) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(defaultMission);
        ConfigurationSection defaultSection = cfg.getConfigurationSection("default");
        if (defaultSection == null) throw new ConfigException("No \"default\" section in default mission file");
        parseDefaultMissionConfig(defaultSection);
    }

    private boolean loadMissionFiles(File missionDir, Map<String, MissionConfig> snapShot) {
        AtomicBoolean error = new AtomicBoolean(false);
        File[] files = missionDir.listFiles(f -> f.isFile() && f.getName().endsWith(".yml") && !f.getName().equals("default.yml"));
        if (files == null || files.length == 0) {
            plugin.saveResource("missions/example.yml", false);
            files = missionDir.listFiles(f -> f.isFile() && f.getName().endsWith(".yml") && !f.getName().equals("default.yml"));
        }

        for (File file : Objects.requireNonNull(files)) {
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(file); // do what YamlConfiguration does in loadConfiguration but allow error to be catched
                for (String missionEntry : cfg.getKeys(false)) {
                    ConfigurationSection missionSection = cfg.getConfigurationSection(missionEntry);
                    if (missionSection == null) continue;
                    String key = missionSection.getName().toLowerCase(Locale.ROOT);
                    try {
                        withContext("Mission: " + missionEntry, () -> parseMissionConfig(missionSection));
                    } catch (ConfigException e) {
                        e.addContext("In file: " + file.getName());
                        error.set(true);
                        handleException(e);
                        var snap = snapShot.get(key);
                        if (snap != null) {
                            plugin.getLogger().warning("Using old version of mission: " + key);
                            missions.put(key, snap);
                        }
                    }
                }
            } catch (Exception e) {
                final List<String> ERROR_HEADER = List.of(
                        "========================================================",
                        "              EasyMissions YAML READING ERROR",
                        "              Plugin version: " + plugin.getPluginMeta().getVersion(),
                        "",
                        "         THIS IS NOT AN EasyMissions ERROR!",
                        "         Please verify that your YAML file is correct.",
                        "========================================================"
                );
                ERROR_HEADER.forEach(plugin.getLogger()::severe);
                plugin.getLogger().severe("File: " + file.getName());
                plugin.getLogger().severe("Error: " + e.getMessage());

                plugin.getLogger().severe("============================================================");
                if (plugin.isDebug()) plugin.getLogger().log(Level.SEVERE, "Stack trace:", e);
                else plugin.getLogger().severe("Enable debug mode to see the stack trace.");
                error.set(true);
            }
        }
        return !error.get();
    }

    private void parseDefaultMissionConfig(@NotNull ConfigurationSection missionSection) {
        String name = getConfigString(missionSection, "name");
        String completedName = getConfigString(missionSection, "completed_name");
        List<String> lore = Collections.unmodifiableList(missionSection.getStringList("lore"));
        List<String> completedLore = Collections.unmodifiableList(missionSection.getStringList("completed_lore"));
        String requirement = getConfigString(missionSection, "requirement_range");
        String category = getConfigString(missionSection, "category");
        ItemRarity rarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, getConfigString(missionSection, "item_rarity"));
        if (rarity == null) throw new ConfigException("No item rarity set in default mission");

        Material material = Material.matchMaterial(getConfigString(missionSection, "item_material"));
        if (material == null) throw new ConfigException("No material set in default mission");

        List<String> worlds = Collections.unmodifiableList(missionSection.getStringList("blacklisted_worlds"));

        defaultMission = new DefaultMission(
                name,
                completedName,
                lore,
                completedLore,
                requirement,
                category,
                rarity,
                material,
                worlds
        );
    }

    private void parseMissionConfig(@NotNull ConfigurationSection missionSection) {
        if (defaultMission == null) throw new ConfigException("Default mission must exist before loading normal ones.");

        String name = missionSection.getString("name", defaultMission.name());
        String completedName = missionSection.getString("completed_name", defaultMission.completedName()).replace("[NAME]", name);

        List<String> lore = new ArrayList<>(missionSection.contains("lore", true) ? missionSection.getStringList("lore") : defaultMission.lore());
        List<String> completedLore = new ArrayList<>(missionSection.contains("completed_lore", true) ? missionSection.getStringList("completed_lore") : defaultMission.completedLore());

        int idx = completedLore.indexOf("[LORE!]");
        if (idx != -1) {
            var strLore = lore.stream().map(s -> "<st>" + s + "</st>").toList();
            completedLore.remove(idx);
            completedLore.addAll(idx, strLore);
        }

        String category = missionSection.getString("category", defaultMission.category());
        String typeId = getConfigString(missionSection, "type").toLowerCase(Locale.ROOT);
        MissionType type = plugin.getTypeRegistry().get(typeId);
        if (type == null)
            throw new ConfigException("Invalid type id " + typeId + " check the type list to find existing id's");

        ConfigurationSection dataSection = missionSection.getConfigurationSection("targets");
        if (dataSection == null) dataSection = missionSection;
        Optional<MissionTarget<?>> data;
        if (type instanceof TargetedMissionType<?, ?> targetedType) {
            ConfigurationSection finalDataSection = dataSection;
            data = Optional.of(withContext("while parsing targets", () -> targetedType.parse(finalDataSection)));
        } else data = Optional.empty();

        ConfigurationSection optionsSection = missionSection.getConfigurationSection("custom_options");
        List<MissionOption> options = new ArrayList<>();
        if (optionsSection != null) {
            for (String key : optionsSection.getKeys(false)) {
                try {
                    var opt = plugin.getOptionRegistry().parse(key, optionsSection.getConfigurationSection(key));
                    if (opt != null) options.add(opt);
                    else plugin.getLogger().warning("Invalid option '" + key + "' at " + missionSection.getName());
                } catch (Exception e) {
                    throw new ConfigException("while parsing option '" + key + "': " + e.getMessage(), e);
                }
            }
        }

        ItemRarity rarity = EnumUtils.getEnumIgnoreCase(ItemRarity.class, missionSection.getString("item_rarity"), defaultMission.itemRarity());

        String reqRangeString = missionSection.getString("requirement_range", defaultMission.requirementRange());
        IntRange range;
        try {
            range = IntRange.fromString(reqRangeString);
        } catch (Exception e) {
            throw new ConfigException("Invalid requirement_range '" + reqRangeString + "': " + e.getMessage());
        }

        List<String> worldStrings = missionSection.contains("blacklisted_worlds", true) ? missionSection.getStringList("blacklisted_worlds") : defaultMission.blacklistedWorlds();
        Set<UUID> blacklistedWorlds = worldStrings.stream()
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

        String task = missionSection.getString("task_description", "");

        Optional<NamespacedKey> itemModel = (modelStr == null || modelStr.isBlank())
                ? Optional.empty()
                : Optional.ofNullable(NamespacedKey.fromString(modelStr));

        Optional<NamespacedKey> completedItemModel = (completedModelStr == null || completedModelStr.isBlank())
                ? itemModel
                : Optional.ofNullable(NamespacedKey.fromString(completedModelStr));

        String matString = missionSection.getString("item_material");
        Material material = matString != null ? Material.matchMaterial(matString) : defaultMission.itemMaterial();
        List<String> rewards = missionSection.getStringList("rewards");

        if (!mainConfig.categories().containsKey(category))
            throw new ConfigException("Category '" + category + "' is not defined in config.yml categories list.");

        // make all collections immutable
        lore = Collections.unmodifiableList(lore);
        completedLore = Collections.unmodifiableList(completedLore);
        rewards = Collections.unmodifiableList(rewards);
        options = Collections.unmodifiableList(options);
        blacklistedWorlds = Collections.unmodifiableSet(blacklistedWorlds);


        MissionConfig mission = new MissionConfig(
                missionSection.getName().toLowerCase(Locale.ROOT),
                name,
                completedName,
                lore,
                completedLore,
                category,
                type,
                task,
                data,
                options,
                rarity,
                range,
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
        if (s == null) throw new ConfigException("Missing field: " + path);
        return s;
    }

    private void handleException(ConfigException e) {
        Logger logger = plugin.getLogger();

        final List<String> ERROR_HEADER = List.of(
                "=======================================",
                "         EasyMissions CONFIG ERROR     ",
                "         Plugin version: " + plugin.getPluginMeta().getVersion(),
                "======================================="
        );

        ERROR_HEADER.forEach(logger::severe);
        e.getFormattedMessage().lines().forEach(logger::severe);
        logger.severe("=======================================");
        if (plugin.isDebug()) logger.log(Level.SEVERE, "Stack trace:", e);
        else logger.severe("Enable debug mode to see the stack trace.");
    }

    private void withContext(String context, Runnable action) {
        try {
            action.run();
        } catch (ConfigException e) {
            throw e.addContext(context);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage(), e).addContext(context);
        }
    }

    private <T> T withContext(String context, Callable<T> action) {
        try {
            return action.call();
        } catch (ConfigException e) {
            throw e.addContext(context);
        } catch (Exception e) {
            throw new ConfigException(e.getMessage(), e).addContext(context);
        }
    }
}

