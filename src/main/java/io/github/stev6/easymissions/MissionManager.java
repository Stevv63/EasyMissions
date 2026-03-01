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

import com.google.common.base.Verify;
import io.github.stev6.easymissions.config.ConfigManager;
import io.github.stev6.easymissions.config.data.MissionConfig;
import io.github.stev6.easymissions.context.MissionContext;
import io.github.stev6.easymissions.event.MissionClaimEvent;
import io.github.stev6.easymissions.event.MissionProgressEvent;
import io.github.stev6.easymissions.mission.Mission;
import io.github.stev6.easymissions.mission.MissionPersistentDataType;
import io.github.stev6.easymissions.option.MissionOption;
import io.github.stev6.easymissions.type.MissionType;
import io.github.stev6.easymissions.type.TargetedMissionType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

@ApiStatus.Internal
public class MissionManager {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final ConfigManager configManager;
    private final NamespacedKey dataKey;
    private final NamespacedKey invalidKey;
    private final EasyMissions plugin;

    public MissionManager(EasyMissions plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dataKey = new NamespacedKey(plugin, "mission_data");
        this.invalidKey = new NamespacedKey(plugin, "invalid_config");
    }

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack createMissionItem(MissionConfig config) {
        int req = Math.max(1, config.requirementRange().random());
        Mission m = Mission.create(config.key(), req);
        ItemStack i = new ItemStack(config.itemMaterial());

        i.editPersistentDataContainer(pdc -> pdc.set(dataKey, MissionPersistentDataType.INSTANCE, m));

        TagResolver tags = getMissionTags(m);
        MiniMessage mm = MINI_MESSAGE;

        List<Component> lore = config.lore().stream().map(line -> mm.deserialize(line, tags)).toList();
        Component displayName = mm.deserialize(config.name(), tags);

        i.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        i.unsetData(DataComponentTypes.ENCHANTABLE);
        i.unsetData(DataComponentTypes.REPAIRABLE);
        i.unsetData(DataComponentTypes.GLIDER);
        i.unsetData(DataComponentTypes.TOOL);
        i.unsetData(DataComponentTypes.WEAPON);
        i.unsetData(DataComponentTypes.BLOCKS_ATTACKS);
        i.unsetData(DataComponentTypes.EQUIPPABLE);
        i.unsetData(DataComponentTypes.CONSUMABLE);
        i.setData(DataComponentTypes.UNBREAKABLE);
        i.setData(DataComponentTypes.REPAIR_COST, Integer.MAX_VALUE);
        i.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        i.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        i.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(
                DataComponentTypes.DAMAGE,
                DataComponentTypes.UNBREAKABLE,
                DataComponentTypes.TOOL).build());

        i.setData(DataComponentTypes.RARITY, config.itemRarity());
        i.setData(DataComponentTypes.ITEM_NAME, Component.text("Easy Missions mission"));
        i.setData(DataComponentTypes.CUSTOM_NAME, displayName);

        var optionalModel = m.isCompleted() ? config.completedItemModel() : config.itemModel();
        if (optionalModel != null) i.setData(DataComponentTypes.ITEM_MODEL, optionalModel);

