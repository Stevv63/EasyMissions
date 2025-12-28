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
import io.github.stev6.easymissions.config.records.MainConfig;
import io.github.stev6.easymissions.config.records.MissionConfig;
import io.github.stev6.easymissions.mission.Mission;
import io.github.stev6.easymissions.mission.missiontype.types.MissionType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
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
        if (instance != null) {
            throw new IllegalStateException("EasyMissionsAPI already initialized");
        }
        instance = new EasyMissionsAPI(plugin);
    }

    /**
     * Gets an instance of the {@link EasyMissionsAPI} to access API methods
     *
     * @return the {@link EasyMissionsAPI} instance
     */
    public static EasyMissionsAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EasyMissionsAPI is not enabled/initialized");
        }
        return instance;
    }

    /**
     * Registers a {@link MissionType}
     * <p>
     * Registration of a {@link MissionType} must happen before {@link ServerLoadEvent} i.e, before the server loads
     *
     * @throws IllegalStateException    if called after the server loaded
     * @throws IllegalArgumentException if type ID already exists
     */
    public void registerType(MissionType... type) {
        Preconditions.checkState(!plugin.isServerLoaded(), "Cannot register types after server load");
        registry.registerType(type);
    }

    /**
     * Gets all available {@link MissionType}s in the {@link MissionTypeRegistry}
     * <p>
     * Third party {@link MissionType}s may not be available yet, schedule your call to run on the next tick
     * or {@link ServerLoadEvent} or in a context where the server has loaded to ensure all types are loaded
     *
     * @return {@code Map<String, MissionType>} containing all {@link MissionType}s
     */
    public Map<String, MissionType> getAllTypes() {
        return registry.types();
    }

    /**
     * Gets a {@link MissionType} using its ID
     * <p>
     * Third party {@link MissionType}s may not be available yet, schedule your call to run on the next tick
     * or {@link ServerLoadEvent} or in a context where the server has loaded to ensure all types are loaded
     *
     * @param id the type's ID
     * @return {@code Optional<MissionType>} containing/not containing the {@link MissionType}
     */
    public Optional<MissionType> getType(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    /**
     * Tries getting a {@link MissionConfig} from a {@link Mission}
     *
     * @param mission a {@link Mission}, check {@link #getMission(ItemStack)} to get it
     * @return {@code Optional<MissionConfig>} containing/not containing the {@link MissionConfig}
     * @throws IllegalStateException if called before the server loaded
     */
    public Optional<MissionConfig> getMissionConfig(Mission mission) {
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
    public boolean isValidMissionId(String id) {
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
    public Optional<MissionConfig> getMissionConfig(String id) {
        checkMissionsLoaded();
        return Optional.ofNullable(plugin.getConfigManager().getMissions().get(id));
    }

    /**
     * Tries getting a {@link Mission} from an item
     *
     * @param item the item to get from
     * @return {@code Optional<Mission>} containing/not containing the mission object
     */
    public Optional<Mission> getMission(ItemStack item) {
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
    public Optional<MissionConfig> getRandomMission(String category) {
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
    public Set<MissionConfig> getMissionConfigsInCategory(String category) {
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
    public boolean isValidCategory(String category) {
        return getCategories().containsKey(category);
    }

    /**
     * Gets a {@link Map} containing all categories and their weights
     * You can be assured that this map will never be empty.
     *
     * @return {@code Map<String, Integer>}
     */
    public Map<String, Integer> getCategories() {
        return Collections.unmodifiableMap(plugin.getConfigManager().getMainConfig().categories());
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
    public TagResolver getMissionTags(Mission mission) {
        checkMissionsLoaded();
        return manager.getMissionTags(mission);
    }

    /**
     * Checks whether an {@link ItemStack} is an item or not
     *
     * @param item the item to check
     * @return {@code true} if it's a {@link Mission}
     * {@code false} if it's not
     * @throws IllegalStateException if called before the server loaded
     */
    public boolean isMission(ItemStack item) {
        return getMission(item).isPresent();
    }

    /**
     * Creates a mission item given a missionConfig
     *
     * @param config the missionConfig to create the mission item with
     * @return {@link ItemStack}
     * @throws IllegalStateException if called before the server loaded
     */
    public ItemStack createMissionItem(MissionConfig config) {
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
    public boolean modifyMission(ItemStack missionItem, Consumer<Mission> modification) {
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
    public boolean claimMissionRewards(Player player, ItemStack missionItem) {
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
    public boolean hasMission(Player player) {
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
    public boolean hasMission(Player player, Predicate<Mission> filter) {
        return getFirstMission(player, filter).isPresent();
    }

    /**
     * Tries to get all mission items in a players inventory
     *
     * @param player player to look in the inventory of
     * @return {@code List<ItemStack>} containing all missions or an empty list if none found
     */
    public List<ItemStack> getAllMissionItems(Player player) {
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
    public LinkedHashSet<Integer> getAllMissionsSlots(Inventory inventory) {
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
    public LinkedHashSet<Integer> getAllMissionsSlots(Inventory inventory, Predicate<Mission> filter) {
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
    public int getFirstMissionSlot(Inventory inventory) {
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
    public int getFirstMissionSlot(Inventory inventory, Predicate<Mission> filter) {
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
    public Optional<ItemStack> getFirstMission(Player player) {
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
    public Optional<ItemStack> getFirstMission(Player player, Predicate<Mission> filter) {
        checkMissionsLoaded();
        int firstSlot = getFirstMissionSlot(player.getInventory(), filter);
        return Optional.ofNullable(player.getInventory().getItem(firstSlot));
    }


    /**
     * Overload of {@link #tryFindAndModifyMission(Player, String, String, Consumer)} Without a target, tries to find the first mission with given type, if found it applies the modifications on it
     * <p>
     * This will respect blacklisted worlds and stop if the player is in a blacklisted world, it handles broken and
     * missing config entries on the mission and sets their display to indicate that they're broken and handles wildcard matching.
     * <p>
     * Additionally, this will call the MissionProgressEvent on successful progression
     * <p>
     * See {@link #tryFindAndModifyMission(Player, String, String, Consumer)} if you want to modify a mission with a target like a break mission
     * <p>
     * Example of usage:
     * <pre>{@code
     * // In an event listener of your own, here it is when a player trades with a villager:
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     * api.tryFindAndModifyMission(player, "trade",
     *     mission -> mission.incrementProgress(1));
     * }</pre>
     *
     * @param player       the player to check
     * @param type         the required mission type to find
     * @param modification the modifications to apply on the mission if found
     * @throws IllegalStateException if called before the server loaded
     */
    public void tryFindAndModifyMission(Player player, String type, Consumer<Mission> modification) {
        checkMissionsLoaded();
        tryFindAndModifyMission(player, type, null, modification);
    }

    /**
     * Tries to find the first mission with given type and target string, if found it applies the modifications on it.
     * This will respect blacklisted worlds and stop if the player is in a blacklisted world, it handles broken and
     * missing config entries on the mission and sets their display to indicate that they're broken and handles wildcard matching.
     * <p>
     * Additionally, this will call the MissionProgressEvent on successful progression
     * <p>
     * See {@link #tryFindAndModifyMission(Player, String, Consumer)} if you want to modify a mission without specifying a target like a walk mission
     * <p>
     * Example of usage:
     * <pre>{@code
     * // In an event listener of your own, here it is when a player tames an entity:
     * EasyMissionsAPI api = EasyMissionsAPI.getInstance();
     * api.tryFindAndModifyMission(player, "tame", entity.getType().name(),
     *     mission -> mission.incrementProgress(1));
     * }</pre>
     *
     * @param player       the player to check
     * @param type         the required mission type to find
     * @param target       the target to match against the mission's valid target list
     * @param modification the modifications to apply on the mission if found
     * @throws IllegalStateException if called before the server loaded
     * @apiNote Target strings given will always be normalized to lowercase
     * @apiNote Only the hotbar, the inventory storage and the offhand are checked, armor slots are skipped
     */
    public void tryFindAndModifyMission(Player player, String type, String target, Consumer<Mission> modification) {
        checkMissionsLoaded();
        manager.findAndModifyFirstMission(player, type, target, modification);
    }


    /**
     * Private helper to ensure mission access never happens before they are loaded
     */
    private void checkMissionsLoaded() {
        if (!plugin.getConfigManager().isMissionsLoaded())
            throw new IllegalStateException("Tried accessing mission config related method before server load");
    }

}
