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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.stev6.easymissions.exception.ConfigException.withContext;

@ApiStatus.Internal
public class ConfigManager {
    private final Map<String, MissionConfig> missions = new HashMap<>();
    private final EasyMissions plugin;
    private final File missionDir;
    private final Map<String, DefaultMission> defaultMissions = new HashMap<>();
    private MainConfig mainConfig;
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
            mainConfig = withContext("Loading main config (config.yml)", () -> MainConfig.parse(plugin.getConfig()));
            return true;
        } catch (ConfigException e) {
            e.handleException(plugin.getLogger(), plugin.isDebug(), plugin.getPluginMeta().getVersion());
            return false;
        }
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
        defaultMissions.clear();
        missionsLoaded = false;

        File defaultConfig = new File(missionDir, "default.yml");
        if (!defaultConfig.isFile()) plugin.saveResource("missions/default.yml", false);

        boolean defaultsLoad;
        try {
            defaultsLoad = withContext("Loading default missions", () -> loadDefaultMissions(defaultConfig));
        } catch (ConfigException e) {
            e.handleException(plugin.getLogger(), plugin.isDebug(), plugin.getPluginMeta().getVersion());
            return false;
        }

        boolean success = loadMissionFiles(missionDir, snapShot);
        missionsLoaded = true;
        return success && defaultsLoad;
    }

    private boolean loadDefaultMissions(File defaultMission) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(defaultMission);
        try {
            withContext("Base Default", () -> {
                ConfigurationSection sec = cfg.getConfigurationSection("default");
                if (sec == null) throw new ConfigException("File must contain a 'default' section.");
                defaultMissions.put("default", DefaultMission.parseBase(sec));
            });
        } catch (ConfigException e) {
            e.handleException(plugin.getLogger(), plugin.isDebug(), plugin.getPluginMeta().getVersion());
            return false;
        }

        boolean error = false;
        DefaultMission baseDefault = defaultMissions.get("default");

        for (String k : cfg.getKeys(false)) {
            if (k.equals("default")) continue;
            ConfigurationSection section = cfg.getConfigurationSection(k);
            if (section != null) {
                try {
                    withContext("Default mission: " + k,
                            () -> defaultMissions.put(k.toLowerCase(Locale.ROOT), DefaultMission.parse(section).inheritFrom(baseDefault)));
                } catch (ConfigException e) {
                    error = true;
                    e.handleException(plugin.getLogger(), plugin.isDebug(), plugin.getPluginMeta().getVersion());
                    plugin.getLogger().warning("Failed to parse default mission '" + k + "'. Using base default as fallback.");
                    defaultMissions.put(k.toLowerCase(Locale.ROOT), defaultMissions.get("default"));
                }
            }
        }
        return !error;
    }

    private boolean loadMissionFiles(File missionDir, Map<String, MissionConfig> snapShot) {
        boolean error = false;
        List<File> filesToLoad;

        try (Stream<Path> stream = Files.walk(missionDir.toPath())) {
            filesToLoad = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yml"))
                    .filter(p -> !p.getFileName().toString().equals("default.yml"))
                    .map(Path::toFile)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading mission directory: " + e.getMessage());
            return false;
        }

        if (filesToLoad.isEmpty()) {
            plugin.saveResource("missions/example.yml", false);
            filesToLoad.add(new File(missionDir, "example.yml"));
        }

        for (File file : filesToLoad) {
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(file);
                for (String missionEntry : cfg.getKeys(false)) {
                    ConfigurationSection missionSection = cfg.getConfigurationSection(missionEntry);
                    if (missionSection == null) continue;
                    String key = missionSection.getName().toLowerCase(Locale.ROOT);
                    try {
                        withContext("Mission: " + missionEntry, () -> parseMissionConfig(missionSection));
                    } catch (ConfigException e) {
                        e.addContext("In file: " + file.getName());
                        error = true;
                        e.handleException(plugin.getLogger(), plugin.isDebug(), plugin.getPluginMeta().getVersion());
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
                error = true;
            }
        }

        return !error;
    }

    private void parseMissionConfig(@NotNull ConfigurationSection missionSection) {
        var defaultMissionStr = missionSection.getString("default", "default").toLowerCase(Locale.ROOT);
        DefaultMission defaultMission = defaultMissions.get(defaultMissionStr);
        if (defaultMission == null)
            throw new ConfigException("Default mission: " + defaultMissionStr + " does not exist");

        MissionConfig mission = MissionConfig.parse(missionSection, defaultMission, defaultMissionStr, plugin);
        missions.put(mission.key(), mission);
    }
}