        i.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());

        return i;
    }

    @Nullable
    public Mission getMissionOrNull(@Nullable ItemStack i) {
        if (i == null || i.isEmpty()) return null;
        return i.getPersistentDataContainer().get(dataKey, MissionPersistentDataType.INSTANCE);
    }

    public boolean isMission(@Nullable ItemStack i) {
        if (i == null || i.isEmpty()) return false;
        return i.getPersistentDataContainer().has(dataKey);
    }

    public boolean isBrokenMission(ItemStack i) {
        if (!isMission(i)) return false;
        return i.getPersistentDataContainer().has(invalidKey);
    }

    public NavigableMap<Integer, Mission> getMissionsInInventory(@NotNull Inventory inv, @Nullable Set<Integer> toSkip) {
        NavigableMap<Integer, Mission> toReturn = new TreeMap<>();
        for (int idx = 0; idx < inv.getSize(); idx++) {
            if (toSkip != null && toSkip.contains(idx)) continue;
            ItemStack i = inv.getItem(idx);
            Mission m = getMissionOrNull(i);
            if (m != null) toReturn.put(idx, m);
        }
        return toReturn;
    }

    public void findAndModifyFirstMission(Player p, MissionType type, Consumer<Mission> doThing) {
        findAndModifyFirstMission(p, type, null, doThing);
    }

    public <C extends MissionContext> void findAndModifyFirstMission(Player p, MissionType type, C ctx, Consumer<Mission> doThing) {
        if (configManager.getMainConfig().mission().cacheSlots())
            findFromCache(p, type, ctx, doThing);
        else
            findFromInventory(p, type, ctx, doThing);
    }

    private <C extends MissionContext> void findFromCache(Player p, MissionType type, C ctx, Consumer<Mission> doThing) {
        var cache = plugin.getMissionCache().getCachedMissionsForPlayer(p);
        if (cache.isEmpty()) return;

        for (var entry : cache.entrySet()) {
            int slot = entry.getKey();
            MissionConfig config = entry.getValue();

            if (config.type() != type) continue;

            if (config.blacklistedWorlds().contains(p.getWorld().getUID())) continue;

            if (type instanceof TargetedMissionType<?, ?> targetedType && config.data() != null) {
                if (!targetedType.matchesRaw(config.data(), ctx)) continue;
            }

            ItemStack i = p.getInventory().getItem(slot);
            Mission m = getMissionOrNull(i);

            if (m == null || !m.getConfigID().equals(config.key())) {
                plugin.getLogger().log(Level.SEVERE, "Cache has corrupted data for " + p.getName() + " please report this to the developers");
                plugin.getMissionCache().handlePlayer(p);
                findFromInventory(p, type, ctx, doThing);
                return;
            }

            if (m.isCompleted()) continue;

            if (attemptMissionProgress(p, i, m, config, ctx, doThing)) return;

        }
    }

    private <C extends MissionContext> void findFromInventory(Player p, MissionType type, C ctx, Consumer<Mission> doThing) {
        PlayerInventory inv = p.getInventory();
        for (int idx = 0; idx <= 40; idx++) {
            if (idx == 36) idx = 40;

            ItemStack i = inv.getItem(idx);
            Mission m = getMissionOrNull(i);

            if (m == null || m.isCompleted() || i == null) continue;

            MissionConfig config = getMissionConfigOrNull(m);
            if (config == null) {
                handleBrokenMission(i, m.getConfigID());
                continue;
            } else if (isBrokenMission(i)) updateMissionData(i, m, true);

            if (config.type() != type) continue;

            if (config.blacklistedWorlds().contains(p.getWorld().getUID())) continue;

            if (type instanceof TargetedMissionType<?, ?> targetedType && config.data() != null) {
                if (!targetedType.matchesRaw(config.data(), ctx)) continue;
            }

            if (attemptMissionProgress(p, i, m, config, ctx, doThing)) return;
        }
    }

    private <C extends MissionContext> boolean attemptMissionProgress(
            Player p,
            ItemStack i,
            Mission m,
            MissionConfig config,
            C ctx,
            Consumer<Mission> doThing) {
        try {
            Mission optionClone = Mission.recreate(m.getConfigID(), m.getRequirement(), m.isCompleted(), m.getProgress(), m.getUUID());
            for (MissionOption option : config.options()) {
                if (!option.check(p, optionClone, i, ctx)) return false;
            }

            int oldProgress = m.getProgress();
            boolean oldCompleted = m.isCompleted();

            doThing.accept(m);
            Mission eventClone = Mission.recreate(m.getConfigID(), m.getRequirement(), m.isCompleted(), m.getProgress(), m.getUUID());
            MissionProgressEvent event = new MissionProgressEvent(p, eventClone, i, oldProgress, m.getProgress());
            if (!event.callEvent()) {
                m.setProgress(oldProgress);
                return false;
            }

            m.setProgress(event.getNewProgress());
            if (m.getProgress() >= m.getRequirement()) m.setCompleted(true);

            if (m.getProgress() != oldProgress || m.isCompleted() != oldCompleted) {
                updateMissionData(i, m, true);
            }

            return true;

        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Error processing mission '" + config.key() + "' for " + p.getName(), ex);
            return false;
        }
    }

    @Nullable
    public MissionConfig weightedRandomMission(@NotNull Map<String, Integer> categoryWeight) {
        int total = 0;
        for (Integer i : categoryWeight.values()) if (i > 0) total += i;
        if (total <= 0) return null;
        int rand = ThreadLocalRandom.current().nextInt(total);
        int gained = 0;

        for (var category : categoryWeight.entrySet()) {
            Integer i = category.getValue();
            if (i <= 0) continue;
            gained += i;
            if (gained > rand) return categoryRandomMission(category.getKey());
        }
        return null;
    }

    @Nullable
    public MissionConfig categoryRandomMission(@NotNull String category) {
        List<MissionConfig> missions = configManager.getMissions().values().stream()
                .filter(m -> m.category().equalsIgnoreCase(category))
                .toList();

        if (missions.isEmpty()) return null;
        return missions.get(ThreadLocalRandom.current().nextInt(missions.size()));
    }

    public void updateMissionData(@NotNull ItemStack i, @NotNull Mission m, boolean updateDisplay) {
        MissionConfig config = getMissionConfigOrNull(m);

        i.editPersistentDataContainer(pdc -> {
            if (config != null) pdc.remove(invalidKey);
            pdc.set(dataKey, MissionPersistentDataType.INSTANCE, m);
        });

        if (updateDisplay) {
            Verify.verifyNotNull(
                    config,
                    "updateDisplay requested for broken mission"
            );
            updateMissionDisplay(i, m, config);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void updateMissionDisplay(@NotNull ItemStack i, @NotNull Mission m, @NotNull MissionConfig c) {
        TagResolver tags = getMissionTags(m);
        MiniMessage mm = MINI_MESSAGE;

        Component displayName = mm.deserialize(
                m.isCompleted() ? c.completedName() : c.name(),
                tags);

        List<Component> lore = (m.isCompleted() ? c.completedLore() : c.lore())
                .stream()
                .map(line -> mm.deserialize(line, tags))
                .toList();

        i.setData(DataComponentTypes.CUSTOM_NAME, displayName);
        i.setData(DataComponentTypes.LORE, ItemLore.lore().lines(lore).build());

        var model = m.isCompleted() ? c.completedItemModel() : c.itemModel();
        i.setData(DataComponentTypes.ITEM_MODEL, Objects.requireNonNullElseGet(model, () -> i.getType().getKey()));
    }

    public void giveRewards(ItemStack i, Player p) {
        Mission mission = getMissionOrNull(i);
        if (mission == null) return;
        MissionConfig config = getMissionConfigOrNull(mission);
        if (config == null) return;

        MissionClaimEvent event = new MissionClaimEvent(p, mission, i, config.rewards());
        if (!event.callEvent()) return;

        p.getInventory().removeItemAnySlot(i);

        for (String reward : event.getCommands()) {
            String command = reward.replace("<player>", p.getName());
            if (command.startsWith("say ")) p.sendRichMessage(command.substring(4).trim());
            else plugin.runCommand(command, Bukkit.getConsoleSender());
        }
    }

    public boolean editMission(@NotNull ItemStack i, @NotNull Consumer<Mission> doThing) {
        Mission m = getMissionOrNull(i);
        if (m == null) return false;

        boolean oldCompleted = m.isCompleted();
        doThing.accept(m);
        if (oldCompleted == m.isCompleted() && m.getProgress() >= m.getRequirement()) m.setCompleted(true);

        if (oldCompleted == m.isCompleted() && m.getProgress() < m.getRequirement() && m.isCompleted())
            m.setCompleted(false);

        updateMissionData(i, m, true);
        return true;
    }

    public TagResolver getMissionTags(@NotNull Mission m) {
        int percentage = (int) ((double) m.getProgress() / m.getRequirement() * 100);
        MissionConfig c = getMissionConfigOrNull(m);
        boolean hasConfig = c != null;
        String category = hasConfig ? c.category() : "unknown";
        String type = hasConfig ? c.type().id() : "unknown";

        return TagResolver.resolver(
                Placeholder.unparsed("uuid", m.getUUID().toString()),
                Placeholder.unparsed("type", type),
                Placeholder.unparsed("type_cap", StringUtils.capitalize(type)),
                Placeholder.unparsed("progress", String.valueOf(m.getProgress())),
                Placeholder.unparsed("requirement", String.valueOf(m.getRequirement())),
                Placeholder.unparsed("category", category),
                Placeholder.unparsed("category_cap", StringUtils.capitalize(category)),
                Placeholder.unparsed("percentage", String.valueOf(percentage)),
                Placeholder.unparsed("config_id", m.getConfigID()),
                Placeholder.unparsed("completed", String.valueOf(m.isCompleted())),
                Placeholder.parsed("task", hasConfig ? c.taskDescription() : "")
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public void handleBrokenMission(ItemStack i, String id) {
        if (isBrokenMission(i)) return;
        if (getMissionOrNull(i) == null) return;
        i.editPersistentDataContainer(pdc -> pdc.set(invalidKey, PersistentDataType.BYTE, (byte) 1));
        plugin.getLogger().severe("Config entry \"" + id + "\" is missing/invalid, please check your config if this is not intentional!");
        ItemLore data = i.getData(DataComponentTypes.LORE);
        List<Component> lore;
        if (data == null) lore = new ArrayList<>();
        else lore = new ArrayList<>(data.lines());
        lore.addAll(0, Stream.of("<red>MISSION HAS INVALID CONFIG ID: </red>" + id).map(MINI_MESSAGE::deserialize).toList());

        i.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(lore).build());
        i.setData(DataComponentTypes.CUSTOM_NAME, MINI_MESSAGE.deserialize("BROKEN MISSION"));
    }

    @Nullable
    public MissionConfig getMissionConfigOrNull(@NotNull Mission m) {
        return configManager.getMissions().get(m.getConfigID());
    }
}
