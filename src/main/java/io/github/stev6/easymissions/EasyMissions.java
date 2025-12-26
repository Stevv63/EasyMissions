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
package io.github.stev6.easymissions;

import io.github.stev6.easymissions.caches.BrewCache;
import io.github.stev6.easymissions.caches.antiexploit.RecentPlaceCache;
import io.github.stev6.easymissions.caches.antiexploit.RecentStepCache;
import io.github.stev6.easymissions.commands.MissionCommands;
import io.github.stev6.easymissions.config.ConfigManager;
import io.github.stev6.easymissions.config.records.MainConfig;
import io.github.stev6.easymissions.listeners.*;
import io.github.stev6.easymissions.listeners.protection.MissionBlockPlace;
import io.github.stev6.easymissions.listeners.protection.MissionInteract;
import io.github.stev6.easymissions.listeners.protection.MissionInventoryResult;
import io.github.stev6.easymissions.mission.missiontype.types.MissionType;
import io.github.stev6.easymissions.mission.missiontype.validator.Validators;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class EasyMissions extends JavaPlugin implements Listener {
    private static EasyMissions plugin;
    private final MissionTypeRegistry typeRegistry = new MissionTypeRegistry();
    private MissionManager missionManager;
    private boolean serverLoaded = false;
    private ConfigManager configManager;
    private RecentPlaceCache recentPlaceCache;
    private BrewCache brewCache;
    private PluginManager pluginManager;
    private RecentStepCache recentStepCache;
    private boolean debug = false;

    @SuppressWarnings("unused")
    public static EasyMissions getInstance() {
        if (plugin == null) throw new IllegalStateException("EasyMissions is null");
        return plugin;
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        pluginManager = getServer().getPluginManager();
        saveDefaultConfig();
        registerTypes();
        configManager = new ConfigManager(this);
        configManager.loadMain();
        initializeCaches(configManager.getMainConfig());
        missionManager = new MissionManager(this);
        registerTypeListeners();
        MissionCommands command = new MissionCommands(this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(command.buildMainCommand(), "EasyMissions command", List.of("em", "easym")));

        debug = plugin.getConfig().getBoolean("debug", false);
        pluginManager.registerEvents(this, this);
        EasyMissionsAPI.init(plugin);
        getLogger().info("EasyMissions " + plugin.getPluginMeta().getVersion() + " loaded");
    }

    @EventHandler
    public void onLoad(ServerLoadEvent e) {
        String types = typeRegistry.types().keySet().stream().sorted().collect(Collectors.joining(configManager.getMainConfig().mission().splitter()));
        getLogger().info("Available types: [%s]".formatted(types));
        configManager.loadMissions();
        serverLoaded = true;
    }

    @Override
    public void onDisable() {
        getLogger().info("EasyMissions disabled");
    }

    public void runCommand(String cmd, CommandSender sender) {
        if (isFolia()) {
            getServer().getGlobalRegionScheduler().execute(this, () -> {
                if (!getServer().dispatchCommand(sender, cmd))
                    getLogger().warning("Tried running invalid command " + cmd);
            });
        } else {
            if (!getServer().dispatchCommand(sender, cmd)) getLogger().warning("Failed running command " + cmd);
        }
    }

    private void initializeCaches(MainConfig config) {
        if (config.antiAbuse().recentPlacementCache())
            recentPlaceCache = new RecentPlaceCache(config.antiAbuse().recentPlacementCacheSize(), config.antiAbuse().recentPlacementCacheTimeout());
        recentStepCache = new RecentStepCache(config.antiAbuse().recentBlockStepCacheSize());
        brewCache = new BrewCache(config.mission().brewCacheTimeOut());
    }

    @ApiStatus.Internal
    public boolean isServerLoaded() {
        return serverLoaded;
    }

    @ApiStatus.Internal
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @ApiStatus.Internal
    public MissionTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    @ApiStatus.Internal
    public MissionManager getMissionManager() {
        return missionManager;
    }

    private void registerTypes() {
        Registry<@NotNull Enchantment> enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        typeRegistry.registerType(
                MissionType.matchEnum("break", Material.class),
                MissionType.matchEnum("place", Material.class),
                MissionType.matchEnum("fish", Material.class),
                MissionType.matchEnum("craft", Material.class),
                MissionType.matchEnum("harvest", Material.class),
                MissionType.matchEnum("disenchant", Material.class),
                MissionType.matchEnum("repair", Material.class),
                MissionType.matchEnum("consume", Material.class),
                MissionType.matchEnum("smelt", Material.class),
                MissionType.matchEnum("milk", EntityType.class),
                MissionType.matchEnum("breed", EntityType.class),
                MissionType.matchEnum("kill", EntityType.class),
                MissionType.matchEnum("damage", EntityType.class),
                MissionType.matchEnum("tame", EntityType.class),
                MissionType.matchEnum("shear", EntityType.class),
                MissionType.matchEnum("potion", PotionType.class),
                MissionType.matchEnum("brew", PotionType.class),
                MissionType.matchRegistry("enchant", enchantment),

                MissionType.complex(
                        "complex_enchant",
                        Validators.matchRegistry(enchantment),
                        Validators.matchInt(),
                        Validators.matchEnum(Material.class)
                ),

                MissionType.simple("walk"),
                MissionType.simple("glide"),
                MissionType.simple("swim"),
                MissionType.simple("xp"),
                MissionType.simple("trade")
        );
    }

    private void registerTypeListeners() {
        pluginManager.registerEvents(new BlocksListener(missionManager, recentPlaceCache), this);
        pluginManager.registerEvents(new ItemListener(missionManager), this);
        pluginManager.registerEvents(new EntityListener(missionManager), this);
        pluginManager.registerEvents(new GiveRewardsListener(missionManager), this);
        pluginManager.registerEvents(new MoveListener(recentStepCache, this), this);
        pluginManager.registerEvents(new BrewListener(missionManager, brewCache), this);
        pluginManager.registerEvents(new InventoryListener(missionManager), this);
        pluginManager.registerEvents(new MissionBlockPlace(missionManager), this);
        pluginManager.registerEvents(new MissionInventoryResult(missionManager), this);
        pluginManager.registerEvents(new ClaimListener(configManager), this);
        pluginManager.registerEvents(new MissionInteract(missionManager), this);
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean reloadConfigs() {
        saveDefaultConfig();
        reloadConfig();
        debug = getConfig().getBoolean("debug", false);
        boolean loaded = configManager.loadMain() && configManager.loadMissions();
        MainConfig mainConfig = configManager.getMainConfig();
        if (recentPlaceCache != null)
            recentPlaceCache.buildCache(mainConfig.antiAbuse().recentPlacementCacheSize(), mainConfig.antiAbuse().recentPlacementCacheTimeout());
        recentStepCache.buildCache(mainConfig.antiAbuse().recentBlockStepCacheSize());
        brewCache.buildCache(mainConfig.mission().brewCacheTimeOut());
        return loaded;
    }
}
