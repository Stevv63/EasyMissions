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

import com.google.common.base.Preconditions;
import io.github.stev6.easymissions.config.data.MainConfig;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.mission.Mission;
import io.github.stev6.easymissions.option.MissionOption;
import io.github.stev6.easymissions.registry.MissionTypeRegistry;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The API for the plugin, this contains all available API methods
 * <p>
 * <b>Some methods cannot be used before/after {@link ServerLoadEvent} is fired such as {@link #getMissionConfig(Mission)}
 * or else they will throw an {@link IllegalStateException}</b>
 *
 * @see #getInstance()
 */
@SuppressWarnings("unused")
public class EasyMissionsAPI {
    private static EasyMissionsAPI instance;
    private final MissionTypeRegistry registry;
    private final MissionManager manager;
    private final EasyMissions plugin;

    private EasyMissionsAPI(EasyMissions plugin) {
        this.plugin = plugin;
        this.registry = plugin.getTypeRegistry();
        this.manager = plugin.getMissionManager();

    }

    @ApiStatus.Internal
    static void init(EasyMissions plugin) {
        Preconditions.checkState(instance == null, "API already initialized");
        instance = new EasyMissionsAPI(plugin);
    }

    /**
     * Gets an instance of the {@link EasyMissionsAPI} to access API methods
     *
     * @return the {@link EasyMissionsAPI} instance
     */
    @NotNull
    public static EasyMissionsAPI getInstance() {
        Preconditions.checkNotNull(instance, "API not initialized");
        return instance;
    }

    /**
     * Registers a custom {@link MissionOption}. Mission options are additional checks that can be added to any mission,
     * such as limiting missions to players with specific permissions.
     * <p>
     * Registration of a {@link MissionOption} must happen <b>before</b> {@link ServerLoadEvent}, meaning during plugin
     * startup (e.g. in {@code onEnable}).
     *
     * <p><b>Example Usage:</b>
     *
     * <pre>{@code
     * // 1. Create your custom MissionOption
     * package io.github.stev6.easymissions.option.impl;
     *
     * import io.github.stev6.easymissions.context.MissionContext;
     * import io.github.stev6.easymissions.mission.Mission;
     * import io.github.stev6.easymissions.option.MissionOption;
     * import org.bukkit.configuration.ConfigurationSection;
     * import org.bukkit.entity.Player;
     * import org.bukkit.inventory.ItemStack;
     * import org.jetbrains.annotations.NotNull;
     *
     * import java.util.HashSet;
     * import java.util.Set;
     *
     * public class PermissionOption implements MissionOption {
     *
     *     private final Set<String> permissions;
     *
     *     // This constructor is required to match Function<ConfigurationSection, MissionOption>
     *     public PermissionOption(ConfigurationSection section) {
     *         this.permissions = new HashSet<>(section.getStringList("values"));
     *     }
     *
     *     @Override
     *     public boolean check(@NotNull Player player,
     *                          @NotNull Mission mission,
     *                          @NotNull ItemStack item,
     *                          @NotNull MissionContext context) {
     *
     *         // generally if you dont want the permission you wont specify it thus the mission option wouldn't be created, but to be lenient we allow it if it is empty anyway
     *         if (permissions.isEmpty()) return true;
     *
     *         // loop and return true if the player has at least one permission
     *         for (String permission : permissions) {
     *             if (player.hasPermission(permission)) {
     *                 return true;
     *             }
     *         }
     *         // if we're here, the player doesn't have any permissions but the list isn't empty so they're missing the required permissions, return false
     *         return false;
     *     }
     * }
     *
     * // 2. Register the option during plugin startup (recommended in onEnable)
     * @Override
     * public void onEnable() {
     *     EasyMissionsAPI.getInstance().registerOption("has_permission", PermissionOption::new);
     * }
     *
     * // 3. Use it in missions.yml
     * // my_mission:
     * //   type: "break"
     * //   targets:
     * //     materials: [ "STONE" ]
     * //   custom_options:
     * //     has_permission: # <= this is what you specify in the id parameter, without it it won't work
     * //       # The player only needs ONE of these permissions
     * //       values:
     * //         - "myplugin.missions.can_break_stone"
     * //         - "myplugin.missions.is_a_miner"
     * }</pre>
     *
     * @param id     The unique identifier for this option. This key is used inside the
     *               {@code custom_options} section of a mission configuration. It is best to keep it unique to avoid conflicts.
     * @param option A function that accepts a {@link ConfigurationSection} and returns a new
     *               {@link MissionOption} instance. This function is invoked when a mission
     *               configuration references this option.
     * @throws IllegalStateException    if called after the server has finished loading.
     * @throws IllegalArgumentException if an option with the same ID is already registered.
     */
    public void registerOption(@NotNull String id, @NotNull Function<ConfigurationSection, MissionOption> option) {
        checkMissionsLoaded();
        plugin.getOptionRegistry().register(id, option);
    }

    /**
     * Registers a {@link MissionType}
     * <p>
     * Registration of a {@link MissionType} must happen before {@link ServerLoadEvent} i.e, before the server loads
     *
     * @throws IllegalStateException    if called after the server loaded
     * @throws IllegalArgumentException if type ID already exists
     */
    public void registerType(@NotNull MissionType... type) {
        checkMissionsLoaded();
        registry.registerType(type);
    }

    /**
     * Updates the data on a mission item with the given {@link Mission}
     *
     * @param missionItem   The ItemStack that is a mission
     * @param mission       The updated {@link Mission} object to put on the Item
     * @param updateDisplay Whether to also update the mission's display (placeholders, lore) or not
     * @throws IllegalStateException    if called before the server loaded
     * @throws IllegalArgumentException if {@code updateDisplay} is true and the given {@link Mission} has no registered {@link MissionConfig}
     */
    public void updateMissionData(@NotNull ItemStack missionItem, @NotNull Mission mission, boolean updateDisplay) {
        checkMissionsLoaded();
        Preconditions.checkArgument(isMission(missionItem), "ItemStack passed isn't a mission item");
        if (updateDisplay)
            Preconditions.checkArgument(manager.getMissionConfigOrNull(mission) != null,
                    "Cannot update display for a broken mission");

        manager.updateMissionData(missionItem, mission, updateDisplay);
    }


    /**
     * Gets all available {@link MissionType}s in the {@link MissionTypeRegistry}, do note that the map is immutable and cannot be modified
     * <p>
     * Third party {@link MissionType}s may not be available yet, schedule your call to run on the next tick
     * or {@link ServerLoadEvent} or in a context where the server has loaded to ensure all types are loaded
     *
     * @return {@code Map<String, MissionType>} containing all {@link MissionType}s
     */
    @NotNull
    public Map<String, MissionType> getAllTypes() {
        return registry.types();
    }

    /**
     * Gets all available {@link MissionOption}s, do note that this map is immutable and cannot be modified
     * <p>
     * Third party {@link MissionOption}s may not be available yet, schedule your call to run on the next tick
     * or {@link ServerLoadEvent} or in a context where the server has loaded to ensure all options are loaded
     *
     * @return {@code Map<String, Function<ConfigurationSection, MissionOption>>} containing all {@link MissionOption}s
     */
    @NotNull
    public Map<String, Function<ConfigurationSection, MissionOption>> getAllOptions() {
        return plugin.getOptionRegistry().options();
    }

    /**
     * Gets a {@link MissionType} using its ID, do note that the collections within it are immutable and cannot be modified
     * <p>
     * Third party {@link MissionType}s may not be available yet, schedule your call to run on the next tick
     * or {@link ServerLoadEvent} or in a context where the server has loaded to ensure all types are loaded
     *
     * @param id the type's ID
     * @return {@code Optional<MissionType>} containing/not containing the {@link MissionType}
     */
    @NotNull
    public Optional<MissionType> getType(@NotNull String id) {
        return Optional.ofNullable(registry.get(id));
    }

    /**
     * Tries getting a {@link MissionConfig} from a {@link Mission}
     *
     * @param mission a {@link Mission}, check {@link #getMission(ItemStack)} to get it
     * @return {@code Optional<MissionConfig>} containing/not containing the {@link MissionConfig}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Optional<MissionConfig> getMissionConfig(@NotNull Mission mission) {
        checkMissionsLoaded();
        return getMissionConfig(mission.getConfigID());
    }

    /**
     * Checks if an ID matches a valid {@link MissionConfig}
     *
     * @param id the config's ID
     * @return {@code true} if valid
     * {@code false} if it's not
     * @throws IllegalStateException if called before the server loaded
     */
    public boolean isValidMissionId(@NotNull String id) {
        checkMissionsLoaded();
        return getMissionConfig(id).isPresent();
    }

    /**
     * Tries getting a {@link MissionConfig} using its ID
     *
     * @param id the config's ID
     * @return {@code Optional<MissionConfig>} containing/not containing the {@link MissionConfig}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Optional<MissionConfig> getMissionConfig(@NotNull String id) {
        checkMissionsLoaded();
        return Optional.ofNullable(plugin.getConfigManager().getMissions().get(id));
    }

    /**
     * Tries getting a {@link Mission} from an item
     *
     * @param item the item to get from
     * @return {@code Optional<Mission>} containing/not containing the mission object
     */
    @NotNull
    public Optional<Mission> getMission(@Nullable ItemStack item) {
        return Optional.ofNullable(manager.getMissionOrNull(item));
    }

    /**
     * Gets a {@link Map} of {@link String} config ID and {@link MissionConfig}
     * <p>
     * Cannot be called before the server loads
     *
     * @return {@code Map<String, MissionConfig}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Map<String, MissionConfig> getMissionConfigs() {
        checkMissionsLoaded();
        return plugin.getConfigManager().getMissions();
    }

    /**
     * Generates a {@link MissionConfig} using the weights of the categories
     *
     * @return {@code Optional<MissionConfig>} containing/not containing the {@link MissionConfig}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Optional<MissionConfig> getRandomMission() {
        checkMissionsLoaded();
        return Optional.ofNullable(manager.weightedRandomMission(getCategories()));
    }

    /**
     * Generates a random {@link MissionConfig} from a given category {@link String}
     *
     * @param category the category {@link String}
     * @return {@code Optional<MissionConfig>} containing/not containing the {@link MissionConfig}
     */
    @NotNull
    public Optional<MissionConfig> getRandomMission(@NotNull String category) {
        checkMissionsLoaded();
        Preconditions.checkArgument(isValidCategory(category), "Invalid category given");
        return Optional.ofNullable(manager.categoryRandomMission(category));
    }

    /**
     * Gets a {@link Set} of all {@link MissionConfig}s within a category
     *
     * @param category the name category
     * @return {@code Set<MissionConfig>} that may be empty if no missions are under that category
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Set<MissionConfig> getMissionConfigsInCategory(@NotNull String category) {
        checkMissionsLoaded();
        Preconditions.checkArgument(isValidCategory(category), "Invalid category given");
        return getMissionConfigs().values().stream()
                .filter(m -> m.category().equalsIgnoreCase(category))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a category {@link String} is a valid category
     *
     * @param category the category {@link String} to validate
     * @return {@code true} if it is valid
     * {@code false} if it isn't
     */
    public boolean isValidCategory(@NotNull String category) {
        return getCategories().containsKey(category);
    }

    /**
     * Gets a {@link Map} containing all categories and their weights
     * You can be assured that this map will never be empty.
     *
     * @return {@code Map<String, Integer>}
     */
    @NotNull
    public Map<String, Integer> getCategories() {
        return plugin.getConfigManager().getMainConfig().categories();
    }

    /**
     * Gets the {@link TagResolver} for a mission.
     * <p>
     * The returned resolver has the following placeholders:
     * <ul>
     *   <li>{@code <uuid>} – the unique UUID of the mission</li>
     *   <li>{@code <type>} – the mission type ID</li>
     *   <li>{@code <targets>} – a list of valid targets, or {@code None}, delimiter from {@link MainConfig}</li>
     *   <li>{@code <progress>} – the current mission's progress</li>
     *   <li>{@code <requirement>} – the progress required to complete the mission</li>
     *   <li>{@code <percentage>} – the completion percentage (0–100)</li>
     *   <li>{@code <config_id>} – the mission config ID</li>
     *   <li>{@code <completed>} – whether the mission is completed or not</li>
     * </ul>
     *
     * @param mission the {@link Mission}
     * @return the {@link TagResolver} containing mission placeholders
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public TagResolver getMissionTags(@NotNull Mission mission) {
        checkMissionsLoaded();
        return manager.getMissionTags(mission);
    }

    /**
     * Checks whether an {@link ItemStack} is a mission or not
     *
     * @param item the item to check
     * @return {@code true} if it's a {@link Mission}
     * {@code false} if it's not
     * @throws IllegalStateException if called before the server loaded
     */
    public boolean isMission(@Nullable ItemStack item) {
        return manager.isMission(item);
    }

    /**
     * Checks whether an {@link ItemStack} is a broken mission or not
     *
     * @param item the mission item to check
     * @return {@code true} if it's a broken mission
     * {@code false} if it's not
     */
    public boolean isBrokenMission(@Nullable ItemStack item) {
        return manager.isBrokenMission(item);
    }

    /**
     * Creates a mission item given a missionConfig
     *
     * @param config the missionConfig to create the mission item with
     * @return {@link ItemStack}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public ItemStack createMissionItem(@NotNull MissionConfig config) {
        checkMissionsLoaded();
        return manager.createMissionItem(config);
    }

    /**
     * Applies a modification to a mission item
     *
     * @param missionItem  the mission item
     * @param modification the modification to be applied
     * @return {@code true} if the modification was successful
     * {@code false} if the modification was unsuccessful
     * @throws IllegalStateException if called before the server loaded
     */
    public boolean modifyMission(@NotNull ItemStack missionItem, @NotNull Consumer<Mission> modification) {
        checkMissionsLoaded();
        return manager.editMission(missionItem, modification);
    }

    /**
     * Claims the mission rewards of the ItemStack, handles command execution on FoliaMC and executes commands as ConsoleSender
     *
     * @param player      player to give rewards to
     * @param missionItem the mission item
     * @return {@code true} if the rewards were successfully claimed,
     * {@code false} if the item is not a mission
     * @throws IllegalStateException if called before the server loaded
     */
    public boolean claimMissionRewards(@NotNull Player player, @NotNull ItemStack missionItem) {
        checkMissionsLoaded();
        if (!isMission(missionItem)) return false;
        manager.giveRewards(missionItem, player);
        return true;
    }

    /**
     * Checks if a player has a mission
     * <p>
     * See {@link #hasMission(Player, Predicate)} if you need filtering
     *
     * @param player player to look in the inventory of
     * @return {@code true} if found
     * {@code false} if not found
     */
    public boolean hasMission(@NotNull Player player) {
        return hasMission(player, m -> true);
    }

    /**
     * Checks if a player has a mission matching a predicate
     *
     * @param player player to look in the inventory of
     * @param filter filter to match missions for, e.g, the first completed mission or only break missions
     * @return {@code true} if found
     * {@code false} if not found
     */
    public boolean hasMission(@NotNull Player player, @NotNull Predicate<Mission> filter) {
        return getFirstMission(player, filter).isPresent();
    }

    /**
     * Tries to get all mission items in a players inventory
     *
     * @param player player to look in the inventory of
     * @return {@code List<ItemStack>} containing all missions or an empty list if none found
     */
    @NotNull
    public List<ItemStack> getAllMissionItems(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        return getAllMissionsSlots(inventory).stream().map(inventory::getItem).toList();
    }

    /**
     * Tries to get all mission slots in an inventory
     * <p>
     * See {@link #getAllMissionsSlots(Inventory, Predicate)} if you need filtering
     *
     * @param inventory the inventory to find a mission in
     * @return {@code LinkedHashSet<Integer>} containing all slot indexes containing missions or an empty set if none.
     */
    @NotNull
    public LinkedHashSet<Integer> getAllMissionsSlots(@NotNull Inventory inventory) {
        return getAllMissionsSlots(inventory, m -> true);
    }

    /**
     * Tries to get all mission slots in an inventory
     * <p>
     * See {@link #getAllMissionsSlots(Inventory)} if you don't need a predicate
     *
     * @param inventory the inventory to find a mission in
     * @param filter    filter to match missions for, e.g, the first completed mission or only break missions
     * @return {@code LinkedHashSet<Integer>} containing all slot indexes containing missions or an empty set if none.
     */
    @NotNull
    public LinkedHashSet<Integer> getAllMissionsSlots(@NotNull Inventory inventory, @NotNull Predicate<Mission> filter) {
        LinkedHashSet<Integer> indexes = new LinkedHashSet<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            Optional<Mission> m = getMission(item);
            if (m.isEmpty() || !filter.test(m.get())) continue;
            indexes.add(i);
        }
        return indexes;
    }

    /**
     * Tries to get the first mission slot in an inventory
     * <p>
     * See {@link #getAllMissionsSlots(Inventory, Predicate)} if you need filtering
     *
     * @param inventory the inventory to find a mission in
     * @return the index of the first mission slot or {@code -1} if not found
     * @throws IllegalStateException if called before the server loaded
     */
    public int getFirstMissionSlot(@NotNull Inventory inventory) {
        return getFirstMissionSlot(inventory, m -> true);
    }

    /**
     * Tries to get the first mission slot in an inventory
     * <p>
     * See {@link #getFirstMissionSlot(Inventory)} if you don't need a predicate
     *
     * @param inventory the inventory to find a mission in
     * @param filter    filter to match missions for, e.g, the first completed mission or only break missions
     * @return the index of the first mission slot or {@code -1} if not found
     * @throws IllegalStateException if called before the server loaded
     */
    public int getFirstMissionSlot(@NotNull Inventory inventory, @NotNull Predicate<Mission> filter) {
        var indexes = getAllMissionsSlots(inventory, filter);
        if (indexes.isEmpty()) return -1;
        return indexes.getFirst();
    }

    /**
     * Overload of {@link #getFirstMission(Player, Predicate)} that finds the first mission without a predicate
     * <p>
     * See {@link #getFirstMission(Player, Predicate)} if you need filtering
     *
     * @param player player to look in the inventory of
     * @return {@code Optional<ItemStack>}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Optional<ItemStack> getFirstMission(@NotNull Player player) {
        return getFirstMission(player, m -> true);
    }

    /**
     * Tries to get the first mission in a player's inventory
     * <p>
     * See {@link #getFirstMission(Player)} if you don't need a predicate
     *
     * @param player player to look in the inventory of
     * @param filter filter to match missions for, e.g, the first completed mission or only break missions
     * @return {@code Optional<ItemStack>}
     * @throws IllegalStateException if called before the server loaded
     */
    @NotNull
    public Optional<ItemStack> getFirstMission(@NotNull Player player, @NotNull Predicate<Mission> filter) {
        checkMissionsLoaded();
        int firstSlot = getFirstMissionSlot(player.getInventory(), filter);
        if (firstSlot == -1) return Optional.empty();
        return Optional.ofNullable(player.getInventory().getItem(firstSlot));
    }


    /**
     * Finds the first mission in a player's inventory matching the given type and applies a modification.
     * This is intended for simple mission types that do not require a specific context, like `walk` or `xp`.
     *
     * @param player       The player to check.
     * @param type         The {@link MissionType} to look for.
     * @param modification The modification to apply to the mission if found (e.g., incrementing progress).
     * @throws IllegalStateException if called before mission configurations are loaded.
     */
    public void findAndProgressMission(@NotNull Player player, @NotNull MissionType type, @NotNull Consumer<Mission> modification) {
        checkMissionsLoaded();
        manager.findAndModifyFirstMission(player, type, modification);
    }

    /**
     * Finds the first mission in a player's inventory matching the type and context, then applies a modification.
     * This is the primary method for progressing targeted missions like breaking blocks or killing entities.
     * <p>
     * Example of usage:
     * <pre>{@code
     * // In a BlockBreakEvent listener:
     * BlockBreakEvent event = ...;
     * Player player = event.getPlayer();
     * BlockContext context = new BlockContext(event.getBlock());
     *
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     * api.findAndProgressMission(player, Break.INSTANCE, context,
     *     mission -> mission.incrementProgress(1));
     * }</pre>
     *
     * @param player       The player to check.
     * @param type         The {@link TargetedMissionType} to look for.
     * @param context      The {@link MissionContext} providing details about the action (e.g., the block broken).
     * @param modification The modification to apply to the mission if found.
     * @param <C>          The type of the MissionContext.
     * @throws IllegalStateException if called before mission configurations are loaded.
     */
    public <C extends MissionContext> void findAndProgressMission(@NotNull Player player, @NotNull TargetedMissionType<C, ?> type, @NotNull C context, @NotNull Consumer<Mission> modification) {
        checkMissionsLoaded();
        manager.findAndModifyFirstMission(player, type, context, modification);
    }


    /**
     * Private helper to ensure mission access never happens before they are loaded
     */
    private void checkMissionsLoaded() {
        Preconditions.checkState(plugin.getConfigManager().isMissionsLoaded(), "Tried accessing mission config related method before server load");
    }

}
